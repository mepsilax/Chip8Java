package core;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Canvas;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import utility.ImageUtils;
import core.event.EventManager;
import core.event.WindowResizedEvent;
import core.graphics.Colour;
import core.graphics.SpriteBatch;
import core.graphics.Texture2D;
import core.input.InputManager;
import core.resource.ResourceManager;

public abstract class Game {
   protected SpriteBatch spriteBatch;
   private int fps = 60;
   private int currentFramesPerSecond;
   private static final boolean displayFPS = false;
   public GameContext context;

   public class GameContext {
      private SpriteBatch spriteBatch;
      private int fps;

      public SpriteBatch getSpriteBatch(){
         return spriteBatch;
      }//End method getSpriteBatch
      
      public int getFPS(){
         return fps;
      }//End method getFPS
   }//End class GameContext


   /**
    * Constructs a new {@link Game} in full screen mode.
    * @param vsync whether vertical sync should be enabled
    */
   public Game(boolean vsync) {
      try {
         Display.setFullscreen(true);
         Display.setVSyncEnabled(vsync);
         context = new GameContext();
      } catch(Exception exc) {
         exc.printStackTrace();
      }//End try/catch
   }//End constructor

   /**
    * Constructs a new {@link Game} in windowed mode.
    * @param name the name of the window.
    * @param width the width of the window.
    * @param height the height of the window.
    * @param resizable whether the window is resizable.
    */
   public Game(String name, int width, int height, boolean resizable) {
      Display.setTitle(name);
      try {
         Display.setDisplayMode(new DisplayMode(width, height));
         context = new GameContext();
      } catch(Exception exc) {
         exc.printStackTrace();
      }//End try/catch
      Display.setResizable(resizable);
   }//End constructor

   /**
    * Sets the target frames per second for this game.
    * @param fps the target fps to set.
    */
   public void setTargetFPS(int fps) {
      this.fps = fps;
      Display.setSwapInterval(fps);
   }//End method setTargetFPS

   /**
    * Gets the target frames per second for this game.
    * @return the target fps.
    */
   public int getTargetFPS() {
      return fps;
   }//End method getTargetFPS

   /**
    * Sets the render target for this game.
    * @param canvas the {@link Canvas} to render to.
    * @throws LWJGLException
    */
   public void setRenderTarget(Canvas canvas) throws LWJGLException{
      Display.setParent(canvas);
   }//End method setRenderTarget

   /**
    * Runs this game
    */
   public final void run() {
      run(new PixelFormat(), null);
   }//End method run

   /**
    * Runs this game.
    * @param format the {@link PixelFormat} to use.
    * @param attribs the {@link ContextAttribs} to use.
    */
   public final void run(PixelFormat format, ContextAttribs attribs) {
      try {
         Display.create(format, attribs);
         ByteBuffer[] list = new ByteBuffer[2];
         list[0] = ImageUtils.createBufferFromImage(ResourceManager.getResource(Texture2D.class, "resources/icon/default16.png").getBufferedImageFromData());
         list[1] = ImageUtils.createBufferFromImage(ResourceManager.getResource(Texture2D.class, "resources/icon/default32.png").getBufferedImageFromData());
         Display.setIcon(list);
         glClearColor(0f, 0f, 0f, 0f);
         spriteBatch = new SpriteBatch(160000, Display.getWidth(), Display.getHeight());
         context.spriteBatch = spriteBatch;
      } catch(Exception exc) {
         exc.printStackTrace();
         System.exit(1);
      }//End try/catch
      gameLoop();
   }//End method run

   /**
    * The main game loop, performs initialisation then enters
    * a loop that executes the update and render methods until
    * the game has requested to close
    */
   protected void gameLoop() {
      init();
      recalculateViewport();

      long lastTime, lastFPS;
      lastTime = lastFPS = System.nanoTime();
      currentFramesPerSecond = 0;

      while(!Display.isCloseRequested()) {
         long deltaTime = System.nanoTime() - lastTime;
         lastTime += deltaTime;

         if(Display.wasResized())
            recalculateViewport();
         EventManager.getEventManager().processEventQueue();
         InputManager.getInputManager().update();
         update(deltaTime / 1e9);

         render();
         Display.update();

         currentFramesPerSecond++;
         if(System.nanoTime() - lastFPS >= 1e9) {
            lastFPS += 1e9;
            if(displayFPS){
               System.out.println(currentFramesPerSecond);
            }
            context.fps = currentFramesPerSecond;
            currentFramesPerSecond = 0;
         }//End if
         Display.sync(fps);
      }//End while
      System.exit(0);
   }//End method gameLoop

   /**
    * Gets the width of the viewport in pixels
    * @return the width of the viewport in pixels
    */
   public int getWidth() {
      return Display.getWidth();
   }//End method getWidth

   /**
    * Gets the height of the viewport in pixels
    * @return the height of the viewport in pixels
    */
   public int getHeight() {
      return Display.getHeight();
   }//End method getHeight

   /**
    * Method to perform initialisation for the game
    */
   protected abstract void init();

   /**
    * Method to recalculate the viewport when the window has been resized
    */
   public void recalculateViewport() {
      glViewport(0, 0, getWidth(), getHeight());
      spriteBatch.recalculateViewport(getWidth(), getHeight());
      EventManager.getEventManager().fireEvent(new WindowResizedEvent(getWidth(), getHeight()));
   }//End method recalculateViewport

   /**
    * Method to perform updating
    * @param deltaTime
    */
   protected abstract void update(double deltaTime);

   /**
    * Method to perform rendering
    */
   protected void render(){};

   /**
    * Sets the clear colour for the OpenGL renderer
    * @param colour the clear {@link Colour} to set
    */
   public void setClearColour(Colour colour){
      glClearColor(colour.getR(), colour.getG(), colour.getB(), colour.getA());
   }//End method setClearColour

   /**
    * Destroys the display and exits the game
    */
   public void destroy() {
      Display.destroy();
      System.exit(0);
   }//End method destroy
}//End class Game