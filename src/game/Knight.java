package game;

import textures.Texture.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Knight {
    //constant
    public int hp = 100;
    public int animationSpd = 3;
    public long attackCD = 500;
    public long hurtDur = 300;
    public int knockBack = 5;

    //damage value
    public int attack1Dmg = 10;
    public int attack2Dmg = 15;
    public int attack3Dmg = 20;

    //position - state
    public int x, y;
    public boolean facingRight = true;
    public AnimationState currState = AnimationState.Idle;

    //animation data
    Map<AnimationState, int[]> animationFrames = new HashMap<>();
    Map<Integer, TextureReader.Texture> textures = new HashMap<>();
    int frameIndex = 0;
    int frameCnt = 0;


    //combat state
    public long nxtAttackTime = 0;
    public boolean isHurting = false;
    public long hurtEndTime = 0;

    //shield sys
    public boolean isDefend = false;
    public float shieldHealth = 100f;
    private final float shieldMaxHealth = 100f;
    private final float shieldRegen = 0.5f;
    private final float shieldDrain = 0.3f;
    private final float shieldDmgRed = 0.9f;
    private boolean isShieldBroken = false;
    private final float shieldUpPoint = 25f;

    private Difficulty currLevel = GameGLEventListener.getDiff();


    public Knight(int x, int y, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
    }

    public void setState(AnimationState newState) {
        if (currState != newState) {
            currState = newState;
            frameIndex = 0;
            frameCnt = 0;
        }
    }

    public void update(){
        long now = System.currentTimeMillis();

        if (isDefend && shieldHealth > 0) {
            shieldHealth = Math.max(0, shieldHealth - shieldDrain);
            if (shieldHealth <= 0) {
                shieldHealth = 0;
                isShieldBroken = true;
                stopDefending();
            }
        } else if (!isDefend && shieldHealth < shieldMaxHealth) {
            shieldHealth = Math.min(shieldMaxHealth, shieldHealth + shieldRegen);
            if (isShieldBroken && shieldHealth >= shieldUpPoint) {
                isShieldBroken = false;
            }
        }

        if (isHurting) {
            if (now >= hurtEndTime) {
                isHurting = false;
                setState(AnimationState.Idle);
            }
            updateAnimation();
            return;
        }

        if (currState == AnimationState.Dead) {
            int[] frames = animationFrames.get(currState);
            if (frames != null && frameIndex < frames.length - 1) {
                updateAnimation();
            }
            return;
        }

        if (currState == AnimationState.Defend) {
            updateAnimation();
            return;
        }


        if (isAttackState(currState)) {
            int[] frames = animationFrames.get(currState);
            if (frames != null && frameIndex >= frames.length - 1) {
                setState(AnimationState.Idle);
            }
        }

        updateAnimation();
    }
    public void updateAnimation() {
        int[] frames = animationFrames.get(currState);
        if (frames == null || frames.length == 0) return;

        frameCnt++;

        if (frameCnt % animationSpd == 0) {
            frameIndex = (frameIndex + 1) % frames.length;
        }
    }

    public void draw(GL gl, int maxWidth, int maxHeight) {

        int[] frames = animationFrames.get(currState);
        if (frames == null || frames.length == 0) return;

        if (frameIndex >= frames.length)
            frameIndex = 0;

        int tid = frames[frameIndex];
        TextureReader.Texture t = textures.get(tid);

        float nx = (2f * x / maxWidth) - 1f;
        float ny = (2f * y / maxHeight) - 1f;

        float aspect = (float) t.getWidth() / t.getHeight();

        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, tid);
        gl.glColor4f(1f, 1f, 1f, 1f);
        gl.glPushMatrix();
        gl.glTranslatef(nx, ny, 0);

        float baseScale;

        if (currState == AnimationState.Dead) {
            if (currLevel == Difficulty.Hard) {
                baseScale = 0.08f;
            } else {
                baseScale = 0.12f;
            }
        } else {
            if (currLevel == Difficulty.Hard) {
                baseScale = 0.30f;
            } else {
                baseScale = 0.22f;
            }
        }
        gl.glScalef(baseScale * aspect, baseScale, 1);

        if (!facingRight) gl.glScalef(-1, 1, 1);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0);
        gl.glVertex2f(-1, -1);
        gl.glTexCoord2f(1, 0);
        gl.glVertex2f(1, -1);
        gl.glTexCoord2f(1, 1);
        gl.glVertex2f(1, 1);
        gl.glTexCoord2f(0, 1);
        gl.glVertex2f(-1, 1);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    public boolean takeDamage(int dmg, Knight attacker) {
        if (currState == AnimationState.Dead) return false;

        boolean isFacingAttacker = (attacker.x < x && !facingRight) || (attacker.x > x && facingRight);

        if (isDefend && isFacingAttacker && shieldHealth > 0) {
//            float actualDamage = dmg * shieldDmgRed;
            //hp -= (int) actualDamage;
            shieldHealth = Math.max(0, shieldHealth - (dmg * 2));
            if(shieldHealth <= 0){
                shieldHealth = 0;
                isShieldBroken = true;
                stopDefending();
            }
//            System.out.println("blocked");

//            if (hp <= 0) {
//                hp = 0;
//                SoundManager.playSSE("death");
//                setState(AnimationState.Dead);
//                isDefend = false;
//            }

            return true;
        } else {

            if (isHurting) return false;
            hp -= dmg;

            if (hp <= 0) {
                hp = 0;
                SoundManager.playSSE("death");
                setState(AnimationState.Dead);
                isDefend = false;
                return false;
            }
            SoundManager.playSSE("hurt");
            isHurting = true;
            hurtEndTime = System.currentTimeMillis() + hurtDur;
            setState(AnimationState.Hurt);
            isDefend = false;

            if (x < attacker.x) {
                x -= knockBack;
            } else {
                x += knockBack;
            }


            facingRight = x < attacker.x;

            return false;
        }
    }

    public boolean isAttackState(AnimationState state) {
        return state == AnimationState.Attack1 ||
                state == AnimationState.Attack2 ||
                state == AnimationState.Attack3;
    }

    public boolean isDead() {
        return currState == AnimationState.Dead;
    }

    public boolean canMove() {
        return !isHurting && !isDead();
    }

    private boolean canAttack() {
        long now = System.currentTimeMillis();
        if (now < nxtAttackTime) return false;
        if (isHurting || currState == AnimationState.Dead) return false;
        if (isDefend) return false;

        return true;
    }

    private void executeAttack(AnimationState attackState, int damage, Knight target, String attackName) {
        long now = System.currentTimeMillis();

        setState(attackState);
        nxtAttackTime = now + attackCD;

        if (Collision.hit(this, target)) {
            boolean blocked = target.takeDamage(damage, this);
            if (blocked) {
                System.out.println(attackName + " BLOCKED!");
            } else {
                System.out.println(attackName + " hit damage: " + damage);
            }
        }
    }
    public void attack1(Knight target) {
        if (!canAttack()) return;
        SoundManager.playSSE("attack1");
        executeAttack(AnimationState.Attack1, attack1Dmg, target, "Light Attack");
    }
    public void attack2(Knight target) {
        if (!canAttack()) return;
        SoundManager.playSSE("attack2");
        executeAttack(AnimationState.Attack2, attack2Dmg, target, "Medium Attack");
    }
    public void attack3(Knight target) {
        if (!canAttack()) return;
        SoundManager.playSSE("attack3");
        executeAttack(AnimationState.Attack3, attack3Dmg, target, "Heavy Attack");
    }
    public float getShieldHealth() {
        return shieldHealth;
    }
    public float getShieldPercent() {
        return shieldHealth / shieldMaxHealth;
    }
    public boolean isDefending() {
        return isDefend && currState == AnimationState.Defend;
    }
    public void stopDefending() {
        if (currState == AnimationState.Defend) {
            isDefend = false;
            setState(AnimationState.Idle);
        }
    }
    public void startDefending() {
        if(isShieldBroken || shieldHealth <= 0) return;
        if (isHurting || currState == AnimationState.Dead) return;
        if (isAttackState(currState)) return;
        isDefend = true;
        setState(AnimationState.Defend);
    }
}

