package it.polimi.ingsw.network.client;

import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.CardsDeck;
import it.polimi.ingsw.model.PatternCard;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.server.SocketParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.network.runServer.DEFAULT_SOCKET_PORT;

public class SocketClient implements ClientInterface {
    private Player player;
    ObservableList<ClientInterface> clients = FXCollections.observableArrayList();
    Socket socket;
    int port;
    SocketParser socketParserClient = new SocketParser();
    ObservableList<PatternCard> patternCards = FXCollections.observableArrayList();
    SocketHandlerClient socketHandlerClient;
    StringProperty gameStatus = new SimpleStringProperty("WAITING_LOGIN");

    @Override
    public void connect(String serverAddress, int portNumber, String userName) throws IOException {
        player = new Player(userName);
        port = portNumber;
        // Make connection and initialize streams
        socket = new Socket(serverAddress, DEFAULT_SOCKET_PORT);
        socketHandlerClient = new SocketHandlerClient(this, socket);
        socketHandlerClient.start();

    }
    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void login()  {
        socketHandlerClient.send("request-login-" + player.getName() + "-end");

    }

    @Override
    public void pushData() {

    }

    @Override
    public void updatePlayersInfo(ClientInterface c)  {
        clients.add(c);
    }

    @Override
    public ObservableList<ClientInterface> getClients() {
        return clients;
    }

    @Override
    public void setCurrentLogged(List<ClientInterface> clients) {
        clients.addAll(clients);
    }

    public String processInput(String type, String header, String data){
        if (type.equals("response")){
            switch(header) {
                case "login":
                    String response=data;
                    if(data.equals("Login Accepted !")){
                        System.out.println("Login successful!");
                        updatePlayersInfo(this);
                        gameStatus.set("WAITING_PLAYERS");

                    } else {
                        System.out.println("Try with a different username, or maybe the game is already began so... :(");
                    }
                    break;
                default:
                    System.out.println("Wrong message!");
            }
        }else if(type.equals("update")){
                switch(header){
                    case "start": System.out.println(data);
                                gameStatus.set("STARTED");
                        break;
                    case "users": ObservableList<String> names = socketParserClient.parseData(data);
                        System.out.println("You are playing against");
                        for (String s : names) {
                            System.out.println(s);
                            ClientInterface client = new SocketClient();
                            ((SocketClient) client).setPlayer(s);
                            updatePlayersInfo(client);
                        }
                        break;

                    case "userLogged": String name = data;
                        System.out.println("user " + name + "logged in");
                        ClientInterface client = new SocketClient();
                        ((SocketClient) client).setPlayer(name);
                        updatePlayersInfo(client);

                        break;

                    default: break;

                }
        }else if(type.equals("request")){
                switch(header){
                    case "initPattern":
                        gameStatus.set("WAITING_PATTERNCARD");
                        ObservableList<String> patternNames = socketParserClient.parseData(data);
                        CardsDeck deck = new CardsDeck("PatternCards.json", new TypeToken<List<PatternCard>>() {
                        }.getType());
                        List<PatternCard> list = new ArrayList<>();
                        list.add((PatternCard) deck.getByName(patternNames.get(0)+patternNames.get(1)));
                        list.add((PatternCard) deck.getByName(patternNames.get(2)+patternNames.get(3)));
                        System.out.println("Choose your pattern card between : " + patternNames.get(0)+ ", " + patternNames.get(1) + ", " + patternNames.get(2) + ", " + patternNames.get(3));
                        patternCards.addAll(list);

                        break;
                    default: break;

                }
        }

        return null;
    }


    public void setPlayer(String name){
        this.player = new Player(name);

    }

    private class SocketHandlerClient extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        SocketClient client;
        Socket socket;
        SocketParser socketParser;

        private SocketHandlerClient(SocketClient client, Socket socket){
            this.client = client;
            this.socket = socket;
            socketParser = new SocketParser();
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            }catch(IOException e){e.printStackTrace();}
        }
        public synchronized void run(){
            // Get messages from the server, line by line;
            System.out.println("client is listening");
            try {


                while (true) {
                    String input = in.readLine();
                    if (input != null) {
                        if (input.equals("Hello from server")) {
                            System.out.println("Connection with server established");
                        }else{
                            socketParser.parseInput(input);
                            System.out.println("Processing " + socketParser.getType() + socketParser.getHeader() + socketParser.getData());
                            String out = processInput(socketParser.getType(), socketParser.getHeader(), socketParser.getData());
                        }
                    }
                }
            }catch (IOException e){
                System.out.println("Error while handlig client socket");
            }
        }

        private synchronized void send(String s){
            while (out==null){}
            out.println(s);
        }
    }
}

