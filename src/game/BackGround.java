package game;

import textures.Texture.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.io.IOException;

public class BackGround {
    private int bgTexId = 0;
    private TextureReader.Texture bgTex;
//    private float scroll = 0f;
//    private float scrollSpd = 0.0005f;

    public void loadBackGround(GL gl, String path){
        try {
            bgTex = TextureReader.readTexture(path, true);

            int[] tex = new int[1];
            gl.glGenTextures(1, tex, 0);
            bgTexId = tex[0];

            gl.glBindTexture(GL.GL_TEXTURE_2D, bgTexId);
            new GLU().gluBuild2DMipmaps(
                    GL.GL_TEXTURE_2D,
                    GL.GL_RGBA,
                    bgTex.getWidth(), bgTex.getHeight(),
                    GL.GL_RGBA,
                    GL.GL_UNSIGNED_BYTE,
                    bgTex.getPixels()
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void update(){
//        scroll+=scrollSpd;
//        if(scroll >= 1f) scroll-=1f;
//        if(scroll <= -1f) scroll+=1f;
//    }
    public void drawBackGround(GL gl){
        gl.glBindTexture(GL.GL_TEXTURE_2D, bgTexId);

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
    }


}
