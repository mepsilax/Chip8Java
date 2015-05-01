package vm;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the memory for the chip 8 virtual machine
 */
public class Chip8Memory {
   /**Array holding the memory.*/
   protected short[] memory = new short[4096];
   /**Changes that have happened to the memory.*/
   protected List<Change> memoryChanges = new ArrayList<Chip8Memory.Change>();
   
   public class Change{
      private int loc;
      private short newVal;
      
      private Change(int loc, short val){
         this.loc = loc;
         this.newVal = val;
      }
      
      public int getChangeLocation(){
         return loc; 
      }
      
      public short getNewValue(){
         return newVal;
      }
   }
   
   public void setMemory(int loc, short val){
      memory[loc] = val;
      memoryChanges.add(new Change(loc, val));
   }
   
   public short getValueAt(int loc){
      return memory[loc];
   }
   
   public int getMemorySize(){
      return memory.length;
   }
   
   public List<Change> getChanges(){
      List<Change> changes = new ArrayList<Chip8Memory.Change>(memoryChanges);
      memoryChanges.clear();
      return changes;
   }
}
