package game;

import textures.AnimListener;
import textures.Texture.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

public class GameGLEventListener extends AnimListener {
    private Knight player1;
    private Knight player2;
    private AIController ai;
    private static Difficulty currLevel = Difficulty.Easy;
    private VictoryBG vic = new VictoryBG();
    private boolean gameOver = false;

    private GameMode gameMode;
    private GameMain parent;
    private BackGround bg;

    private final int maxWidth = 200;
    private final int maxHeight = 200;

    private int bgTexId = 0;
    private TextureReader.Texture bgTex;

    private boolean roundOver = false;
    private long roundEndTime = 0;
    private final long roundTransitionDelay = 3000;
    private String roundWinnerText = "";

    BitSet keyBits = new BitSet(256);

    public GameGLEventListener(GameMode mode, GameMain parent) {
        this.gameMode = mode;
        this.parent = parent;
        bg = new BackGround();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClearColor(1, 1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        player1 = new Knight(50, 25, true);
        player2 = new Knight(150, 25, false);

        if (gameMode == GameMode.SinglePlayer) {
            currLevel = Difficulty.Easy;
            ai = new AIController(player2, player1,currLevel);
            loadAllAnimations(gl, player2, currLevel.path);
        }
        else{
            loadAllAnimations(gl, player2, "src//assets//knight2");
        }

        loadAllAnimations(gl, player1, "src//assets//knight1");
        //loadAllAnimations(gl, player2, "src//assets//knight2");

        SoundManager.loadSound("attack1", "src//assets//sounds//attack1.wav");
        SoundManager.loadSound("attack2", "src//assets//sounds//attack2.wav");
        SoundManager.loadSound("attack3", "src//assets//sounds//attack3.wav");
        SoundManager.loadSound("hurt", "src//assets//sounds//hurt.wav");
        SoundManager.loadSound("death", "src//assets//sounds//death.wav");

        bg.loadBackGround(gl, "src//assets//environment//backGround.png");

        vic.load(gl,"src//assets//ui//winfinal.png");
    }

    private void loadAllAnimations(GL gl, Knight k, String basePath) {
        loadAnimation(gl, k, AnimationState.Idle, basePath + "//idle");
        loadAnimation(gl, k, AnimationState.Run, basePath + "//run");
        loadAnimation(gl, k, AnimationState.Attack1, basePath + "//attack1");
        loadAnimation(gl, k, AnimationState.Attack2, basePath + "//attack2");
        loadAnimation(gl, k, AnimationState.Attack3, basePath + "//attack3");
        loadAnimation(gl, k, AnimationState.Hurt, basePath + "//hurt");
        loadAnimation(gl, k, AnimationState.Dead, basePath + "//dead");
        loadAnimation(gl, k, AnimationState.Defend, basePath + "//shield");
    }

    public void loadAnimation(GL gl, Knight k, AnimationState state, String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((d, n) -> n.endsWith(".png"));

        if (files == null || files.length == 0) {
            System.out.println("Error loading folder: " + folderPath);
            return;
        }

        Arrays.sort(files);

        int[] ids = new int[files.length];
        gl.glGenTextures(files.length, ids, 0);

        for (int i = 0; i < files.length; i++) {
            try {
                TextureReader.Texture t =
                        TextureReader.readTexture(files[i].getPath(), true);

                k.textures.put(ids[i], t);

                gl.glBindTexture(GL.GL_TEXTURE_2D, ids[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D,
                        GL.GL_RGBA,
                        t.getWidth(), t.getHeight(),
                        GL.GL_RGBA,
                        GL.GL_UNSIGNED_BYTE,
                        t.getPixels()
                );

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        k.animationFrames.put(state, ids);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        backToMenu();

        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

//        bg.update();
        bg.drawBackGround(gl);

        if (gameMode == GameMode.SinglePlayer) {
            if (player2.isDead() && currLevel == Difficulty.Hard) {
                if (!gameOver) {
                    gameOver = true;
                    GameStats.endSinglePlayer();
                }
                vic.drawVictory(gl, 0f, 0.5f, 0.5f);
                //drawFinalStats(gl);
            }
            handlePlayer1Input();
            if (ai != null) {
                ai.update();
            }
            if (player2.isDead()) {
                spawnNextBot(gl);
            }
        }

        else {
            if (!roundOver && !GameStats.isMatchOver()) {
                handlePlayer1Input();
                handlePlayer2Input();

                if (player1.isDead()) {
                    endRound(2);
                } else if (player2.isDead()) {
                    endRound(1);
                }
            } else if (roundOver && !GameStats.isMatchOver()) {
                long now = System.currentTimeMillis();
                if (now - roundEndTime > roundTransitionDelay) {
                    startNextRound(gl);
                }
            }
            else{
                vic.drawVictory(gl, 0f, 0.5f, 0.5f);
            }
        }

        player1.update();
        player2.update();

        if (player2.isDead()) {
            player2.draw(gl, maxWidth, maxHeight);
            player1.draw(gl, maxWidth, maxHeight);
        } else {
            player1.draw(gl, maxWidth, maxHeight);
            player2.draw(gl, maxWidth, maxHeight);
        }

//        drawBars(gl, player1, maxWidth, maxHeight, true);
//        drawBars(gl, player2, maxWidth, maxHeight, false);

    }

    private void handlePlayer1Input() {
        boolean moving = false;

        if (player1.isHurting || player1.currState == AnimationState.Dead) {
            return;
        }

        if (isKeyPressed(KeyEvent.VK_SHIFT)) {
            player1.startDefending();
            return;
        } else {
            player1.stopDefending();
        }

        if (isKeyPressed(KeyEvent.VK_A) && player1.x > 0 && player1.canMove()) {
            player1.x--;
            player1.facingRight = false;
            player1.setState(AnimationState.Run);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_D) && player1.x < maxWidth - 10 && player1.canMove()) {
            player1.x++;
            player1.facingRight = true;
            player1.setState(AnimationState.Run);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_Q)) {
            player1.attack1(player2);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_W)) {
            player1.attack2(player2);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_E)) {
            player1.attack3(player2);
            moving = true;
        }

        if (!moving && !player1.isHurting && player1.canMove() && !player1.isAttackState(player1.currState)) {

            player1.setState(AnimationState.Idle);
        }
    }

    private void handlePlayer2Input() {
        boolean moving = false;

        if (player2.isHurting || player2.currState == AnimationState.Dead) {
            return;
        }

        if (isKeyPressed(KeyEvent.VK_K)) {
            player2.startDefending();
            return;
        } else {
            player2.stopDefending();
        }

        if (isKeyPressed(KeyEvent.VK_J) && player2.x > 0 && player2.canMove()) {
            player2.x--;
            player2.facingRight = false;
            player2.setState(AnimationState.Run);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_L) && player2.x < maxWidth - 10 && player2.canMove()) {
            player2.x++;
            player2.facingRight = true;
            player2.setState(AnimationState.Run);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_U)) {
            player2.attack1(player1);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_I)) {
            player2.attack2(player1);
            moving = true;
        }

        if (isKeyPressed(KeyEvent.VK_O)) {
            player2.attack3(player1);
            moving = true;
        }

        if (!moving && !player2.isHurting && player2.canMove() && !player2.isAttackState(player2.currState)) {

            player2.setState(AnimationState.Idle);
        }
    }

    public void backToMenu(){
        if(isKeyPressed(KeyEvent.VK_ESCAPE)){
            parent.returnToMenu();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        keyBits.set(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBits.clear(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public boolean isKeyPressed(int key) {
        return keyBits.get(key);
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
    }

    @Override
    public void displayChanged(GLAutoDrawable d, boolean modeChanged, boolean deviceChanged) {
    }


//    private void drawBars(GL gl, Knight k, int maxWidth, int maxHeight, boolean isPlayer) {
//        float healthPer = Math.max(0, k.hp) / 100f;
//        float shieldPer = k.getShieldPercent();
//
//        float xPos = isPlayer ? -0.95f : 0.55f;
//        float yPos = 0.9f;
//        float barWidth = 0.35f;
//        float barHeight = 0.04f;
//
//        gl.glDisable(GL.GL_TEXTURE_2D);
//
//        String label = isPlayer ? "P1" : (gameMode == GameMode.SinglePlayer ? "AI" : "P2");
//
//        gl.glColor3f(0.2f, 0.2f, 0.2f);
//        gl.glBegin(GL.GL_QUADS);
//        gl.glVertex2f(xPos, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth, yPos);
//        gl.glVertex2f(xPos, yPos);
//        gl.glEnd();
//
//        if (healthPer > 0.6f) {
//            gl.glColor3f(0, 1, 0);
//        } else if (healthPer > 0.3f) {
//            gl.glColor3f(1, 1, 0);
//        } else {
//            gl.glColor3f(1, 0, 0);
//        }
//
//        gl.glBegin(GL.GL_QUADS);
//        gl.glVertex2f(xPos, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth * healthPer, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth * healthPer, yPos);
//        gl.glVertex2f(xPos, yPos);
//        gl.glEnd();
//
//        gl.glColor3f(1, 1, 1);
//        gl.glLineWidth(2.0f);
//        gl.glBegin(GL.GL_LINE_LOOP);
//        gl.glVertex2f(xPos, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth, yPos - barHeight);
//        gl.glVertex2f(xPos + barWidth, yPos);
//        gl.glVertex2f(xPos, yPos);
//        gl.glEnd();
//
//        float shieldYPos = yPos - barHeight - 0.05f;
//
//        gl.glColor3f(0.1f, 0.1f, 0.2f);
//        gl.glBegin(GL.GL_QUADS);
//        gl.glVertex2f(xPos, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth, shieldYPos);
//        gl.glVertex2f(xPos, shieldYPos);
//        gl.glEnd();
//
//        if (k.isDefending()) {
//            float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() * 0.01);
//            gl.glColor3f(0, 0.5f + pulse * 0.5f, 1);
//        } else {
//            gl.glColor3f(0, 0.5f, 1);
//        }
//
//        gl.glBegin(GL.GL_QUADS);
//        gl.glVertex2f(xPos, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth * shieldPer, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth * shieldPer, shieldYPos);
//        gl.glVertex2f(xPos, shieldYPos);
//        gl.glEnd();
//
//        gl.glColor3f(0.5f, 0.7f, 1);
//        gl.glLineWidth(1.5f);
//        gl.glBegin(GL.GL_LINE_LOOP);
//        gl.glVertex2f(xPos, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth, shieldYPos - barHeight * 0.7f);
//        gl.glVertex2f(xPos + barWidth, shieldYPos);
//        gl.glVertex2f(xPos, shieldYPos);
//        gl.glEnd();
//        gl.glLineWidth(1.0f);
//
//        gl.glEnable(GL.GL_TEXTURE_2D);
//    }


    public static Difficulty getDiff(){
        return currLevel;
    }
    private void spawnNextBot(GL gl) {
        if (currLevel == Difficulty.Easy) {
            currLevel = Difficulty.Medium;
            SoundManager.stopMusic();
            SoundManager.playMusic("src//assets//sounds//Combat.wav");
            player1.hp = 110;
            player1.attack1Dmg = 11;
            player1.attack2Dmg = 16;
            player1.attack3Dmg = 25;
            player2 = new Knight(160, 25, false);
        } else if (currLevel == Difficulty.Medium) {
            currLevel = Difficulty.Hard;
            SoundManager.stopMusic();
            SoundManager.playMusic("src//assets//sounds//SamuraiMusic.wav");
            player1.hp = 115;
            player1.attack1Dmg = 13;
            player1.attack2Dmg = 17;
            player1.attack3Dmg = 30;
            player2 = new Knight(160, 35, false);
        } else {
            return;
        }

        loadAllAnimations(gl, player2, currLevel.path);

        ai = new AIController(player2, player1, currLevel);
    }

    private void endRound(int winner) {
        if (roundOver) return;

        roundOver = true;
        roundEndTime = System.currentTimeMillis();

        if (winner == 1) {
            GameStats.player1WinsRound();
            roundWinnerText = "PLAYER 1 WINS ROUND " + (GameStats.getCurrentRound() - 1);
        } else {
            GameStats.player2WinsRound();
            roundWinnerText = "PLAYER 2 WINS ROUND " + (GameStats.getCurrentRound() - 1);
        }
    }
    private void startNextRound(GL gl) {
        roundOver = false;

        player1.hp = 100;
        player1.maxHp = 100;
        player1.stamina = player1.maxStamina;
        player1.shieldHealth = 100;
        player1.x = 50;
        player1.y = 25;
        player1.facingRight = true;
        player1.currState = AnimationState.Idle;
        player1.isHurting = false;

        player2.hp = 100;
        player2.maxHp = 100;
        player2.stamina = player2.maxStamina;
        player2.shieldHealth = 100;
        player2.x = 150;
        player2.y = 25;
        player2.facingRight = false;
        player2.currState = AnimationState.Idle;
        player2.isHurting = false;
    }
}