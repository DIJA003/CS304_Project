package game;

import game.Knight;

import java.util.Random;

public class AIController {

    Knight ai, player;
    int attackRange = 40, moveSpd = 1;

    long lastTry = 0;
    long attackDelay = 600;
    Random random = new Random();

    long lastDef = 0;
    long defCD = 2000;
    boolean shouldDef = false;
    long defDur = 0;

    public AIController(Knight ai, Knight player) {
        this.ai = ai;
        this.player = player;
    }

    public void update() {
            long now = System.currentTimeMillis();

            if (ai.isHurting || ai.currState == AnimationState.Dead) {
                ai.stopDefending();
                return;
            }

            if (player.currState == AnimationState.Dead) {
                ai.setState(AnimationState.Idle);
                ai.stopDefending();
                return;
            }

            int diff = player.x - ai.x;
            int distance = Math.abs(diff);

            handleDefensiveBehavior(now, distance);

            if (ai.isDefending()) {
                ai.facingRight = diff > 0;
                return;
            }

            if (distance <= attackRange) {
                ai.facingRight = diff > 0;

                if (now - lastTry > attackDelay) {
                    performAttack();
                    lastTry = now;

                    attackDelay = 400 + random.nextInt(400);
                }

                if (ai.currState == AnimationState.Attack1 ||
                        ai.currState == AnimationState.Attack2 ||
                        ai.currState == AnimationState.Attack3) {
                    return;
                }
            }
            else if (distance < attackRange + 20) {
                if (random.nextInt(100) < 5) {
                    if (random.nextBoolean() && ai.x > 5) {
                        ai.x -= moveSpd;
                    } else if (ai.x < 195) {
                        ai.x += moveSpd;
                    }
                    ai.setState(AnimationState.Run);
                } else {
                    ai.setState(AnimationState.Idle);
                }
                ai.facingRight = diff > 0;
            }
            else {
                if (diff > 0) {
                    ai.facingRight = true;
                    ai.x += moveSpd;
                    ai.setState(AnimationState.Run);
                } else if (diff < 0) {
                    ai.facingRight = false;
                    ai.x -= moveSpd;
                    ai.setState(AnimationState.Run);
                }
            }
            if (ai.canMove() &&
                    ai.currState != AnimationState.Run &&
                    ai.currState != AnimationState.Attack1 &&
                    ai.currState != AnimationState.Attack2 &&
                    ai.currState != AnimationState.Attack3) {
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
        boolean playerIsAttacking = (player.currState == AnimationState.Attack1 ||
                player.currState == AnimationState.Attack2 ||
                player.currState == AnimationState.Attack3);

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
