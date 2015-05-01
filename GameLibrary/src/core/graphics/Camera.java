package core.graphics;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * The Camera provides a simple way to position a "camera" within the 2D game world
 * with options for zoom and rotation. The user of the camera need only manipulate these properties
 * and the camera creates a translation matrix to pass into a {@link SpriteBatch} to translate the
 * on screen graphics to reflect the state of this Camera.
 */
public class Camera {
   /**The position of this {@link Camera}.**/
   private float x, y;
   /**The target position of this {@link Camera}. **/
   private Float targetX, targetY;
   /**The zoom level of this {@link Camera}.**/
   private float zoom = 1;
   /**The rotation level of this {@link Camera} in radians.**/
   private float rotation;
   /**The width and height of the viewport of this {@link Camera}*/
   private int width, height;
   /**The scroll speed of this camera**/
   private float scrollSpeed = 1000;
   /**The path for this camera to follow**/
   @SuppressWarnings("unused")
   private List<Vector2f> cameraPath;
   
   /**
    * Constructs a new {@link Camera}.
    * @param width the width of the view port.
    * @param height the height of the view port.
    */
   public Camera(int width, int height){
      this.width = width;
      this.height = height;
      cameraPath = new ArrayList<Vector2f>();
      
   }//End constructor
   
   /**
    * Creates a translation matrix reflecting the state of this {@link Camera}.
    * @return a {@link Matrix4f} containing translation information to apply this "camera" to a {@link SpriteBatch}.
    */
   public Matrix4f createTranslationMatrix(){
      return createTranslationMatrix(1f);
   }//End method createTranslationMatrix
   
   /**
    * Creates a translation matrix reflecting the state of this {@link Camera}.
    * @param paralax the paralax delta to multiply the position of this {@link Camera} for paralax scrolling effects.
    * @return a {@link Matrix4f} containing translation information to apply this "camera" to a {@link SpriteBatch}.
    */
   public Matrix4f createTranslationMatrix(float paralax){
      Matrix4f transform = new Matrix4f();
      Matrix4f translateOrigin = new Matrix4f();
      Matrix4f.translate(new Vector2f((width / 2) / zoom, (height / 2) / zoom), translateOrigin, translateOrigin);
      Matrix4f translate = new Matrix4f();
      Matrix4f.translate(new Vector2f(-x * paralax, -y * paralax), translate, translate);
      Matrix4f sca = new Matrix4f();
      Matrix4f.scale(new Vector3f(zoom, zoom, 1f), sca, sca);
      Matrix4f rotate = new Matrix4f();
      Matrix4f.rotate(rotation, new Vector3f(0, 0, 1), rotate, rotate);
      Matrix4f identity = new Matrix4f();
      // Scale > Origin > Rotate > Translate
      Matrix4f.mul(transform, sca, transform);
      Matrix4f.mul(transform, translateOrigin, transform);
      Matrix4f.mul(transform, rotate, transform);
      Matrix4f.mul(transform, translate, transform);
      Matrix4f.mul(translate, identity, translate);
      return transform;
   }//End method createTranslationMatrix
   
   /**
    * Updates this camera.
    * @param delta the delta time since the last update.
    */
   public void update(double delta){
      if(targetX != null){
         x = scrollToTarget(delta, x, targetX);
         if(x == targetX){
            targetX = null;
         }//End if
      }//End if
      if(targetY != null){
         y = scrollToTarget(delta, y, targetY);
         if(y == targetY){
            targetY = null;
         }//End if
      }//End if
   }//End method update

   /**
    * Scrolls this camera towards the target position.
    * @param delta the time in seconds since the last update.
    * @param positionToUpdate the position variable to update.
    * @param targetPosition the target position to reach.
    * @return the result of the update.
    */
   private float scrollToTarget(double delta, float positionToUpdate, float targetPosition) {
      if(positionToUpdate > targetPosition){
         positionToUpdate -= scrollSpeed * delta;
         positionToUpdate = Math.max(targetPosition, positionToUpdate);
      } else if (positionToUpdate < targetPosition){
         positionToUpdate += scrollSpeed * delta;
         positionToUpdate = Math.min(targetPosition, positionToUpdate);
      }//End if
      return positionToUpdate;
   }//End method scrollToTarget
   
   /**
    * Sets the position of this {@link Camera}.
    * @param x the x position of this {@link Camera} to set.
    * @param y the y position of this {@link Camera} to set.
    */
   public void setPosition(float x, float y){
      this.x = x;
      this.y = y;
   }//End method setPosition
   
   /**
    * Sets the position of this {@link Camera}.
    * @param position the position of this {@link Camera}.
    */
   public void setPosition(Vector2f position){
      this.x = position.x;
      this.y = position.y;
   }//End method setPosition
   
   /**
    * Sets the target position of this camera, the camera will scroll 
    * to this position each update.
    * @param x the target x position of the camera.
    * @param y the target y position of the camera.
    */
   public void setTargetPosition(float x, float y){
      this.targetX = x;
      this.targetY = y;
   }//End method targetPosition
   
   /**
    * Sets the target position and scroll speed of the camera.
    * @param x the target x position of the camera.
    * @param y the target y position of the camera.
    * @param scrollSpeed the scroll speed of the camera in pixels per second.
    */
   public void setTargetPosition(float x, float y, float scrollSpeed){
      setTargetPosition(x, y);
      this.scrollSpeed = scrollSpeed;
   }//End method setTargetPosition
   
   /**
    * Gets the position of this {@link Camera} as a {@link Vector2f}.
    * @return the position of this {@link Camera} as a {@link Vector2f}.
    */
   public Vector2f getPosition(){
      return new Vector2f((float)x, (float)y);
   }//End method getPosition
   
   /**
    * Sets the bounds of this {@link Camera}.
    * @param bounds the {@link Rectangle} bounds to set.
    */
   public void setBounds(Rectangle bounds){
      this.x = bounds.x;
      this.y = bounds.y;
      this.width = bounds.width;
      this.height = bounds.height;
   }//End method setBounds
   
   /**
    * Sets the width of the view port of this {@link Camera}.
    * @param width the width of the view port of this {@link Camera} to set.
    */
   public void setWidth(int width){
      this.width = width;
   }//End method setWidth
   
   /**
    * Sets the height of the view port of this {@link Camera}.
    * @param height the height of the view port of this {@link Camera} to set.
    */
   public void setHeight(int height){
      this.height = height;
   }//End method setHeight
   
   /**
    * Gets the view port bounds of this {@link Camera} as a {@link Rectangle}.
    * @return the view port bounds of this {@link Camera} as a {@link Rectangle}.
    */
   public Rectangle getBounds(){
      return new Rectangle((int)x - width / 2, (int)y - height / 2, width, height);
   }//End method getBounds
   
   /**
    * Sets the zoom level of this camera, with 1f being 100% and 0.1f being 10%.
    * @param zoom the zoom level of this camera to set
    */
   public void setZoom(float zoom){
      zoom = zoom * 100;
      zoom = Math.round(zoom);
      this.zoom = Math.max(zoom / 100, 0.01f);
   }//End method setZoom
   
   /**
    * Gets the zoom level of this {@link Camera}.
    * @return the zoom level of this {@link Camera}.
    */
   public float getZoom(){
      return zoom;
   }//End method getZoom
   
   /**
    * Sets the rotation of this {@link Camera} in radians.
    * @param rotation the rotation of the {@link Camera} to set.
    */
   public void setRotationRad(float rotation){
      this.rotation = rotation;
   }//End method setRotationRad
   
   /**
    * Sets the rotation of this {@link Camera} in degrees.
    * @param rotation the rotation of the {@link Camera} to set.
    */
   public void setRotationDeg(float rotation){
      this.rotation = (float) Math.toRadians(rotation);
   }//End method setRotationDeg
   
   /**
    * Gets the rotation of this {@link Camera} in radians.
    * @return the rotation of this {@link Camera} in radians.
    */
   public float getRotationRad(){
      return rotation;
   }//End method getRotationRad
   
   /**
    * Gets the rotation of this {@link Camera} in degrees.
    * @return the rotation of this {@link Camera} in degrees.
    */
   public double getRotationDeg(){
      return Math.toDegrees(rotation);
   }//End method getRotationDeg
   
   /**
    * Sets the scrollSpeed of this {@link Camera}.
    * @param scrollSpeed the scrollSpeed of this {@link Camera} to set.
    */
   public void setScrollSpeed(float scrollSpeed){
      this.scrollSpeed = scrollSpeed;
   }//End method setScrollSpeed

   /**
    * Gets the width of the viewport of this {@link Camera}.
    * @return the width of the viewport of this {@link Camera} in pixels.
    */
   public int getWidth(){
      return width;
   }//End method getWidth
   
   /**
    * Gets the height of the viewport of this {@link Camera}.
    * @return the height of the viewport of this {@link Camera} in pixels.
    */
   public int getHeight(){
      return height;
   }//End method getHeight
   
   /**
    * Method to check if this {@link Camera} contains a given area represented as a {@link Rectangle}. 
    * @param rectangle the {@link Rectangle} to check.
    * @return <code>true</code> if the area is contained in this {@link Camera} otherwise <code>false</code>. 
    */
   public boolean contains(Rectangle rectangle){
      return getBounds().intersects(rectangle);
   }//End method contains
}//End class Camera
