package game;

public class GameStats {
    private static long singlePlayerStartTime = 0;
    private static long singlePlayerEndTime = 0;
    private static boolean singlePlayerOn = false;

    private static int player1Round = 0;
    private static int player2Round = 0;
    private static int currRound = 1;
    private static final int roundsToWin = 2;

    private static int totalDmg = 0;
    private static int atckLand = 0;
    private static int perfectBlock = 0;

    public static void startSinglePLayer(){
        singlePlayerStartTime = System.currentTimeMillis();
        singlePlayerOn = true;
        singlePlayerEndTime = 0;
        resetRound();
    }

    public static void endSinglePlayer(){
        if(singlePlayerOn){
            singlePlayerEndTime = System.currentTimeMillis();
            singlePlayerOn = false;
        }
    }

    public static long getTime(){
        if(singlePlayerEndTime > 0 && singlePlayerStartTime > 0){
            return (singlePlayerEndTime - singlePlayerStartTime)/1000;
        }
        return 0;
    }

    public static String getFormatTime(){
        long seconds = getTime();
        long minutes = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d",minutes,sec);
    }

    public static void resetRound(){
        player1Round = 0;
        player2Round = 0;
        currRound = 1;
    }

    public static void player1WinsRound(){
        player1Round++;
        currRound++;
    }

    public static void player2WinsRound(){
        player2Round++;
        currRound++;
    }

    public static int getPlayer1Rounds() {
        return player1Round;
    }

    public static int getPlayer2Rounds() {
        return player2Round;
    }

    public static int getCurrentRound() {
        return currRound;
    }

    public static int getWinner() {
        if (player1Round >= roundsToWin) return 1;
        if (player2Round >= roundsToWin) return 2;
        return 0;
    }

    public static boolean isMatchOver() {
        return player1Round >= roundsToWin || player2Round >= roundsToWin;
    }
}
