package core.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.SharedDrawable;

import core.graphics.Texture2D;


/**
 * The ContentManager is responsible for loading and managing all {@link Resource}s within a game.
 */
public class ResourceManager {
   /**The {@link SharedDrawable} to create a shared OpenGL context for
    * the loading of texture data that can be shared between threads. */
   private static SharedDrawable sharedDrawable;
   /** A blank 1x1 white texture; */
   private static Texture2D blankTexture;
   /** Constructor **/
   private ResourceManager(){};
   
   /** Map containing {@link ResourceMap}s for each resource type */
   private static Map<Class<? extends Resource>, ResourceMap<? extends Resource>> resourceMap =
         new HashMap<Class<? extends Resource>, ResourceMap<? extends Resource>>();;

   /**
    * Method to load a {@link Resource} of the given {@link Class} and handle it in this {@link ResourceManager}.
    * @param clazz the type of the {@link Resource} to load.
    * @param fileName the path of the file to load.
    * @return the {@link Resource} that has been loaded, null if the load fails.
    */
   private static <T extends Resource> T load(Class<T> clazz, String fileName){
      try {
         //ThreadedOGLUtil.setContext();
         T resource = clazz.newInstance();
         String extension = fileName.substring(fileName.lastIndexOf('.'), fileName.length()).trim().toUpperCase();
         if(resource.validExtensions() != null && !resource.validExtensions().contains(extension)){
            throw new IllegalArgumentException("The file extension " + extension + " for resource type " + clazz.getCanonicalName() +
                  ". Supported file types are " + resource.validExtensions().toString());
         }//End if
         resource.load(fileName);
         resource.setName(fileName);
         addContent(resource, fileName);
         return resource;
      } catch (InstantiationException | IllegalAccessException | IOException e) {
         System.err.println("Unable to create a new instance of " + clazz.getCanonicalName());
         e.printStackTrace();
      }//End try/catch
      return null;
   }//End method load

   public static InputStream getInputStreamForFilename(String fileName) throws IOException{
      return ResourceManager.class.getClassLoader().getResourceAsStream(fileName);
   }//End method getInputStreamForFileName
   
   public static File getRelativeFileForFilename(String fileName) throws IOException{
      URL url = ResourceManager.class.getClassLoader().getResource(fileName);
      if(url == null){
         return new File(fileName);
      } else {
         return new File(url.getPath());
      }//End if
   }//End method getRelativeFileForFilename
   /**
    * Initialises this {@link ResourceManager} and creates a shared context for this thread 
    * so loaded OpenGL resources can be shared.
    * @throws LWJGLException
    */
   public static void initialise() throws LWJGLException{
      if(sharedDrawable == null){
         boolean releaseContext = false;
         Display.makeCurrent();
         if(!Display.isCurrent()){
            releaseContext = true;
         }//End if
         
         sharedDrawable = new SharedDrawable(Display.getDrawable());

         if(releaseContext){
            Display.releaseContext();
         }//End if
      }//End if
   }//End method checkContext
   
   /**
    * Method to add a {@link Resource} to this {@link ResourceManager}.
    * @param resource the {@link Resource} to add to this {@link ResourceManager}
    * @param name the name of the {@link Resource}.
    */
   private static <T extends Resource> void addContent(T resource, String name){
      if(!resourceMap.containsKey(resource.getClass())){
         resourceMap.put(resource.getClass(), new ResourceMap<T>());
      }//End if
      @SuppressWarnings("unchecked")
      ResourceMap<T> resources = (ResourceMap<T>) resourceMap.get(resource.getClass());
      if(resources.containsKey(name)){
         throw new IllegalArgumentException(
               "The content manager already has a resource named " + name + " for the type " + resource.getClass().getName());
      } else {
         resources.addResource(name, resource);
      }//End if
   }//End method addContent
   
   /**
    * Gets the resource of the given type from this content manager.
    * @param clazz the {@link Class} of the resource to find.
    * @param resourceName the name of the resource to find.
    * @return The {@link Resource} of type T with the given name, if not found this manager will
    *  attempt to load the resource.
    */
   @SuppressWarnings("unchecked")
   public static <T extends Resource> T getResource(Class<T> clazz, String resourceName){
      ResourceMap<?> resources = resourceMap.get(clazz);
      if(resources != null){
         T resource = (T) resources.getResource(resourceName);
         if(resource == null){
            resource = load(clazz, resourceName);
         }//End if
         return resource;
      } else {
         return load(clazz, resourceName);
      }//End if
   }//End method getResource
   
   /**
    * Returns a 1x1 white {@link Texture2D}.
    * @return a 1x1 white {@link Texture2D}.
    */
   public static Texture2D getBlankTexture(){
      if(blankTexture == null){
         blankTexture = new Texture2D(true);
      }//End if
      return blankTexture;
   }//End method getBlankTexture
}//End class ContentManager
