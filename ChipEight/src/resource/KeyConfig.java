package resource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import utility.TokenizerUtilities;
import core.resource.Resource;
import core.resource.ResourceManager;

/**
 * Resource representing the key map for the chip 8 controls.
 */
public class KeyConfig extends Resource{
   /**Key map of LWJGL keys to Chip 8 keys.*/
   private Map<Integer, Integer> keyMap;
   
   /**
    * {@inheritDoc}
    */
   @Override public List<String> validExtensions() {
      return Arrays.asList( new String[]{ ".KONF"} );
   }//End method validExtensions
   
   /**
    * {@inheritDoc}
    */
   @Override public void load(String fileName) throws IOException {
      keyMap = new HashMap<Integer, Integer>();
      File file = ResourceManager.getRelativeFileForFilename(fileName);
      FileReader reader = new FileReader(file);
      StreamTokenizer tokenizer = TokenizerUtilities.createTokenizer(reader);
      while(!TokenizerUtilities.isEndOfFile(tokenizer)){
         int key = Keyboard.getKeyIndex(TokenizerUtilities.readString(tokenizer));
         int c8Key= TokenizerUtilities.readInteger(tokenizer);
         keyMap.put(key, c8Key);
      }//End while
      reader.close();
   }//End method load 
   
   /**
    * Gets the key {@link Map}
    * @return the key {@link Map}.
    */
   public Map<Integer, Integer> getKeyMap(){
      return keyMap;
   }//End method getKeyMap

}//End class KeyConfig
