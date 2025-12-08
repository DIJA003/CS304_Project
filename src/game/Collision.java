package game;

public class Collision {
    public static boolean hit(Knight attacker, Knight victim) {
        int hitboxWidth = 25;
        int hitboxHeight = 90;

        return Math.abs(attacker.x - victim.x) < hitboxWidth &&
                Math.abs(attacker.y - victim.y) < hitboxHeight;
    }
}
