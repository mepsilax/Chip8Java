package core.graphics;

/**
 * RotationOrigin defines the origin point for rotation of a {@link Texture2D} in the {@link SpriteBatch}.
 */
public class RotationOrigin {
   /**Constant defining the top left origin.*/
   public static final RotationOrigin TOP_LEFT = new RotationOrigin(0, 0);
   /**Constant defining a special case origin to be interpreted as the center point.*/
   public static final RotationOrigin CENTER = new RotationOrigin(Integer.MAX_VALUE, Integer.MAX_VALUE);

   /**Origin coordinates.*/
   private int xOrigin, yOrigin;

   /**
    * Constructs a new {@link RotationOrigin}.
    * @param x the x point of the origin.
    * @param y the y point of the origin.
    */
   public RotationOrigin(int x, int y){
      xOrigin = x;
      yOrigin = y;
   }//End constructor
   
   /**
    * Gets the x coordinate of the origin.
    * @return the x coordinate of the origin.
    */
   public int getX(){
      return xOrigin;
   }//End method getX
   
   /**
    * Gets the y coordinate of the origin.
    * @return the y coordinate of the origin.
    */
   public int getY(){
      return yOrigin;
   }//End method getY
}//End class RotationOrigin.
