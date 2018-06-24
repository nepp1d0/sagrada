package it.polimi.ingsw.network.server;

import it.polimi.ingsw.GameManager;
import it.polimi.ingsw.model.*;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MainServer {
    public static final int DEFAULT_RMI_PORT = 1234;
    public static final int DEFAULT_SOCKET_PORT= 3130;
    public static final int CONNECTION_TIMEOUT = 5;
    private RMIServerInterface rmiServer;  //TODO: check if it is really right to create the common server interface
    private ServerInterface socketServer;
    private Timer timer;
    private boolean timerIsRunning=false;
    private boolean isGameStarted = false;

    private List<ClientObject> connectedClients = new ArrayList<>();
    private List<ClientObject> inGameClients = new ArrayList<>();
    private GameManager gm;


    /** MainServer handles the two different type of connections
     * (RMI and Socket), and unify them to communicate with GameManager
     *
     * @param args are the parameter from command line
     *
     */
    public MainServer(String[] args){

        //RMI Server
        try {
            rmiServer = new RMIServer(connectedClients, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        new Thread(){
            public void run(){
                try {
                    rmiServer.start(args);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();


        //Socket server
        socketServer= new SocketServer(connectedClients, this);

        new Thread(){
            public void run(){
                try {

                    socketServer.start(args);

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();



    }


    public  boolean addClient(ClientObject client){
        if(isGameStarted){
            return false;
        }
        if(connectedClients == null ){
            connectedClients.add(client);
            return true;

        }else{

            for (ClientObject clients : connectedClients){
                try {
                    if (clients.getPlayer().getName().equals(client.getPlayer().getName())){
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            connectedClients.add(client);
            return true;
        }

    }

    public  boolean checkTimer() {
        if (connectedClients.size()<2){
            System.out.println("Waiting for more players . . .");
            if (timerIsRunning) {
                timer.cancel();
                timerIsRunning = false;
                System.out.println("Timer stopped");
            }
        }else {
            if (connectedClients.size()==2){
                timerIsRunning = true;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            //showClients();
                            System.out.println("Time to join the game is out !");
                            if (connectedClients.size() >= 2) {
                                System.out.println("Let the game begin !");
                                timerIsRunning = false;
                                if(!isGameStarted) {
                                    new Thread(){
                                        public void run(){
                                            initGame(getPlayersFromClients(connectedClients));
                                            isGameStarted = true;
                                        }
                                    }.start();

                                }
                            }
                        }catch (Exception e){
                            System.out.println("Exception inside timer!!!!!!!!!!!!!!!!!!!!!!!");
                            e.printStackTrace();
                        }
                    }
                }, CONNECTION_TIMEOUT*1000);

                System.out.println("Timer has started!!" );

            }else if(connectedClients.size()==4){
                if (timerIsRunning) {
                    timer.cancel();
                    timerIsRunning = false;
                    System.out.println("Timer stopped");
                }
                System.out.println("Let the game begin !");
                if(!isGameStarted) {
                    initGame(getPlayersFromClients(connectedClients));
                    isGameStarted = true;
                }


            } else if(connectedClients.size()>4){
                System.out.println("Too many users !");
                return false;
            }
        }
        return true;

    }


    public  void disconnect(ClientObject client){
            if(!isGameStarted){
                if(connectedClients.size()>1) {
                    for (ClientObject c : connectedClients) {
                        try {
                            if (!c.getPlayer().getName().equals(client.getPlayer().getName())) {
                                c.notifyPlayerDisconnection(client.getPlayer());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                connectedClients.remove(client);
                checkTimer();
            }else{
                try {
                    gm.disconnectPlayer(client.getPlayer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public  void initGame(List<Player> players){
        //Code to init Game manager
        gm = new GameManager(this, players);
        gm.init();

    }

    public  List<Player> getPlayersFromClients(List<ClientObject> clients){
        List<Player> players= new ArrayList<>();

        for (ClientObject c : clients){
            try {
                players.add(c.getPlayer());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return players;
    }

    public  void addLoggedPlayer(Player p) {
        for(ClientObject c : connectedClients){
            try {
                if (!c.getPlayer().getName().equals(p.getName())){
                    c.pushLoggedPlayer(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public  void addAlreadyLoogedPlayers(ClientObject client){
        if (connectedClients.size()>0) {
            try {
                client.pushPlayers(getPlayersFromClients(connectedClients));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void gameStartedProcedures(List<Player> players){
        inGameClients.addAll(connectedClients);
        for (ClientObject c : inGameClients){
            try {
                c.notifyGameStarted(players);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void choosePatternCard(List<PatternCard> patternCards, Player player){
        for(ClientObject c : inGameClients ){
            try {
                if (c.getPlayer().getName().equals(player.getName())){
                    c.requestPatternCardChoice(patternCards);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    public  void setPlayerChoice(ClientObject client, String name){
        //TODO: check correct pattern card
        try {
            while (gm==null){}
            client.pushPatternCardResponse(name);
            gm.completePlayerSetup(client.getPlayer(), name);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public  void initPlayersData(){
        for(ClientObject client1 : inGameClients){
            List<Player> thinPlayers = new ArrayList<>();
            for(ClientObject client2 : inGameClients){
                try {
                    if (!client1.getPlayer().getName().equals(client2.getPlayer().getName())){
                        thinPlayers.add(getOpponentVisibleFromClient(client2.getPlayer()));

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pushing opponents: " + thinPlayers.toString());
            try {
                client1.pushOpponentsInit(thinPlayers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public  Player getOpponentVisibleFromClient(Player p ){
        Player player = new Player(p.getName());
        player.getPlayerWindow().setWindowPattern(p.getPlayerWindow().getWindowPattern());
        player.setPrivateObjectiveCard(null);
        return player;
    }

    public  void setPublicObj(ObjCard[] publicObj){
        for(ClientObject c : inGameClients){
            try {
                c.pushPublicObj(publicObj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public  void setPrivateObj(Player p, ObjCard privateObjectiveCard){
        for (ClientObject c : inGameClients) {
            try {
                if (c.getPlayer().getName().equals(p.getName())) {
                    System.out.println("SETTING private " + privateObjectiveCard.getName() + "  to " + p.getName());
                    c.setPrivObj(privateObjectiveCard, getPlayersFromClients(inGameClients));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void setDraft(List<Die> draft){
        for (ClientObject c: inGameClients){
            try {
                c.pushDraft(draft);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void notifyBeginTurn(Player p ){
        for (ClientObject c : inGameClients){
            try {
                c.notifyTurn(p);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public  void notifyPlacementResponse(boolean response, Player p){
        for(ClientObject c : inGameClients){
            try {
                if (c.getPlayer().getName().equals(p.getName())){
                    c.notifyMoveResponse(response, "response");
                }else{
                    c.notifyMoveResponse(response, "update");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void handleMove(Die d, int row, int column, Player p ){
        gm.processMove(d, row, column, p);
    }

    //TODO
    public void notifyEndTurn(List<Player> players){}
    public void notifyEndRound(List<Player> players){}
    public void chooseDieFromRoundTrackForToolCard(List<Die> draftPool){
        //chooses the turn and the number of die
    }

    public void chooseDieFromWindowPatternForToolCards(){}
    public void chooseDieFromDraftPoolForToolCards(){}
    public void chooseOldCoodinatesForToolCards(){
        //chooses row and column of the die the player wants to move
    }
    public void chooseNewCoodinatesForToolCards(){
        //chooses row and column of where the player wants to place the die
    }
    public void chooseIfDecreaseForToolCards(){
        // chooses if the player wants to decrease or increase the value of the die
        // if he chooses true he wants to decrease the value, otherwise increase
    }
    public void chooseIfPlaceDieFOrToolCards(){
        //chooses if the player wants to place the die or to put it back in the draft pool
        // if he chooses true he wants to place the die on the window pattern otherwise put it back in the draft pool
    }
    public void chooseDieValueForToolCards(){
        //after drafting from the dice bag the player can choose the value of the die
    }
}
