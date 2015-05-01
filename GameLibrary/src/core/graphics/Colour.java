package core.graphics;

public class Colour {
   public static Colour White = new Colour(1,1,1,1);
   public static Colour Blue  = new Colour(0,0,1,1);
   public static Colour Red   = new Colour(1,0,0,1);
   public static Colour Green = new Colour(0,1,0,1);
   public static Colour Black = new Colour(0,0,0,1);
   /**Colour information**/
   float r, b, g, a;

   public Colour(float r, float g, float b, float a){
      this.r = r;
      this.b = b;
      this.g = g;
      this.a = a;
   }

   public float getR(){
      return r;
   }
   public float getB(){
      return b;
   }
   public float getG(){
      return g;
   }
   public float getA(){
      return a;
   }
   
   public char getRInt(){
      return (char) (r * 255);
   }

   public char getGInt(){
      return (char) (g * 255);
   }
   public char getBInt(){
      return (char) (b * 255);
   }
   public char getAInt(){
      return (char) (a * 255);
   }
   
   
   @Override
   public String toString() {
      return r + " " + g + " " + b + " " + a;
   }
}
