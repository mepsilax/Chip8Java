package core.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import core.event.Event;
import core.event.EventManager;

public class InputManager {
   
   private static InputManager inputManager;
   /**The first key this manager should handle**/
   private static final int MIN_KEY = Keyboard.KEY_ESCAPE;
   /**The last key this manager should handle**/
   private static final int MAX_KEY = Keyboard.KEY_DELETE;
   /**The map of Keys to their code**/
   private Map<Integer, Key> keyMap;
   /**The current mouse x position in relation to the graphical display.**/
   private int mouseX;
   /**The current mouse y position in relation to the graphical display.**/
   private int mouseY;
   /**The mouse x position on the last update.*/
   private int lastMouseX;
   /**The mouse y position on the last update.*/
   private int lastMouseY;
   /**The change in mouse x position since the last update.*/
   private int mouseChangeX;
   /**The change in mouse y position since the last update.*/
   private int mouseChangeY;
   /**The number of mouse scroll wheel clicks this update.*/
   private int scrollWheelClicks;
   /**The map of mouse buttons to their code*/
   private Map<Integer, MouseButton> mouseMap;
   /**List of registered {@link KeyboardShortcut}s in this application.*/
   private List<KeyboardShortcut> shortcuts;
   
   private class KeyboardShortcut {
      private Key modifierKey;
      private Key key;
      private ShortcutListener listener;
      
      private KeyboardShortcut(Key modifierKey, Key key, ShortcutListener listener){
         if(modifierKey != null){
            this.modifierKey = modifierKey;
         } else {
            this.modifierKey = new Key(0);
            this.modifierKey.down = true;
            this.modifierKey.press = true;
         }
         this.key = key;
         this.listener = listener;
      }
      
      private boolean check(){
         if(modifierKey.down && key.press){
            return true;
         }
         return false;
      }
   }
   
   /**
    * {@link Event} to be raised when the Mouse has been clicked
    */
   public class MouseClickedEvent extends Event{
      /**The index of the mouse button that has been clicked.*/
      private int mouseIndex;
      /**The x position of the mouse on click.*/
      private int mouseX;
      /**The y position of the mouse on click.*/
      private int mouseY;
      
      /**
       * Constructs a new {@link MouseClickedEvent}.
       * @param mouseIndex the index of the mouse button that has been clicked.
       */
      public MouseClickedEvent(int mouseIndex){
         this.mouseIndex = mouseIndex;
         this.mouseX = getMouseX();
         this.mouseY = getMouseY();
      }//End constructor
      
       /**
       * Gets the index of the mouse button that has been clicked
       * @return the index of the mouse button that has been clicked
       */
      public int getMouseIndex(){
         return mouseIndex;
      }//End method getMouseIndex
      
      /**
       * Gets the x position of the mouse at the time of the click.
       * @return the x position of the mouse at the time of the click.
       */
      public int getMouseX(){
         return mouseX;
      }//End method getMouseX
      
      /**
       * Gets the y position of the mouse at the time of the click.
       * @return the y position of the mouse at the time of the click.
       */
      public int getMouseY(){
         return mouseY;
      }//End method getMouseY
   }//End class MouseClickedEvent
   
   /**
    * Inner class to represent a {@link Keyboard} key and its state.
    */
   private class Key{
      /**The LWJGL Code of this key**/
      private int keyCode;
      /**Whether this key is currently down**/
      boolean down;
      /**Whether this key has been pressed since the last frame**/
      boolean press;
      
      /**
       * Constructs a new {@link Key}.
       * @param keyCode the keycode of the {@link Key}.
       */
      private Key(int keyCode){
         this.keyCode = keyCode;
      }//End constructor
      
      /**
       * Checks the state of this {@link Key}.
       */
      private void checkKey(){
         press = false;
         if(Keyboard.isKeyDown(keyCode)){
            if(!down){
               press = true;
            }//End if
            down = true;
         } else {
            down = false;
         }//End if
      }//End method checkKey
   }//End class Key
   
   /**
    * Inner class to represent a {@link Mouse} button and its state.
    */
   private class MouseButton{
      /**Whether this button is currently down**/
      boolean down;
      /**Whether this button has been pressed since the last frame**/
      boolean press;
      
      /**
       * Checks the state of this {@link MouseButton}.
       */
      private void checkButton(Boolean event){
         press = false;
         if(event){
            down = true;
         } else {
            if(down){
               press = true;
            }//End if
            down = false;
         }//End if
      }//End method checkKey
   }//End class Key
   
   /**
    * Updates the input state
    */
   public void update(){
      for(int i = MIN_KEY; i <= MAX_KEY; i++){
         keyMap.get(i).checkKey();
      }//End for
      scrollWheelClicks = 0;
      lastMouseX = mouseX;
      lastMouseY = mouseY;
      mouseX = Mouse.getX();
      mouseY = Display.getHeight() - Mouse.getY();
      mouseChangeX = mouseX - lastMouseX;
      mouseChangeY = mouseY - lastMouseY;
      while(Mouse.next()){
         scrollWheelClicks += Mouse.getEventDWheel() / 100;
         int button = Mouse.getEventButton();
         if(button >= 0){
            MouseButton mButton = mouseMap.get(button);
            if(mButton != null){
               mButton.checkButton(Mouse.getEventButtonState());
               if(mouseMap.get(button).press){
                  EventManager.getEventManager().fireEvent(new MouseClickedEvent(button));
               }//End if
            }
         }//End if
      }//End while
      for(KeyboardShortcut shortcut : shortcuts){
         if(shortcut.check()){
            shortcut.listener.notify(shortcut.modifierKey.keyCode, shortcut.key.keyCode);
            break;
         }//End if
      }//End for
   }//End method update
   
   /**
    * Checks if the given {@link Key} is down.
    * @param key the keycode of the {@link Key} to check.
    * @return <code>true</code> if the {@link Key} is down, otherwise <code>false</code>.
    */
   public boolean isKeyDown(int key){
      return keyMap.get(key).down;
   }//End method isKeyDown
   
   /**
    * Checks if the given {@link Key} has been pressed this update.
    * @param key the keycode of the {@link Key} to check.
    * @return <code>true</code> if the {@link Key} has been pressed this update, otherwise <code>false</code>.
    */
   public boolean isKeyPress(int key){
      return keyMap.get(key).press;
   }//End method isKeyPress
   
   /**
    * Gets whether the given mouse button is currently down.
    * @param index the index of the mouse button to check.
    * @return <code>true</code> if the given mouse button is currently down, otherwise <code>false</code>.
    */
   public boolean isMouseDown(int index){
      return mouseMap.get(index).down;
   }//End method isMouseDown
   
   /**
    * Gets whether the given mouse button has completed a complete press this update.
    * @param the index of the mouse button to check.
    * @return <code>true</code> if the given button has completed a press this update, otherwise <code>false</code>.
    */
   public boolean isMousePress(int index){
      return mouseMap.get(index).press;
   }//End method isMousePress
   
   /**
    * Constructs a new {@link InputManager}.
    */
   private InputManager(){
      keyMap = new HashMap<Integer, InputManager.Key>();
      for(int i = MIN_KEY; i <= MAX_KEY; i++){
         keyMap.put(i, new Key(i));
      }//End for
      mouseMap = new HashMap<Integer, InputManager.MouseButton>();
      for(int i = 0; i < Mouse.getButtonCount(); i++){
         mouseMap.put(i, new MouseButton());
      }//End for
      shortcuts = new ArrayList<InputManager.KeyboardShortcut>();
   }//End constructor
   
   /**
    * Gets the {@link InputManager}.
    * @return the {@link InputManager}.
    */
   public static InputManager getInputManager(){
      if(inputManager == null){
         inputManager = new InputManager();
      }//End if
      return inputManager;
   }//End method getInputManager 

   /**
    * Gets the current x position of the mouse.
    * @return the current x position of the mouse.
    */
   public int getMouseX(){
      return mouseX;
   }//End method getMouseX
   
   /**
    * Gets the current y position of the mouse.
    * @return the current y position of the mouse.
    */
   public int getMouseY(){
      return mouseY;
   }//End method getMouseY
   
   /**
    * Gets the change in x mouse position this update.
    * @return the change in the x mouse position this update.
    */
   public int getMouseChangeX(){
      return mouseChangeX;
   }//End method getMouseChangeX
   
   /**
    * Gets the change in y mouse position this update.
    * @return the change in y mouse position this update.
    */
   public int getMouseChangeY(){
      return mouseChangeY;
   }//End method getMouseChangeY
   
   /**
    * Gets the number of scroll wheel clicks performed by the mouse this update.
    * @return the number of scroll wheel clicks performed by the mouse this update.
    */
   public int getScrollWheelClicks(){
      return scrollWheelClicks;
   }//End method getScrollWheelClicks
   
   /**
    * Registers a {@link KeyboardShortcut} to this {@link InputManager}
    * @param modifier the code of the modifier of the shortcut.
    * @param key the code of the key of the shortcut.
    * @param listener the listener to notify.
    */
   public void addKeyboardShorcut(int modifier, int key, ShortcutListener listener){
      KeyboardShortcut shorcut = new KeyboardShortcut(keyMap.get(modifier), keyMap.get(key), listener);
      shortcuts.add(shorcut);
   }//End method addKeyboardShorcut
}//End class InputManager
