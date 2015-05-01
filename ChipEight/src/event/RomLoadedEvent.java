package event;

import core.event.Event;

public class RomLoadedEvent extends Event {
   private String romName;
   
   public RomLoadedEvent(String romname){
      this.romName = romname;
   }
   
   public String getRomName(){
      return romName;
   }
}
