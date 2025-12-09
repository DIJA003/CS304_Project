package game;

import textures.Texture.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.io.IOException;

public class VictoryBG{
    private int texId = 0;
    private TextureReader.Texture tex;

    public void load(GL gl, String path) {
        try {
            tex = TextureReader.readTexture(path, true);

            int[] texArr = new int[1];
            gl.glGenTextures(1, texArr, 0);
            texId = texArr[0];

            gl.glBindTexture(GL.GL_TEXTURE_2D, texId);
            new GLU().gluBuild2DMipmaps(
                    GL.GL_TEXTURE_2D,
                    GL.GL_RGBA,
                    tex.getWidth(),
                    tex.getHeight(),
                    GL.GL_RGBA,
                    GL.GL_UNSIGNED_BYTE,
                    tex.getPixels()
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void drawVictory(GL gl, float x, float y, float scale){
        float aspect = (float) tex.getWidth() / tex.getHeight();

        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texId);
        gl.glColor4f(1f, 1f, 1f, 1f);

        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glScalef(scale * aspect, scale, 1);

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex2f(-1, -1);
        gl.glTexCoord2f(1, 0); gl.glVertex2f(1, -1);
        gl.glTexCoord2f(1, 1); gl.glVertex2f(1, 1);
        gl.glTexCoord2f(0, 1); gl.glVertex2f(-1, 1);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }
}
