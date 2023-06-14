package client;

import controller.ClientServerHandler;
import controller.GameHandler;
import myShelfieException.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class RMIClient extends Client {


    //-------------- RMI attributes --------------
    private ClientServerHandler clientServerHandler;
    private GameHandler gameHandler;
    private Registry registry;

    /**
     * constructor of ClientApp
     * @throws RemoteException
     */

    public RMIClient() throws RemoteException {

        super();
        model.setConnectionType(false);

    }

    @Override
    public void initializeClient() throws IOException, NotBoundException {

        //System.out.println("--- initialize the RMI Client --- ");

        getServerSettings();

        // Getting the registry
        registry = LocateRegistry.getRegistry(hostname, PORT);

        // Looking up the registry for the remote object
        this.clientServerHandler = (ClientServerHandler) registry.lookup("ServerAppService");


    }

    @Override
    public void getServerSettings() {

        Long PORT_pre;

        try{
            Object o = new JSONParser().parse(new FileReader("src/main/config/header.json")); //C:/Users/Utente/IS23-AM05/project_eng_soft_2023/
            JSONObject j =(JSONObject) o;
            Map arg = new LinkedHashMap();
            arg = (Map) j.get("serverSettings");

            hostname = (String) arg.get("hostname");
            PORT_pre = (Long) arg.get("RMIPORT");

            PORT = PORT_pre.intValue();

        }catch (FileNotFoundException e) {
            System.out.println("RMIClient --- FileNotFoundException occurred trying to retrieve server's information from header.json");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("RMIClient --- IOException occurred trying to retrieve server's information from header.json");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("RMIClient --- ParseException occurred trying to retrieve server's information from header.json");
            e.printStackTrace();
        }

    }


    /**
     * asks the server to log in, is divided in RMI and socket
     * @return GameHandler interface
     * @throws LoginException
     * @throws IOException
     * @throws RemoteException
     */
    @Override
    public void askLogin(String nick) throws LoginException, IOException, RemoteException{

        this.gameHandler= clientServerHandler.login(nick , this);

    }


    /**
     * asks the server to continue a game, is divided in RMI and socket
     * @return GameHandler interface
     * @throws LoginException
     * @throws RemoteException
     */
    @Override
    public void askContinueGame() throws LoginException, RemoteException {

        this.gameHandler= clientServerHandler.continueGame(model.getNickname(), this, model.getGameID());

        gameHandler.restoreSession();

    }


    /**
     * asks the server to leave the game I'm playing, is divided in RMI and socket
     * @return true if everything went fine
     * @throws RemoteException
     */
    @Override
    public void askLeaveGame() throws RemoteException, LoginException {

        clientServerHandler.leaveGame(model.getNickname(), model.getGameID());
        super.getPingChecker().stopPingProcess();


    }


    /**
     * asks the server to leave the game I'm playing, is divided in RMI and socket
     * @param coord
     * @return true if everything went fine
     * @throws InvalidChoiceException
     * @throws NotConnectedException
     * @throws InvalidParametersException
     * @throws RemoteException
     * @throws NotMyTurnException
     */
    @Override
    public void askBoardTiles( List<Integer> coord) throws InvalidChoiceException, NotConnectedException, InvalidParametersException, RemoteException, NotMyTurnException {

        gameHandler.chooseBoardTiles( coord );

    }

    @Override
    public void askInsertShelfTiles( int choosenColumn, List<Integer> coord) throws RemoteException, NotConnectedException, NotMyTurnException, InvalidChoiceException, InvalidLenghtException{

        gameHandler.insertShelfTiles( choosenColumn, coord);
    }

    @Override
    public void askSetNumberOfPlayers(int n, String nick) {

        try {
            clientServerHandler.setNumberOfPlayers(n, nick);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void askPassMyTurn() {

        try {
            System.out.println(" timeout!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
            gameHandler.passMyTurn();
        } catch (RemoteException e) {
            System.out.println("---error occurred when asking to skip the turn");
            throw new RuntimeException(e);
        }

    }

    @Override
    public void notifyPong() throws RemoteException {

        //System.out.println("notifyPong()");
        clientServerHandler.pong(model.getNickname(), model.getGameID());

    }

    @Override
    public void askPostMessage(String message, ArrayList<String> recipients){
        try {
            gameHandler.postMessage(message, recipients);
        } catch (RemoteException e) {
            System.out.println("---error occurred while posting the message");
        }
    }


}