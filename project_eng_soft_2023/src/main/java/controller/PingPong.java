package controller;

/**
 * check if the player is still connected
 */
public class PingPong implements Runnable{

    ControlPlayer controlPlayer;
    private boolean connected;

    private int counter;

    public PingPong(ControlPlayer cp){

        controlPlayer=cp;
        connected=false;
        counter=0;

    }

    @Override
    public void run() {

        while( true  ){

            (new Thread(new PingClient())).start(); //

            try {
                Thread.sleep(5000); //wait for 5 seconds
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //depending on the result after the sleep I set the status of the player
            if(connected){
                //if connected I change the status only if before he wasn't Online
                if(controlPlayer.getPlayerStatus().equals(PlayerStatus.NOT_ONLINE) )controlPlayer.setPlayerStatus(PlayerStatus.NOT_MY_TURN);
                counter=0;
            }

            else{
                controlPlayer.setPlayerStatus(PlayerStatus.NOT_ONLINE);
                System.out.println("    "+controlPlayer.getPlayerNickname()+" went offline ");

                counter++;
                //when counter reaches 12( -> 1 minute offline ) I disconnect the player from the game
                if(counter >=12 ){
                    controlPlayer.getGame().removePlayer(controlPlayer);

                    System.out.println("    "+controlPlayer.getPlayerNickname()+" timeout connection: removed ");
                    break;
                }
            }
        }
    }

    class PingClient implements Runnable{

        @Override
        public void run() {

            try{
                System.out.println("    ping to " + controlPlayer.getPlayerNickname());
                connected=controlPlayer.askPing();
            }catch (Exception e){
                connected=false;
            }
        }
    }

    public boolean getConnectionStatus(){
        return connected;
    }
}
