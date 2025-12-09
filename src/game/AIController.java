package game;

import game.Knight;

import java.util.Random;

public class AIController {

    Knight ai, player;
    Difficulty diff;
    int attackRange = 40, moveSpd = 1;

    long lastTry = 0;
    long attackDelay = 600;
    Random random = new Random();

    long lastDef = 0;
    long defCD = 2000;
    boolean shouldDef = false;
    long defDur = 0;

    public AIController(Knight ai, Knight player, Difficulty diff) {
        this.ai = ai;
        this.player = player;
        this.diff = diff;
        applayDifficulty();
    }

    private void applayDifficulty(){
        switch(diff){
            case Easy:
                attackDelay = 900;
                moveSpd = 1;
                ai.hp = 1;
                ai.attack1Dmg = 8;
                ai.attack2Dmg = 12;
                ai.attack3Dmg = 16;
                ai.shieldHealth = 80;
                break;
            case Medium:
                attackDelay = 650;
                moveSpd = 2;
                ai.hp = 1;
                ai.attack1Dmg = 12;
                ai.attack2Dmg = 17;
                ai.attack3Dmg = 22;
                ai.shieldHealth = 100;
                break;
            case Hard:
                attackDelay = 450;
                moveSpd = 3;
                ai.hp = 17;
                ai.attack1Dmg = 15;
                ai.attack2Dmg = 20;
                ai.attack3Dmg = 28;
                ai.shieldHealth = 120;
                break;
        }
    }

    public void update() {
        long now = System.currentTimeMillis();

        if (ai.currState == AnimationState.Dead) {
            ai.stopDefending();
            return;
        }
        if (player.currState == AnimationState.Dead) {
            ai.stopDefending();
            ai.setState(AnimationState.Idle);
            return;
        }

        int diff = player.x - ai.x;
        int distance = Math.abs(diff);
        ai.facingRight = (diff > 0);

        handleDefensiveBehavior(now, distance);

        if (ai.isDefending())
            return;

        if (distance <= attackRange) {

            if (now - lastTry > attackDelay) {
                performAttack();
                lastTry = now;
                attackDelay = 400 + random.nextInt(400);
                return;
            }
            if (ai.isAttackState(ai.currState))
                return;
            ai.setState(AnimationState.Idle);
            return;
        }

        if (random.nextInt(100) < 5) {
            if (random.nextBoolean() && ai.x > 5) {
                ai.x -= moveSpd;
            } else if (ai.x < 195) {
                ai.x += moveSpd;
            }
            ai.setState(AnimationState.Run);
            return;
        }

        if (diff > 0 && ai.x < 195) {
            ai.x += moveSpd;
            ai.setState(AnimationState.Run);
        } else if (diff < 0 && ai.x > 5) {
            ai.x -= moveSpd;
            ai.setState(AnimationState.Run);
        } else {
            ai.setState(AnimationState.Idle);
        }
        if (ai.canMove() &&
                ai.currState != AnimationState.Run &&
                !ai.isAttackState(ai.currState)) {
            ai.setState(AnimationState.Idle);
        }
    }
    private void performAttack() {
        int choice = random.nextInt(100);

        float hpRatio = ai.hp / 100f;

        if (hpRatio > 0.5f) {
            if (choice < 40) {
                ai.attack1(player);
            } else if (choice < 70) {
                ai.attack2(player);
            } else {
                ai.attack3(player);
            }
        } else {
            if (choice < 60) {
                ai.attack1(player);
            } else if (choice < 85) {
                ai.attack2(player);
            } else {
                ai.attack3(player);
            }
        }
    }

    private void handleDefensiveBehavior(long now, int distance) {
        boolean playerIsAttacking = player.isAttackState(player.currState);
        if (playerIsAttacking && distance < 50 && ai.getShieldHealth() > 20) {
            if (now - lastDef > defCD) {
                if (random.nextInt(100) < 60) {
                    ai.startDefending();
                    shouldDef = true;
                    defDur = now + 800;
                    lastTry = now;
                }
            }
        }

        if (shouldDef && now > defDur) {
            ai.stopDefending();
            shouldDef = false;
        }
    }
}
