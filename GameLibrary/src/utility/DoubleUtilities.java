package utility;

/**
 * Utility methods for comparison of Doubles to eliminate bugs
 * relating to double equality.
 * @author Ryan Harrison
 */
public class DoubleUtilities {
   /**Delta to use for double comparison**/
   private static final double DELTA = 0.00000001;
   
   /**
    * Method to compare two double values. This method compares the doubles by ensuring that the
    * difference between the doubles is below a certain delta value.
    * @param d1 the first double to compare.
    * @param d2 the second double to compare.
    * @return whether or not the doubles are equal or not.
    */
   public static boolean doubleEquals(double d1, double d2) {
      if(Math.abs(d1 - d2) < DELTA) {
         return true;
      } else {
         return false;
      }//End if
   }//End method doubleEquals
   
   /**
    * Method to compare two double values and return whether the first is less
    * than or equal to the second.
    * @param d1 the first double to compare.
    * @param d2 the second double to compare.
    * @return whether or not d1 is less than or equal to d2.
    */
   public static boolean doubleLessThanOrEqual(double d1, double d2){
       if(d1 < d2 || doubleEquals(d1, d2)){
           return true;
       } else {
           return false;
       }//End if
   }//End method doubleLessThanOrEqual

   /**
    * Method to compare two double values and return whether the first is 
    * greater than or equal to the second.
    * @param d1 the first double to compare.
    * @param d2 the second double to compare.
    * @return whether or not d1 is greater than or equal to d2.
    */
   public static boolean doubleGreaterThanOrEqual(double d1, double d2){
       if(d1 > d2 || doubleEquals(d1, d2)){
           return true;
       } else {
           return false;
       }//End if
   }//End method doubleGreaterThanOrEqual

   /**
    * Method to compare two double values and return whether the first is 
    * strictly less than the second.
    * @param d1 the first double to compare.
    * @param d2 the second double to compare.
    * @return whether or not d1 is strictly less than d2.
    */
   public static boolean doubleStrictlyLessThan(double d1, double d2){
       if(d1 < d2 && !doubleEquals(d1, d2)){
           return true;
       } else {
           return false;
       }//End if
   }//End method doubleStrictlyLessThan

   /**
    * Method to compare two double values and return whether the first is 
    * strictly greater than the second.
    * @param d1 the first double to compare.
    * @param d2 the second double to compare.
    * @return whether or not d1 is strictly greater than d2.
    */
   public static boolean doubleStrictlyGreaterThan(double d1, double d2){
       if(d1 > d2 && !doubleEquals(d1, d2)){
           return true;
       } else {
           return false;
       }//End if
   }//End method doubleStrictlyGreaterThan
}//End class DoubleUtilities
