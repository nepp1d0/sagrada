package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.WindowPattern;
import it.polimi.ingsw.network.server.ServerInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import static it.polimi.ingsw.network.server.MainServer.DEFAULT_RMI_PORT;


public class RMIClient extends UnicastRemoteObject implements ClientInterface {
    public Player player;
    ObservableList<ClientInterface> clients = FXCollections.observableArrayList();
    ServerInterface server;
    ClientHandler ch;

    public RMIClient(ClientHandler ch) throws RemoteException{
        this.ch = ch;
    }

    protected RMIClient() throws RemoteException {
    }

    @Override
    public String getName() throws RemoteException {
        return null;
    }

    @Override
    public void login() throws RemoteException {

    }


    @Override
    public void connect(String serverAddress, int portNumber, String userName) throws RemoteException, IOException {

    }

    @Override
    public void validatePatternCard(WindowPattern w) {

    }

    @Override
    public void requestPlacement(int number, String color, int row, int column) {

    }
/*
    public RMIClient(String name, String hostName) throws RemoteException {
        player = new Player(name);
        try {
            System.out.println("Ip address : " + java.net.InetAddress.getLocalHost());
            //System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
            server = (ServerInterface) Naming.lookup("rmi://" + hostName + ":" + DEFAULT_RMI_PORT + "/sagrada");
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public void login() {
        try {
            if (server.login(this)) {
                System.out.println("Login accepted");
            } else {
                System.err.println("Login failed");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pushData() {

    }

    @Override
    public void updatePlayersInfo(ClientInterface c) {
        clients.add(c);

    }

    @Override
    public ObservableList<ClientInterface> getClients() {
        return clients;

    }

    @Override
    public void setCurrentLogged(List<ClientInterface> c) {
        clients.addAll(c);

    }

    @Override
    public void connect(String serverAddress, int portNumber, String userName) throws RemoteException, IOException {

    }*/
}
