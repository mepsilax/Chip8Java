package core.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;

/**
 * FrameBuffer, buffer of the frames, simple as yo.
 */
public class FrameBuffer {
   /**The reference of this FrameBuffer within OpenGL**/
   private int reference = -1;
   
   /**
    * Constructs a new {@link FrameBuffer}
    */
   public FrameBuffer(){
      reference = glGenFramebuffers();
   }//End constructor
   
   /**
    * Sets the target texture for this frame buffer
    * @param texture the target texture to set
    */
   public void setTexture(Texture2D texture){
      bind();
      texture.bind();
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texture.getWidth(), texture.getHeight(), 0, 
            GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.getRef(), 0);
      glEnable(GL_DEPTH_TEST);
      glDepthFunc(GL_LEQUAL);
      glDepthMask(true);
      texture.unbind();
      unbind();
   }//End method setTexture

   /**
    * Clears the colour buffer of this frame buffer
    */
   public void clear(){
      bind();
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      unbind();
   }//End method clear
   
   /**
    * Binds this frame buffer to the
    * OpenGL context
    */
   public void bind(){
      glBindFramebuffer(GL_FRAMEBUFFER, reference);
   }//End method bind
   
   /**
    * Unbinds this frame buffer from the
    * OpenGL context
    */
   public void unbind(){
      glBindFramebuffer(GL_FRAMEBUFFER, 0);
   }//End method unbind
}//End class FrameBuffer
