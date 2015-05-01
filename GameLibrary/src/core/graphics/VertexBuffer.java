package core.graphics;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * A class to hold vertex information inside a buffer in memory local memory until
 * it is drawn
 */
public class VertexBuffer {
   /**Constant defining the number of bytes in a Java 32bit float**/
   private static final int BYTES_PER_FLOAT = 4;
   /**The attributes for each vertex**/
   protected VertexAttribute[] attributes;
   /**The buffer for holding vertex information**/
   private FloatBuffer buffer;
   /**The number of vertices in this buffer**/
   private int vertCount;
   /**The total number of components in each individual vertex**/
   private int componentCount;

   /**
    * Constructs a new {@link VertexBuffer}
    * @param vertices the number of vertices this buffer can hold
    * @param attributes a list of attributes in each vertex
    */
   public VertexBuffer(int vertices, List<VertexAttribute> attributes) {
      this.attributes = attributes.toArray(new VertexAttribute[attributes.size()]);
      for (VertexAttribute a : attributes){
         componentCount += a.getComponents();
      }//End for
      this.vertCount = vertices;
      this.buffer = BufferUtils.createFloatBuffer(vertices * componentCount);
   }//End constructor

   /**
    * Flips this buffer
    */
   public void flip() {
      buffer.flip();
   }//End method flip

   /**
    * Method to clear the data in this buffer
    */
   public void clear() {
      buffer.clear();
   }//End method clear

   /**
    * Method to put a given float into this buffer.
    * @param f the float to put in the buffer.
    */
   public VertexBuffer add(float f) {
      buffer.put(f);
      return this;
   }//End method put

   /**
    * Method to get the {@link FloatBuffer} in this buffer
    * @return the {@link FloatBuffer} containing all the vertex information
    */
   public FloatBuffer buffer() {
      return buffer;
   }//End method buffer

   /**
    * Method to get the number of components that make up a whole vertex for this buffer
    * @return the number of components that make up a whole vertex for this buffer.
    */
   public int getComponentCount() {
      return componentCount;
   }//End method getComponentCount

   /**
    * Gets the vertex count of this buffer.
    * @return the vertex count of this buffer.
    */
   public int getVertexCount() {
      return vertCount;
   }//End method getVertexCount

   /**
    * Binds this buffer to the OpenGL context
    */
   private void bind() {
      int offset = 0;
      int stride = componentCount * BYTES_PER_FLOAT;

      for (int i=0; i<attributes.length; i++) {
         VertexAttribute a = attributes[i];
         buffer.position(offset);
         glEnableVertexAttribArray(a.getLocation());
         glVertexAttribPointer(a.getLocation(), a.getComponents(), false, stride, buffer);
         offset += a.getComponents();
      }//End for
   }//End method bind

   /**
    * Binds and draws the data contained in this vertex buffer.
    * @param geom the OpenGL geometry mode to use.
    * @param first the index in the buffer to start drawing from.
    * @param count the number of vertices to draw.
    */
   public void draw(int geom, int first, int count) {
      if(buffer.hasRemaining()){
         bind();
         glDrawArrays(geom, first, count);
         unbind();
      }//End if
   }//End method draw

   /**
    * Unbinds this buffer from the OpenGL context
    */
   private void unbind() {
      for (int i=0; i<attributes.length; i++) {
         VertexAttribute a = attributes[i];
         glDisableVertexAttribArray(a.getLocation());
      }//End for
   }//End method unbind
   
   /**
    * Sets the vertex order buffer of this vertex buffer.
    * @param order
    */
   public void setVertexOrderBuffer(int[] order){
      
   }//End method setVertexOrderBuffer
}//End class VertexBuffer
