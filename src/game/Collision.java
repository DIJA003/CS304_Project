package game;

public class Collision {

    private static final int lightAtckWidth = 16;
    private static final int mediumAtckWidth = 17;
    private static final int heavyAtckWidth = 30;
    private static final int hitboxWidth = 50;

    private static final int atckRangeOffSet = 15;

    public static boolean hit(Knight attacker, Knight victim) {
        if (victim.currState == AnimationState.Dead) {
            return false;
        }
        int hitboxWidth = getHitboxWidth(attacker.currState);
        int attackX = attacker.facingRight ?
                attacker.x + atckRangeOffSet :
                attacker.x - atckRangeOffSet;

        int horizontalDist = Math.abs(attackX - victim.x);
        int verticalDist = Math.abs(attacker.y - victim.y);

        boolean correctDirection = attacker.facingRight ?
                (victim.x >= attacker.x) :
                (victim.x <= attacker.x);

        return correctDirection &&
                horizontalDist < hitboxWidth &&
                verticalDist < Collision.hitboxWidth;
    }

    private static int getHitboxWidth(AnimationState state) {
        switch (state) {
            case Attack2:
                return mediumAtckWidth;
            case Attack3:
                return heavyAtckWidth;
            default:
                return lightAtckWidth;
        }
    }
}