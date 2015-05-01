package core.input;

/**
 * Event listener for when a keyboard shortcut has been pressed in the {@link InputManager}.
 */
public interface ShortcutListener {
   /**
    * Notifies this listener.
    * @param modifierCode the keyboard code of the modifier of the shortcut.
    * @param keyCode the keyboard code of the key of the shortcut.
    */
   public void notify(int modifierCode, int keyCode);
}//End interface ShortcutListener
