package game;

import javax.sound.sampled.*;
import java.io.File;

public class Sound {
    public static void play(String path){
        try{
            File file = new File(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
