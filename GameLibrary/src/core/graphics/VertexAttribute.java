package core.graphics;

public class VertexAttribute {
   
   /**The name of this attribute for shader attribute mapping**/
   private String name;
   /**The number of components in this attribute**/
   private int components;
   /**The location in glVertexAttribLocation this attribute will be bound to**/
   private int location;
      
   /**
    * Constructs a new {@link VertexAttribute}
    * @param location the location in the shader this attribute will be bound to.
    * @param name the name of this attribute.
    * @param numComponents the number of vertices that make up this attribute.
    */
   public VertexAttribute(int location, String name, int numComponents) {
      this.location = location;
      this.name = name;
      this.components = numComponents;
   }//End constructor
   
   /**
    * Method to get the name of this attribute
    * @return the name of this attribute
    */
   public String getName(){
      return name;
   }//End method getName
   
   /**
    * Method to get the number of components in this attribute
    * @return the number of vertices that make up this attribte
    */
   public int getComponents(){
      return components;
   }//End method getComponents
   
   /**
    * Method to get the shader location of this attribute.
    * @return the shader location of this attribute
    */
   public int getLocation(){
      return location;
   }//End method getLocation
   
}//End class VertexAttribute