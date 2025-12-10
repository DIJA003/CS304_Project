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


    private AIMode mode;
    private int aggLevel = 50;
    private long lastDdg = 0;
    private long ddgCD = 3000;

    private int playerAtckCnt = 0;
    private long lastPlayerAtck = 0;
    private boolean isPlayerAgg = false;

    public AIController(Knight ai, Knight player, Difficulty diff) {
        this.ai = ai;
        this.player = player;
        this.diff = diff;
        applayDifficulty();
        setMode();
    }

    private void applayDifficulty(){
        switch(diff){
            case Easy:
                attackDelay = 900;
                moveSpd = 1;
                ai.hp = 100;
                ai.attack1Dmg = 8;
                ai.attack2Dmg = 12;
                ai.attack3Dmg = 16;
                ai.shieldHealth = 80;
                break;
            case Medium:
                attackDelay = 650;
                moveSpd = 2;
                ai.hp = 150;
                ai.attack1Dmg = 12;
                ai.attack2Dmg = 17;
                ai.attack3Dmg = 22;
                ai.shieldHealth = 100;
                break;
            case Hard:
                attackDelay = 450;
                moveSpd = 3;
                ai.hp = 200;
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

        adaptBehav(now, distance);

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

        if (mode == AIMode.Aggressive && distance > attackRange + 10) {
            if (diff > 0 && ai.x < 195 && ai.stamina > 20) {
                ai.x += moveSpd;
                ai.setState(AnimationState.Run);
            } else if (diff < 0 && ai.x > 5 && ai.stamina > 20) {
                ai.x -= moveSpd;
                ai.setState(AnimationState.Run);
            }
        } else if (mode == AIMode.Passive) {
            if (random.nextInt(100) < 30) {
                if (distance < 60 && ai.x > 5 && diff > 0) {
                    ai.x -= moveSpd;
                    ai.setState(AnimationState.Run);
                } else if (distance < 60 && ai.x < 195 && diff < 0) {
                    ai.x += moveSpd;
                    ai.setState(AnimationState.Run);
                }
            } else {
                normalMove(diff);
            }
        } else {
            if (random.nextInt(100) < 5) {
                if (random.nextBoolean() && ai.x > 5) {
                    ai.x -= moveSpd;
                } else if (ai.x < 195) {
                    ai.x += moveSpd;
                }
                ai.setState(AnimationState.Run);
                return;
            }
            normalMove(diff);
        }

        if (ai.canMove() &&
                ai.currState != AnimationState.Run &&
                !ai.isAttackState(ai.currState)) {
            ai.setState(AnimationState.Idle);
        }
    }

    private void normalMove(int diff) {
        if (diff > 0 && ai.x < 195 && ai.stamina > 10) {
            ai.x += moveSpd;
            ai.setState(AnimationState.Run);
        } else if (diff < 0 && ai.x > 5 && ai.stamina > 10) {
            ai.x -= moveSpd;
            ai.setState(AnimationState.Run);
        } else {
            ai.setState(AnimationState.Idle);
        }
    }

    private void adaptBehav(long now, int distance) {
        float hpRatio = ai.hp / (float) ai.maxHp;

        if(hpRatio < 0.3f){
            aggLevel = Math.max(20,aggLevel - 30);
        }else if(hpRatio > 0.7f && player.hp < ai.hp){
            aggLevel = Math.min(90, aggLevel + 20);
        }
        if (player.stamina < 30 && distance < 80) {
            aggLevel = Math.min(100, aggLevel + 30);
        }
    }

    private void performAttack() {
        int choice = random.nextInt(100);
        float hpRatio = ai.hp / (float) ai.maxHp;
        float staminaRatio = ai.stamina / ai.maxStamina;
        //float hpRatio = ai.hp / 100f;

        switch(diff) {
            case Easy:
                if (choice < 50) {
                    ai.attack1(player);
                } else if (choice < 80) {
                    ai.attack2(player);
                } else {
                    ai.attack3(player);
                }
                break;
            case Medium:
                if (hpRatio > 0.5f) {
                    if (choice < 35) {
                        ai.attack1(player);
                    } else if (choice < 70) {
                        ai.attack2(player);
                    } else if (staminaRatio > 0.4f) {
                        ai.attack3(player);
                    } else {
                        ai.attack1(player);
                    }
                } else {
                    if (choice < 50) {
                        ai.attack1(player);
                    } else if (choice < 80 && staminaRatio > 0.3f) {
                        ai.attack2(player);
                    } else if (staminaRatio > 0.5f) {
                        ai.attack3(player);
                    } else {
                        ai.attack1(player);
                    }
                }
                break;
            case Hard:
                if (isPlayerAgg) {
                    if (choice < 40) {
                        ai.attack3(player);
                    } else if (choice < 75) {
                        ai.attack2(player);
                    } else {
                        ai.attack1(player);
                    }
                } else if (player.stamina < 30) {
                    if (choice < 50 && staminaRatio > 0.5f) {
                        ai.attack3(player);
                    } else if (choice < 80) {
                        ai.attack2(player);
                    } else {
                        ai.attack1(player);
                    }
                } else {
                    if (choice < 30) {
                        ai.attack1(player);
                    } else if (choice < 65 && staminaRatio > 0.3f) {
                        ai.attack2(player);
                    } else if (staminaRatio > 0.5f) {
                        ai.attack3(player);
                    } else {
                        ai.attack1(player);
                    }
                }
                break;
        }
    }

    private void handleDefensiveBehavior(long now, int distance) {
        boolean playerIsAttacking = player.isAttackState(player.currState);
        int defenseChance = 60;

        switch(diff) {
            case Easy:
                defenseChance = 40;
                break;
            case Medium:
                defenseChance = 65;
                break;
            case Hard:
                defenseChance = 85;
                if (player.stamina > 60) defenseChance = 90;
                break;
        }

        if (playerIsAttacking && distance < 50 && ai.getShieldHealth() > 20 && ai.stamina > 25) {
            if (now - lastDef > defCD) {
                if (random.nextInt(100) < defenseChance) {
                    ai.startDefending();
                    shouldDef = true;

                    long defDuration = diff == Difficulty.Hard ? 1000 :
                            diff == Difficulty.Medium ? 800 : 600;
                    defDur = now + defDuration;
                    lastDef = now;
                    lastTry = now;
                }
            }
        }
        if (shouldDef && now > defDur) {
            ai.stopDefending();
            shouldDef = false;
        }
    }

    private void setMode() {
        switch(diff) {
            case Easy:
                mode = AIMode.Passive;
                break;
            case Medium:
                mode = AIMode.Balanced;
                break;
            case Hard:
                mode = AIMode.Aggressive;
                break;
        }
    }
}
