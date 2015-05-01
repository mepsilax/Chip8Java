package vm;

import java.util.Map;
import java.util.Map.Entry;

import resource.KeyConfig;
import core.input.InputManager;
import core.resource.ResourceManager;

public class Keypad {
   private static final String DEFAULT_KEY_CONFIG = "resource/default.konf";
   private static final int KEY_COUNT = 16;
   
   private InputManager input = InputManager.getInputManager();
   private Map<Integer, Integer> keyMap;
   private Integer keyPress;
   private boolean[] keys;
   
   public Keypad(){
      keyMap = ResourceManager.getResource(KeyConfig.class, DEFAULT_KEY_CONFIG).getKeyMap();
      keys = new boolean[KEY_COUNT];
   }//End constructor
  
   public void update(){
      keyPress = null;
      for(Entry<Integer, Integer> key : keyMap.entrySet()){
         if(input.isKeyDown(key.getKey())){
            keys[key.getValue()] = true;
            if(keyPress == null){
               keyPress = key.getValue();
            }
         } else {
            keys[key.getValue()] = false;
         }//End if
      }//End for
   }//End method update
   
   public boolean isKeyPressed(int key){
      return keys[key];
   }//End method isKeyPressed
   
   public Integer getKeyPress(){
      return keyPress;
   }
}//End class Keypad
