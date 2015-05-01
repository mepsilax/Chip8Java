package core.event;

/**
 * {@link Event} raised when the main window has been resized
 */
public class WindowResizedEvent extends Event {
   
   /**The new width of the window.*/
   public int width;
   /**The new height of the window.*/
   public int height;
   
   /**
    * Constructor
    * @param width the new width of the window.
    * @param height the new height of the window.
    */
   public WindowResizedEvent(int width, int height) {
      this.width = width;
      this.height = height;
   }//End constructor
   
   /**
    * Gets the new width of the resized window in pixels.
    * @return the width of the window in pixels.
    */
   public int getWidth(){
      return width;
   }//End method getWidth
   
   /**
    * Gets the new height of the resized window in pixels.
    * @return the new height of the window in pixels.
    */
   public int getHeight(){
      return height;
   }//End method getHeight
   
}//End class WindowResizedEvent
