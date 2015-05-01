package core.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;

import core.resource.ResourceManager;

/**
 * The SpriteBatch is responsible for generating vertex data through
 * draw calls and storing them in a buffer to be sent over to the GPU for processing.
 * Drawing is done per texture, the sprite batch will continue to collect vertex information
 * until either a draw call is made using a different texture to the last one that was used ,the
 * maximum number of sprites allowed in the buffer has been met, or the end method is called
 * the sprite batch will then make OpenGL calls to transfer the contents of the vertex buffer to the
 * GPU and draw them.
 */
public class SpriteBatch {
   /**Constant defining the number of vertices needed to draw a sprite**/
   private static final int VERTICES_PER_SPRITE = 6;
   /**The default vertex shader to use for rendering**/
   public static final String DEFAULT_VERTEX = "resources/shader/default.vert";
   /**The default fragment shader to use for rendering**/
   public static final String DEFAULT_FRAGMENT = "resources/shader/default.frag";
   /**The default {@link VertexAttribute}s for a sprite, 3 for position(x,y,z),
    * 4 for colour(r,b,g,a), 2 for texture coordinates (u, v)*/
   private static final List<VertexAttribute> DEFAULT_ATTRIBUTE =
         Arrays.asList(new VertexAttribute[]{
               new VertexAttribute(0, "Position", 3),
               new VertexAttribute(1, "Colour", 4),
               new VertexAttribute(2, "TexCoord", 2)
         });

   /**the default {@link ShaderProgram}**/
   private ShaderProgram defaultShaderProgram;
   /**The shader program currently in use**/
   private ShaderProgram program;
   /**The {@link VertexBuffer} for storing vertex information**/
   private VertexBuffer vertexBuffer;
   /**Flag marking whether this sprite batch is ready to receive draw calls**/
   private boolean ready = false;
   /**The {@link Texture2D} this sprite batch is currently drawing*/
   private Texture2D currentTexture;
   /**The maximum number of sprites this spritebatch can hold before flushing**/
   private int maxSprites;
   /**The current number of sprites being buffered**/
   private int spriteCount;
   //Variables for buffer rendering
   /**Render target frame buffer for rendering to texture**/
   private FrameBuffer renderTargetBuffer;
   /**If true, render to texture mode is switched on and the render target buffer should be used**/
   private boolean renderToFrameBuffer;
   /**The projection matrix used for rendering**/
   private Matrix4f projection;
   /**The current camera for checking if an object should be culled**/
   private Camera camera;
   /**The current translation matrix.**/
   private Matrix4f currentTranslation;
   /**The colour to use for rendering, defaults to white.*/
   private Colour colour = Colour.White;
   

   /**
    * Constructs a new {@link SpriteBatch}
    * @param bufferSize the number of sprites this spritebatch can hold before
    *  flushing
    */
   public SpriteBatch(int bufferSize, int width, int height){
      glEnable(GL_DEPTH_TEST);
      glDepthFunc(GL_LEQUAL);
      defaultShaderProgram = new ShaderProgram(DEFAULT_VERTEX, DEFAULT_FRAGMENT, DEFAULT_ATTRIBUTE);
      program = defaultShaderProgram;
      vertexBuffer = new VertexBuffer(bufferSize , DEFAULT_ATTRIBUTE);
      this.maxSprites = bufferSize / vertexBuffer.getComponentCount();
      recalculateViewport(width, height);
   }//End constructor

   /**
    * Starts the {@link SpriteBatch} with a default alpha blend
    */
   public void begin(){
      begin(new Matrix4f());
   }//End method begin
   
   /**
    * Starts this {@link SpriteBatch}.
    * @param camera the camera to use for object culling and transformation.
    */
   public void begin(Camera camera){
      this.camera = camera;
      begin(camera.createTranslationMatrix());
   }//End method begin
   
   /**
    * Starts this {@link SpriteBatch} with the given translation {@link Matrix4f}.
    */
   public void begin(Matrix4f translation){
      begin(BlendMode.ALPHA_BLEND);
      this.currentTranslation = translation;
      setTranslationMatrixForShader(translation);
   }//End method begin

   private void setTranslationMatrixForShader(Matrix4f translation) {
      program.begin();
      Matrix4f view = new Matrix4f();
      //Holder matrixes
      Matrix4f projectionView = new Matrix4f();
      Matrix4f projectionTransposition = new Matrix4f();
      //Transpose the projection matrix for OpenGL.
      Matrix4f.transpose(projection, projectionTransposition);
      //Multiply the view and the projection matrix
      projectionView = Matrix4f.mul(view, projectionTransposition, projectionView);
      //Get the float buffer of the matrix
      FloatBuffer buffer = BufferUtils.createFloatBuffer(20);
      projectionView.store(buffer);
      buffer.flip();
      //Store the matrix in the vertex shader
      glUniformMatrix4(program.getUniform("CameraMatrix"), false, buffer);
      setTranslationMatrix(translation);
      program.end();
   }
   
   /**
    * Method to set the translation matrix in the {@link ShaderProgram} to the given matrix.
    * @param translation the translation matrix to set in the {@link ShaderProgram}.
    */
   private void setTranslationMatrix(Matrix4f translation){
      FloatBuffer buffer = BufferUtils.createFloatBuffer(20);
      translation.store(buffer);
      buffer.flip();
      glUniformMatrix4(program.getUniform("TransformMatrix"), false, buffer);
   }//End method setTranslationMatrix
   
   /**
    * Starts this {@link SpriteBatch}
    * @param blendMode the {@link BlendMode} to use.
    */
   public void begin(BlendMode blendMode){
      if(ready){
         throw new RuntimeException("end() must be called before begin");
      }//End if
      blendMode.enable();
      if(renderToFrameBuffer){
         renderTargetBuffer.clear();
         renderTargetBuffer.bind();
      }//End if
      ready = true;
      spriteCount = 0;
   }//End method begin

   /**
    * Sets the colour to clear the buffer with when
    * {@link SpriteBatch#clear} is called.
    * @param colour the {@link Colour} to set the clear buffer.
    */
   public void setClearColour(Colour colour){
	   glClearColor(colour.r, colour.g, colour.b, colour.a);
   }//End method setClearColour

   /**
    * Clears the GL_COLOR_BUFFER_BIT
    */
   public void clear(){
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
   }//End method clear

   private void calculateProjectionViewMatrix(boolean forFrameBuffer, int width, int height) {
      projection = createOrthographic2DMatrix(0f, 0f, width, height, forFrameBuffer);
   }//End method calculate projection view matrix
   
   /**
    * Sets this {@link SpriteBatch} to draw to the specified {@link Texture2D} instead
    * of the frame buffer.
    * @param texture the non-null texture to render to.
    * @param int bufferWidth the desired width of the buffer.
    * @param int bufferHeight the desired height of the buffer.
    */
   public void changeRenderTarget(Texture2D texture, int bufferWidth, int bufferHeight){
      if(ready){
         throw new RuntimeException("The render target for SpriteBatch must be set before any calls to SpriteBatch.begin()");
      } else if (texture == null){
         throw new NullPointerException("texture must be a non-null Texture2D");
      }//End if
      if(renderTargetBuffer == null){
         renderTargetBuffer = new FrameBuffer();
      }//End if
      renderToFrameBuffer = true;
      texture.setHeight(bufferHeight);
      texture.setWidth(bufferWidth);
      renderTargetBuffer.setTexture(texture);
      //The projection matrix needs to be flipped if rendering to a frame buffer
      //thanks to OpenGLs fantastic coordinate system.
      calculateProjectionViewMatrix(true, texture.getWidth(), texture.getHeight());
   }//End method changeRenderTarget
   
   /**
    * Sets this {@link SpriteBatch} to draw to the specified {@link Texture2D}.
    * Sets a default buffer width and height of the current {@link Display} resolution.
    * @param texture the {@link Texture2D} to render to.
    */
   public void changeRenderTarget(Texture2D texture){
      changeRenderTarget(texture, Display.getWidth(), Display.getHeight());
   }//End method changeRenderTarget
   
   /**
    * Sets the render target of this sprite batch to the default frame buffer.
    */
   public void defaultRenderTarget(){
      if(ready){
         throw new RuntimeException("The render target for SpriteBatch must be set before any calls to SpriteBatch.begin()");
      }//End if
      renderToFrameBuffer = false;
      calculateProjectionViewMatrix(false, Display.getWidth(), Display.getHeight());
   }//End method defaultRenderTarget

   /**
    * Creates the orthographic 2d matrix for getting the vertices on screen correctly within
    * the shader
    * @param x the x position of the matrix
    * @param y the y position of the matrix
    * @param width the width of the matrix
    * @param height the height of the matrix
    * @param forFrameBuffer if drawing to a frame buffer, this will flip the top and bottom of the matrix
    * @return a {@link Matrix4f} containing the Orthographic Matrix.
    */
   public Matrix4f createOrthographic2DMatrix(float x, float y, float width, float height, boolean forFrameBuffer) {
      if(forFrameBuffer){
         return createOrthographicMatrix(x, x + width, y, y  + height, -1f, 1f);
      }//End if
      return createOrthographicMatrix(x, x + width, y + height, y, -1f, 1f);
   }//End method createOrthographic2DMatrix

   public Matrix4f createOrthographicMatrix(float left, float right, float bottom, float top, float near, float far){
      float x = 2 / (right - left);
      float y = 2 / (top - bottom);
      float z = -2 / (far - near);
      float tx = -(right + left) / (right - left);
      float ty = -(top + bottom) / (top - bottom);
      float tz = -(far + near) / (far - near);

      Matrix4f m = new Matrix4f();
      m.m00 = x;
      m.m10 = 0;
      m.m20 = 0;
      m.m30 = 0;
      m.m01 = 0;
      m.m11 = y;
      m.m21 = 0;
      m.m31 = 0;
      m.m02 = 0;
      m.m12 = 0;
      m.m22 = z;
      m.m32 = 0;
      m.m03 = tx;
      m.m13 = ty;
      m.m23 = tz;
      m.m33 = 1;
      return m;
   }//End method createOrthographicMatrix

   /**
    * Draws the given texture at the specified coordinates at the full height and width
    * @param texture the texture to draw.
    * @param x the x position to draw the texture at
    * @param y the y position to draw the texture at
    */
   public void draw(Texture2D texture, float x, float y){
      draw(texture, x, y, texture.getWidth(), texture.getHeight(), 0, 0, 1f, 1f, 0f, 0f, RotationOrigin.TOP_LEFT);
   }//End method draw

   public void draw(Texture2D texture, float x, float y, float depth){
      draw(texture, x, y, texture.getWidth(), texture.getHeight(), 0, 0, 1f, 1f, depth, 0f, RotationOrigin.TOP_LEFT);
   }//End method draw

//   public void draw(Texture2D texture, float x, float y, float depth, float rotation){
//      draw(texture, x, y, texture.getWidth(), texture.getHeight(), 0, 0, 1f, 1f, rotation, depth, RotationOrigin.CENTER);
//   }//End method draw

   public void draw(Texture2D texture, float x, float y, float width, float height){
      draw(texture, x, y, width, height, 0, 0, 1f, 1f, 0f, 0f, RotationOrigin.TOP_LEFT);
   }
   
   public void draw(Texture2D texture, float x, float y, float width, float height, float rotation, RotationOrigin origin){
      draw(texture, x, y, width, height, 0, 0, 1f, 1f, rotation, 0f, origin);
   }

   public void draw(Texture2D texture, float x, float y, float width, float height, float depth){
      draw(texture, x, y, width, height, 0, 0, 1f, 1f, depth, 0f, RotationOrigin.TOP_LEFT);
   }
   public void draw(Texture2D texture, float x, float y, float width, float height, float depth, float rotation){
      draw(texture, x, y, width, height, 0, 0, 1f, 1f, rotation, depth, RotationOrigin.CENTER);
   }
   
   public void draw(Texture2D texture, float x, float y, float width, float height, float depth, float rotation, RotationOrigin origin){
      draw(texture, x, y, width, height, 0, 0, 1f, 1f, rotation, depth, origin);
   }

   public void draw(Texture2D texture, float x, float y, float width, float height,
         float sourceX, float sourceY, float sourceWidth, float sourceHeight, float depth, float rotation){
      float u = sourceX / (float)texture.getWidth();
      float v = sourceY / (float)texture.getHeight();
      float u2 = (sourceX + sourceWidth) / (float)texture.getWidth();
      float v2 = (sourceY + sourceHeight) / (float)texture.getHeight();
      draw(texture, x, y, width, height, u, v, u2, v2, depth, rotation, RotationOrigin.CENTER);
   }

   public void draw(Texture2D texture, float x, float y, float width, float height,
         float sourceX, float sourceY, float depth, float rotation){
      float u = (float)sourceX / texture.getWidth();
      float v = (float)sourceY / texture.getHeight();
      float u2 = (float)(sourceX + width) / texture.getWidth();
      float v2 = (float)(sourceY + height) / texture.getHeight();
      draw(texture, x, y, width, height, u, v, u2, v2, depth, rotation, RotationOrigin.CENTER);
   }

   public void draw(Texture2D texture, float x, float y, float width, float height,
         float sourceX, float sourceY, float depth){
      float u = (float)sourceX / texture.getWidth();
      float v = (float)sourceY / texture.getHeight();
      float u2 = (float)(sourceX + width) / texture.getWidth();
      float v2 = (float)(sourceY + height) / texture.getHeight();
      draw(texture, x, y, width, height, u, v, u2, v2, depth, 0, RotationOrigin.TOP_LEFT);
   }
   public void draw(Texture2D texture, float x, float y, float width, float height,
         float sourceX, float sourceY, float sourceWidth, float sourceHeight, float rotation, RotationOrigin origin){
      float u = (float)sourceX / texture.getWidth();
      float v = (float)sourceY / texture.getHeight();
      float u2 = (float)(sourceX + sourceWidth) / texture.getWidth();
      float v2 = (float)(sourceY + sourceHeight) / texture.getHeight();
      draw(texture, x, y, width, height, u, v, u2, v2, rotation, 0, origin);
   }

   public void draw(Texture2D texture, float x, float y, float width, float height,
         float u, float v, float u2, float v2, float rotation, float depth, RotationOrigin origin){
      if(!ready){
         throw new RuntimeException("begin() must be called before any drawing can be performed");
      }//End if
      if(texture == null){
         throw new RuntimeException("The texture must be non-null");
      }//End if
      
      if(currentTexture != null && texture != currentTexture || spriteCount >= maxSprites ){
         render();
      }//End if
      currentTexture = texture;
      /**Top left**/
      float x1,y1 ;
      /**Top right**/
      float x2,y2;
      /**Bottom Left**/
      float x3,y3;
      /**Bottom Right**/
      float x4,y4;

      if (rotation != 0) {
         float originX = 0;
         float originY = 0;
         if(origin.equals(RotationOrigin.CENTER)){
            originX =  width / 2;
            originY =  height / 2 ;
         } else {
            originX = origin.getX();
            originY = origin.getY();
         }
         float p1x = -originX;
         float p1y = -originY;
         float p2x = width - originX;
         float p2y = -originY;
         float p3x = width - originX;
         float p3y = height - originY;
         float p4x = -originX;
         float p4y = height - originY;
         final float cos = (float) Math.cos(rotation);
         final float sin = (float) Math.sin(rotation);

         x1 = x + (cos * p1x - sin * p1y) + originX; // TOP LEFT
         y1 = y + (sin * p1x + cos * p1y) + originY;

         x2 = x + (cos * p2x - sin * p2y) + originX; // TOP RIGHT
         y2 = y + (sin * p2x + cos * p2y) + originY;

         x3 = x + (cos * p3x - sin * p3y) + originX; // BOTTOM RIGHT
         y3 = y + (sin * p3x + cos * p3y) + originY;

         x4 = x + (cos * p4x - sin * p4y) + originX; // BOTTOM LEFT
         y4 = y + (sin * p4x + cos * p4y) + originY;
      } else {
         x1 = x;
         y1 = y;
         x2 = x+width;
         y2 = y;

         x3 = x+width;
         y3 = y+height;

         x4 = x;
         y4 = y+height;
      }//END IF
      
      float r = colour.getR();
      float g = colour.getG();
      float b = colour.getB();
      float a = colour.getA();
      
      if(camera == null || camera.contains(new Rectangle((int)x1, (int)y1, (int)(x3 - x1), (int)(y3 - y1)))){
         //Create 6 vertices of, X,Y,Z, R,G,B,A, U,V to create 2 triangles forming a quad
         vertexBuffer.add(x1).add(y1).add(depth).add(r).add(g).add(b).add(a).add(u).add(v);
         vertexBuffer.add(x2).add(y2).add(depth).add(r).add(g).add(b).add(a).add(u2).add(v);
         vertexBuffer.add(x3).add(y3).add(depth).add(r).add(g).add(b).add(a).add(u2).add(v2);
         vertexBuffer.add(x3).add(y3).add(depth).add(r).add(g).add(b).add(a).add(u2).add(v2);
         vertexBuffer.add(x4).add(y4).add(depth).add(r).add(g).add(b).add(a).add(u).add(v2);
         vertexBuffer.add(x1).add(y1).add(depth).add(r).add(g).add(b).add(a).add(u).add(v);
         spriteCount++;
      }//End if
   }//End method draw

   /**
    * Draws a line from the given start position to end position.
    * @param x1 the x start position of the line.
    * @param y1 the y start position of the line.
    * @param x2 the x end position of the line.
    * @param y2 the y end position of the line.
    * @param width the width of the line.
    */
   public void drawLine(int x1, int y1, int x2, int y2, float width){
      int dx = (x2-x1);
      int dy = (int)y2-y1;
      float dist = (float)Math.sqrt(dx*dx + dy*dy);
      float rad = (float)Math.atan2(dy, dx);
      draw(ResourceManager.getBlankTexture(), x1, y1, (int)dist, (int)width, 0, rad, RotationOrigin.TOP_LEFT);
   }//End method drawLine
   
   /**
    * Draws text using a given {@link SpriteFont}
    * @param x the x coordinate to draw the text.
    * @param y the y coordinate to draw the text.
    * @param text the text to draw.
    * @param font the {@link SpriteFont} to use for drawing.
    */
   public void drawText(int x, int y, String text, SpriteFont font){
      font.drawTextString(x, y, text, this);
   }//End method drawText
   
   
   /**
    * Ends this sprite batch and flushes any buffered vertices to the GPU
    */
   public void end(){
      if(!ready){
         System.err.println("begin() must be called before end");
         throw new RuntimeException("begin() must be called before end");
      }//End if
      render();
      setColour(Colour.White);
      currentTexture = null;
      camera = null;
      ready = false;
      program = defaultShaderProgram;
   }//End method end


   /**
    * Sets the viewport size this spritebatch should use
    * and recalculates the orthographic matrix
    * @param width
    * @param height
    */
   public void recalculateViewport(int width, int height){
      calculateProjectionViewMatrix(renderToFrameBuffer, width, height);
   }//End method recalculateViewPort

   /**
    * flushes the contents of the vertex buffer to the GPU and renders
    */
   private void render(){
      if(renderToFrameBuffer){
         renderTargetBuffer.bind();
      }//End if
      program.begin();
      if(currentTexture != null){
         currentTexture.bind();
         glUniform1i(program.getUniform("texture"), 0);
         vertexBuffer.flip();
         vertexBuffer.draw(GL_TRIANGLES, 0, spriteCount * VERTICES_PER_SPRITE);
         vertexBuffer.clear();
         spriteCount = 0;
         program.end();
         currentTexture.unbind();
      }//End if
      if(renderToFrameBuffer){
         renderTargetBuffer.unbind();
      }//End if

   }//End method render
   
   public void setShaderProgram(ShaderProgram program){
      render();
      this.program = program;
      setTranslationMatrixForShader(currentTranslation);
   }
   
   public void restoreDefaultShaderProgram(){
      render();
      program = defaultShaderProgram;
   }
   
   /**
    * Sets the {@link Colour} this {@link SpriteBatch} should use for rendering.
    * @param colour the colour to set for the {@link SpriteBatch}.
    */
   public void setColour(Colour colour){
      this.colour = colour;
   }//End method colour
}//End class SpriteBatch
