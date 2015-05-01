package core.graphics;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

/**
 * A blend mode holds the settings and makes calls to {@link GL11#glBlendFunc(int sFactor, int dFactor)}
 */
public enum BlendMode {
   /**Default alpha blending.**/
   ALPHA_BLEND(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA),
   /**Additive alpha blending.**/
   ADDITIVE(GL_SRC_ALPHA, GL_ONE);
   
   /**The source factor to use for glBlendFunc**/
   int sourceFactor;
   /**The destination factor to use for glBlendFunc**/
   int destinationFactor;
   
   /**
    * Constructs a new {@link BlendMode}
    * @param sourceFactor the Source Factor to use for the blend function
    * @param destinationFactor the Destination Factor to use for the blend function
    */
   BlendMode(int sourceFactor, int destinationFactor){
      this.sourceFactor = sourceFactor;
      this.destinationFactor = destinationFactor;
   }//End constructor

   /**
    * Enables this blend mode within OpenGL
    */
   void enable(){
      glEnable(GL_BLEND);
      glBlendFunc(sourceFactor, destinationFactor);
   }//End method enable
   
}//End enum BlendMode
