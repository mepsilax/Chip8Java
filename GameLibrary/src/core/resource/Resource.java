package core.resource;

import java.io.IOException;
import java.util.List;

/**
 * Interface to mark an {@link Object} as a resource that can be managed by the {@link ResourceManager}. 
 */
public abstract class Resource {
   /** The name of this {@link Resource}.**/
   private String name;

   public Resource(){};
   
   /**
    * Method to load this resource
    */
   public void load(String fileName) throws IOException{
      throw new UnsupportedOperationException("The load operation has not been implemented for this type " + getClass().getSimpleName());
   }//End method load

   /**
    * Gets the name of this {@link Resource}.
    * @return the name of this resource as found in the {@link ResourceManager}.
    */
   public String getName(){
      return name;
   }//End method getName

   /**
    * Sets the name of this resource.
    * @param name the name of this resource to set.
    */
   public void setName(String name){
      this.name = name;
   }//End method getName
   
   /**
    * Returns a list of valid file extensions that this {@link Resource} supports.
    * @return a {@link List} of valid file extensions that this {@link Resource} supports.
    */
   public abstract List<String> validExtensions();
   
}//End interface Resource
