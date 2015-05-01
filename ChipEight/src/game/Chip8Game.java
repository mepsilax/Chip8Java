package game;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.lwjgl.opengl.Display;

import ui.EmulatorFrame;
import vm.Chip8VM;
import core.Game;
import core.event.EventManager;
import core.resource.ResourceManager;
import event.DebugStepEvent;
import event.GameInitialisedEvent;
import event.LoadRomEvent;
import event.ToggleDebugEvent;

public class Chip8Game extends Game{
   /**The {@link Chip8VM}.*/
   private Chip8VM vm;
   /**{@link EventManager} for message passing.*/
   private EventManager event = EventManager.getEventManager();
   /**Whether the VM is currently in debug mode.*/
   private boolean paused = false;

   /**
    * Constructs a new Chip8Game
    */
   public Chip8Game() {
      super("Chip8", 640, 320, false);
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         vm = new Chip8VM();
        // setTargetFPS(60);
         event.registerFor(ToggleDebugEvent.class, (event)->(paused = !paused));
         event.registerFor(DebugStepEvent.class, (event)->debugStep());
         event.registerFor(LoadRomEvent.class, (event)->vm.loadGame(((LoadRomEvent)event).getFile()));
      } catch (Exception e) {
         e.printStackTrace();
      }//End try/catch
   }//End constructor

   /**
    * {@inheritDoc}
    */
   @Override protected void update(double deltaTime) {
      if(!paused){
         cycleVM(10);
      }//End if
      draw();
   }//End method update

   /**
    * Cycles the VM by the specified number of times and decrements the delay and sound timers by one;
    * @param times the number of times to cycle the VM.
    */
   public void cycleVM(int times){
      for(int i = 0; i < times; i++){
         vm.cycle();
      }//End for
      vm.decrementDelayTimer();
      vm.decrementSoundTimer();
   }//End method cycleVM

   /**
    * Draws the contents of the VM display buffer to the screen
    */
   public void draw(){
      spriteBatch.clear();
      spriteBatch.begin();
      boolean[][] displayBuffer = vm.getDisplayBuffer();
      int xSize = Display.getWidth() / displayBuffer.length;
      int ySize = Display.getHeight() / displayBuffer[0].length;
      for(int x = 0; x < displayBuffer.length; x++){
         for(int y = 0; y < displayBuffer[x].length; y++){
            if(displayBuffer[x][y]){
               context.getSpriteBatch().draw(ResourceManager.getBlankTexture(), x  * xSize, y * ySize, xSize, ySize);
            }//End if
         }//End for
      }//End for
      spriteBatch.end();
   }//End method draw

   /**
    * {@inheritDoc}
    */
   @Override protected void init() {
      vm.initialise();
      SwingUtilities.invokeLater(this::openFrames);
   }//End method unit

   public void openFrames(){
      EmulatorFrame frame = new EmulatorFrame(vm);
      frame.setVisible(true);
      frame.pack();  
      EventManager.getEventManager().fireEvent(new GameInitialisedEvent());
   }//End method openFrames

   /**
    * Steps forward the VM by 1 operation.
    */
   private void debugStep(){
      if(paused){
         cycleVM(1);
      }//End if
   }//End method debugStep

   /**
    * Entry point.
    * @param args arguments.
    */
   public static void main(String[] args){
      new Chip8Game().run();
   }//End method main

}//End class Chip8Game
