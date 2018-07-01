package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.server.RMI.RMIServerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIClientInterface extends ClientInterface, Remote {
    @Override
    void connect(String serverAddress, int portNumber, String userName) throws RemoteException;

    @Override
    String getName() throws RemoteException;

    @Override
    void requestPlacement(int number, String color, int row, int column)throws RemoteException;

    void loginResponse(boolean result) throws RemoteException;

    void addPlayersToProxy(List<Player> players) throws RemoteException;

    void addPlayerToProxy(Player player)throws RemoteException;

    void initPatternCardChoice(List<PatternCard> choices) throws RemoteException;

    void initGame(List<Player> p, int timeout) throws RemoteException;

    void setPrivateObj(String name) throws RemoteException;

    void setPublicObj(List<PublicObjectiveCard> publicObjCards) throws RemoteException;
    void setDraft(List<Die> draft) throws RemoteException;
    void beginTurn(String name, int round, int turn) throws RemoteException;
    void patternCardResponse(String name)throws RemoteException;

    void initTools(List<String> names) throws RemoteException;

    void setEndPoint(RMIServerInterface server) throws RemoteException;
    void updateOpponentsInfo(List<Player> players) throws RemoteException;
    void moveResponse(String name, boolean response, Die d, int row, int column) throws RemoteException;
    void moveTimeOut()throws RemoteException;
    void endCurrentTurn(String name) throws RemoteException;
    void endRound(List<Die> dice) throws RemoteException;

    public void chooseDieFromWindowPattern() throws RemoteException;
    public void chooseDieFromDraftPool() throws RemoteException ;
    public void chooseDieFromRoundTrack() throws RemoteException;
    public void chooseIfDecrease() throws RemoteException;
    public void chooseIfPlaceDie() throws RemoteException;
    public void chooseToMoveOneDie() throws RemoteException;
    public void setValue() throws RemoteException;
    public void setOldCoordinates() throws RemoteException;

    public void setNewCoordinates() throws RemoteException;


    public void sendDieFromWP(Die d, int row, int column) throws RemoteException;

    public void sendDieFromDP(Die d) throws RemoteException;

    public void sendDieFromRT(Die d, int round) throws RemoteException;

    public void sendDecreaseChoice(boolean choice) throws RemoteException;

    public void sendPlacementChoice(boolean choice) throws RemoteException;

    public void sendNumberDiceChoice(boolean choice) throws RemoteException;

    public void sendValue(int value) throws RemoteException;

    public void sendOldCoordinates(int row, int column) throws RemoteException;

    public void sendNewCoordinates(int row, int column) throws RemoteException;

}
