package event;

import java.io.File;

import core.event.Event;

public class LoadRomEvent extends Event{
   private File file;
   
   public LoadRomEvent(File file){
      this.file = file;
   }
   
   public File getFile(){
      return file;
   }

}
