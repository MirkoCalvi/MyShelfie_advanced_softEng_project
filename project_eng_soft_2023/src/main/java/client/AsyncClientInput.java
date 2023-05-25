package client;

import myShelfieException.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import model.Tile;

public class AsyncClientInput implements Runnable{


    SocketClient socketClient;

    private Stream outClient;
    private Stream inClient;

    public AsyncClientInput(SocketClient socketClient){
        this.socketClient = socketClient;
        setStreams();
    }

    private void setStreams(){
        outClient = socketClient.getOutputStream();
        inClient = socketClient.getInputStream();
    }


    /**
     * Receives request from the server, parse the 'Action' field and so choose the action to perform on the client
     *
     */
    private void startServer(){

        JSONObject request = new JSONObject();
        JSONObject response = new JSONObject();

        while (true){

            if(!request.equals(null)) request.clear();
            if(!response.equals(null)) response.clear();

            try{
                request = inClient.read();
            } catch (InvalidOperationException e) {
                System.out.println("AsyncClientInput  --- InvalidOperationException occurred trying to read a new request");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("AsyncClientInput  --- IOException occurred trying to read a new request");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("AsyncClientInput  --- ClassNotFoundException occurred trying to read a new request");
                e.printStackTrace();
            }

            String Action = (String) request.get("Action");

            switch (Action){

                //enterNumberOfPlayers
                case "askNumberOfPlayers":
                    try{
                        socketClient.enterNumberOfPlayers();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    break;


                //updateBoard
                case "notifyUpdatedBoard":
                    try{

                        Tile[][] board = (Tile[][]) request.get("Param1");
                        Tile[][] shelf = (Tile[][]) request.get("Param2");
                        Map<String, Tile[][]> map = (Map<String, Tile[][]>) request.get("Param3");
                        int score = (int) request.get("Param4");

                        socketClient.updateBoard(board,shelf,map,score);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    break;


                //startPlaying
                case "notifyStartPlaying":

                    try{
                        int pgcNum = (int) request.get("Param1");
                        Map<Tile, Integer[]> cardMap = (Map<Tile, Integer[]>) request.get("Param2");
                        int cgc1Num = (int) request.get("Param3");
                        int cgc2Num = (int) request.get("Param4");
                        int gameID = (int) request.get("Param5");

                        socketClient.startPlaying(pgcNum,cardMap,cgc1Num,cgc2Num,gameID);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                    break;


                //startYourTurn
                case "notifyStartYourTurn":

                    try {
                        socketClient.startYourTurn();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                    break;

                //endYourTurn
                case "notifyEndYourTurn":
                    try {
                        socketClient.endYourTurn();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                    break;

                //endGame
                case "notifyEndGame":
                    try{
                        Map<Integer, String> results = (Map<Integer, String>) request.get("Param1");

                        socketClient.theGameEnd(results);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                //askPing
                case "askPing":
                    try {
                        socketClient.ping();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }


            }





        }

    }






    @Override
    public void run() {
        startServer();
    }
}
