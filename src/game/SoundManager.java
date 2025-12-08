package game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, Clip> soundEffects = new HashMap<>();
    private static Clip musicClip = null;

    public static void loadSound(String name, String path){
        try{
            File file = new File(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            soundEffects.put(name,clip);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void playSSE(String name){
        Clip c = soundEffects.get(name);
        if(c == null) return;
        if(c.isRunning()) c.stop();
        c.setFramePosition(0);
        c.start();
    }

    public static void playMusic(String path){
        try{
            if(musicClip != null && musicClip.isRunning()) musicClip.stop();
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            musicClip = AudioSystem.getClip();
            musicClip.open(audio);

            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void stopMusic(){
        if(musicClip != null) musicClip.stop();
    }
}
