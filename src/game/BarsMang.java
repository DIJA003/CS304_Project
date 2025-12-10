package game;

import textures.Texture.TextureReader;

import javax.media.opengl.GL;
import java.io.IOException;

public class BarsMang {
    private int id = 0;
    private TextureReader.Texture tex;

    public BarsMang(GL gl){
        try{
            tex = TextureReader.readTexture("src//assets//ui//PlayerBars.png", true);
            gl.glGenTextures(1, new int[]{id = 0}, 0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, id);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA,
                    tex.getWidth(), tex.getHeight(), 0,
                    GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, tex.getPixels());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
