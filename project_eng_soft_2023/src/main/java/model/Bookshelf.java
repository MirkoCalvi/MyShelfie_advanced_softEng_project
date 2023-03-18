package model;

import java.util.ArrayList;


public class Bookshelf {

    /**
     * shelf attribute represent the game bookshelf. Here is implemented with a 6x5 matrix
     */
    private Tile[][] shelf;


    /**
     * dimension of the shelf's matrix
     */
    private int r= 6; //rows
    private int c = 5; //columns

    /**
     * represents the PersonalGoalCard assigned at the beginning of the game
     */
    private PersonalGoalCard pgc;


    /**
     * represent all the token that the player can accumulate during the game
     */
    private Token tokenCG1;
    private Token tokenCG2;
    private Token tokenEOG;

    /**
     * represents the board used by all the players during the game
     * every update on the board must be visible to every player and so,  to everyone using a bookshelf
     * this motivates the choice of the static method for the attribute
     */
    private static Board board;


    /**
     *
     * @param board constructor instantiate the board described above
     * @param pgc constructor instantiate the pgc described above
     */
    public Bookshelf(Board board, PersonalGoalCard pgc){
        shelf = new Tile[r][c];
        this.board = board;
        this.pgc = pgc;
        tokenCG1 = null;
        tokenCG2 = null;

    }


    /**
     *
     * @param stream_tiles When the player draw the tiles from the deck the are setted into an array ;
     *                     The position of the tiles in the array is the order on which the player will insert the card on the column:
     *                     stream_tiles lenght must be more than 0 and less than 3

     * @param column The player must specifies which column he wants to put the tiles in
     *
     * @return The return statement has been thought for returning feedback codes:
     *          return 0 --> successfully insertion
     *          return -1 --> there isn't enough space in the column selected
     *          return -2 --> array stream tiles doesn't have an acceptable lenght
     *
     */
    public int putTiles(ArrayList<Tile> stream_tiles, int column) /*throws InvalidColumnException*/ {

        if(stream_tiles.size() > 3 || stream_tiles.size() <=0) return -2;
        else{
            //controllo a priori che ci sia spazio a sufficienza nelle colonna selezionata

            int count_col = 0;

            for(int i=0; i<r;i++){
                if(shelf[i][column] == null) count_col++;
            }

            if(count_col < stream_tiles.size()) return -1; //codice -1 rappresenta una colonna invalida

            int stream_tiles_pointer = 0;
            for(int i = 0; i<r; i++){
                if(shelf[i][column] == null){
                    shelf[i][column] = stream_tiles.get(stream_tiles_pointer);
                    stream_tiles_pointer++;
                    if(stream_tiles_pointer == stream_tiles.size()) i=r; //break
                }
            }

            return 0;
        }
    }


    /**
     * Feedback method, it shows the element of the matrix in order to render the whole matrix on the CLI
     */
    public void getBookshelf(){
        for(int i = r-1; i>=0; i--){
            System.out.println("\n");
            for(int j=0;j<c;j++){
                System.out.println(shelf[i][j]);

            }
        }
    }

    /**
     *
     * @return this method returns the number of max free spaces for all the columns of the shelf
     */

    public int maxShelfSpace(){
        int max=0; int count=0;
        for(int j=0;j<c;j++){
            for(int i=5; i>=0;i--){
                if(shelf[i][j] == null) count++;
                else break;
            }

            if(max < count) max = count;
            count = 0;
        }

        return max;

    }

    /**
     *
     * @return sum of all the points accumulated by the player during the game
     */
    public int getMyScore(){
        return getScoreGroup()+ getScorePGC()+getScoreCGC()+getScoreEOG();
    }


    /**
     * The aim of this method is to calculate the points scored by creating groups of adjacent tiles in the matrix
     * The data structures used are a list of queues and an additional matrix
     * The additional matrix is used to track which tiles in the matrix has been already visited during the research of groups
     * The queue saves all the groups found so it can be easy to count the size of it and so calculate the points dued to it
     *
     * Groups are found through a recursive function "recursive_checker()" which inspect all the adjacent tiles of the same type
     *
     * @return return statement returns the point scored by the creation of all the groups of the matrix
     */
    public int getScoreGroup(){

        ArrayList<Coda> groups= new ArrayList<Coda>();

        int points=0;

        boolean[][] shelf_checker = new boolean[r][c];

        //inizializzo la matrice a false
        for(int i = 0; i<r;i++){
            for(int j = 0; j<c;j++){
                shelf_checker[i][j] = false;
            }
        }


        for(int i=0;i<r;i++){
            for(int j=0; j<c; j++){
                if(shelf[i][j] != null && shelf_checker[i][j] == false){


                    Coda queue = new Coda();
                    queue.enqueue(new Coordinate(i,j));
                    shelf_checker[i][j] = true;
                    recursive_checker(queue.head(), shelf_checker,queue);

                    groups.add(queue);


                }
            }
        }

        for(int i=0; i<groups.size(); i++){
            if(groups.get(i).getCoda().size() == 3) points = points+2;
            if(groups.get(i).getCoda().size() == 4) points = points+3;
            if(groups.get(i).getCoda().size() == 5) points = points+5;
            if(groups.get(i).getCoda().size() >= 6) points = points+8;

        }

        return points;
    }

    /**
     * It's a supporting method for getScoreGroup() function.
     * It can visit adjacent tiles of the same type and add them to the queue, if the tile hasn't been already visited
     *
     * @param point point indicates the coordinates of the Tile that I want to start inspecting
     * @param shelf_checker shelf_checker is the addition boolean matrix which indicates if the tiles has been already visited
     * @param queue is the structure where the single groups is stored
     *              then the queue is stores into an array of queues which stores all the groups found
     */

    private void recursive_checker(Coordinate point,boolean[][] shelf_checker,Coda queue){


        //NORTH
        if(point.getX()+1 < r)
            if(shelf[point.getX()][point.getY()] == shelf[point.getX()+1][point.getY()] && shelf_checker[point.getX()+1][point.getY()] == false)
                if(shelf[point.getX()+1][point.getY()] != null){
                    shelf_checker[point.getX()+1][point.getY()]  = true;
                    queue.enqueue(new Coordinate(point.getX()+1, point.getY() ));
                    recursive_checker(queue.tail(),shelf_checker,queue);

                }

        //SOUTH
        if(point.getX()-1 >= 0)
            if(shelf[point.getX()][point.getY()] == shelf[point.getX()-1][point.getY()] && shelf_checker[point.getX()-1][point.getY()] == false)
                if(shelf[point.getX()-1][point.getY()] != null){
                    shelf_checker[point.getX()-1][point.getY()]  = true;
                    queue.enqueue(new Coordinate(point.getX()-1, point.getY() ));
                    recursive_checker(queue.tail(),shelf_checker,queue);

                }

        //EAST
        if(point.getY()+1 < c)
            if(shelf[point.getX()][point.getY()] == shelf[point.getX()][point.getY()+1] && shelf_checker[point.getX()][point.getY()+1] == false)
                if(shelf[point.getX()][point.getY()+1] != null){
                    shelf_checker[point.getX()][point.getY()+1]  = true;
                    queue.enqueue(new Coordinate(point.getX(), point.getY()+1));
                    recursive_checker(queue.tail(),shelf_checker,queue);

                }

        //WEST
        if(point.getY()-1 >= 0)
            if(shelf[point.getX()][point.getY()] == shelf[point.getX()][point.getY()-1] && shelf_checker[point.getX()][point.getY()-1] == false)
                if(shelf[point.getX()][point.getY()-1] != null){
                    shelf_checker[point.getX()][point.getY()-1]  = true;
                    queue.enqueue(new Coordinate(point.getX(), point.getY()-1 ));
                    recursive_checker(queue.tail(),shelf_checker,queue);

                }






    }

    /**
     * assign the tokenCG1 if it has been completed
     */



    public void checkCG1(){

        if(board.getCG1().checkGoal() == true)

            tokenCG1 = board.getCG1().getTopToken();


    }

    /**
     * assign the tokenCG2 if it has been completed
     */

    public void checkCG2(){

        if(board.getCG2().checkGoal() == true)

            tokenCG2 = board.getCG2().getTopToken();


    }

    /**
     *
     * @return return the points scored by completing common goal card goals
     */
    public int getScoreCGC()
        return tokenCG1.getScore()+tokenCG2().getScore();


    /**
     *
     * @return return the point scored by completing personal goal card goal
     */
    public int getScorePGC(){

        return pgc.getScore();

    }

    /**
     * It has a double aim:
     * 1. Indicates if someone has finished it's game (his/her matrix is all filled)
     * 2. Indicated if the "owner" of this bookshelf has finished by filling all the gaps in the matrix
     * ==> indicates is the game is still running or not
     * @return true if game is ended, false if game is not ended
     */

    public boolean checkEOG(){
        if(board.getEOG() == null) return true;
        else{
            boolean end = true;

            for(int i=0; i<r;i++)
                for(int j=0; j<c;j++)
                    if(shelf[i][j] == null) end = false;

            if(end == true)
                tokenEOG = board.getEOG();

            return end;
        }
    }

    /**
     * If the player is the first one finishing to fill the matrix so it has the EOG token it'll assign him 1 extra point
     */

    public int getScoreEOG(){
        if(tokenEOG != null)
            return 1;
        else return 0;
    }







}