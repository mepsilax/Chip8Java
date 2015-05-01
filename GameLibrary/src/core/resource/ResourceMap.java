package core.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * A ResourceMap holds all of the {@link Resource}s of a particular type mapped to its name.
 * @param <T> the {@link Resource} type this {@link ResourceMap} holds.
 */
public class ResourceMap <T extends Resource>{
   
   /** The {@link Map} of {@link Resource}s to their names for this {@link ResourceMap} */
   private Map<String, T> resources;
   
   /**
    * Constructs a new {@link ResourceMap}
    */
   public ResourceMap(){
      resources = new HashMap<String, T>();
   }//End constructor
   
   /**
    * Gets the {@link Resource} with the given name from this {@link ResourceMap}.
    * @param name the name of the {@link Resource} get.
    * @return the {@link Resource} with the given name, <code>null</code> if not found.
    */
   public T getResource(String name){
      return resources.get(name);
   }//End method getResource  
   
   /**
    * Adds a {@link Resource} to this {@link ResourceMap} with the given name.
    * @param name the name of the resource to add.
    * @param resource the {@link Resource} to add.
    */
   public void addResource(String name, T resource){
      resources.put(name, resource);
   }//End method addResource

   /**
    * Removes the {@link Resource} with the given name from this {@link ResourceMap}.
    * @param name the name of the {@link Resource} to remove.
    */
   public void removeResource(String name){
      resources.remove(name);
   }//End method removeResource
   
   /**
    * Checks if this {@link ResourceMap} contains a {@link Resource} of the given name.
    * @param name the name of the {@link Resource} to check.
    * @return <code>true</code> if the {@link Resource} exists in this {@link ResourceMap}, otherwise
    *    <code>false</code>.
    */
   public boolean containsKey(String name) {
      return resources.containsKey(name);
   }//End method containsKey
}//End class ResourceManager
