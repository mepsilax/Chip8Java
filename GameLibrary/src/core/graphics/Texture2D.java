package core.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.BufferUtils;

import utility.ImageUtils;
import core.resource.Resource;
import core.resource.ResourceManager;
import static org.lwjgl.opengl.GL11.*;
import de.matthiasmann.twl.utils.PNGDecoder;

/**
 * A Texture2D is an image inside the OpenGL context
 * that can be used to add colour data to a fragment inside the
 * fragment shader.
 * NOTE: Currently this can only take .png as a texture format.
 */
public class Texture2D extends Resource {
   /** Constant defining the PNG filename extensions. **/
   public static String PNG_EXTENSION = ".PNG";
   /**The width of this texture**/
   private int width;
   /**The height of this texture**/
   private int height;
   /**The OpenGL reference of this texture**/
   private int glRef;
   /**The colour data for this texture, only populated if requested by the user.**/
   private Colour[][] colourData;

   /**
    * Constructs an empty Texture2D.
    */
   public Texture2D(){
      this(false);
   }//End constructor
   
   /**
    * Constructs an empty Texture2D.
    * @param blank1by1 if true the texture will be made to be a 1x1 white pixel with an alpha of 255.
    */
   public Texture2D(boolean blank1by1){
      glRef = glGenTextures();
      if(blank1by1){
         createBlankOneByOne();
      }//End if
   }//End constructor

   /**
    * {@inheritDoc}
    */
   @Override public void load(String fileName) {
      try {
         InputStream stream = ResourceManager.getInputStreamForFilename(fileName);
         PNGDecoder decoder = new PNGDecoder(stream);
         width = decoder.getWidth();
         height = decoder.getHeight();
         //Create the texture in openGL
         glBindTexture(GL_TEXTURE_2D, glRef);
         //Load the png information from the PNG decoder
         ByteBuffer buffer = BufferUtils.createByteBuffer(4 * width * height);
         decoder.decode(buffer, width * 4, PNGDecoder.Format.RGBA);
         buffer.flip();

         //Set the default texture parameters //TODO maybe add some options to specify these
         glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
         glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
         glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
         glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
         //load the texture data into graphics memory
         glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
         glBindTexture(GL_TEXTURE_2D, 0);
      } catch (IOException e) {
         e.printStackTrace();
      }//End try catch
   }
   
   /**
    * Loads this {@link Texture2D} using the colour data from a {@link BufferedImage}.
    * @param image the {@link BufferedImage} to use as this {@link Texture2D}.
    */
   public void load(BufferedImage image){
      bind();
      width = image.getWidth();
      height = image.getHeight();
      ByteBuffer buffer = ImageUtils.createBufferFromImage(image);
      //Set the default texture parameters //TODO maybe add some options to specify these
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
      //load the texture data into graphics memory
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      glBindTexture(GL_TEXTURE_2D, 0);
      unbind();
   }//End method load.

   private void createBlankOneByOne(){
      width = 1;
      height = 1;
      glBindTexture(GL_TEXTURE_2D, glRef);
      ByteBuffer buffer = BufferUtils.createByteBuffer(4);
      buffer.put((byte)0xFF);
      buffer.put((byte)0xFF);
      buffer.put((byte)0xFF);
      buffer.put((byte)0xFF);
      buffer.flip();
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
      glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      glBindTexture(GL_TEXTURE_2D, 0);
   }
   
   /**
    * Gets a {@link BufferedImage} from the data in this texture.
    * This method iterates over each pixel in the texture, so can be a bit slow.
    * @return a {@link BufferedImage} from the data in the current texture
    */
   public BufferedImage getBufferedImageFromData(){
      ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
      bind();
      glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      for(int x = 0; x < width; x++){
         for(int y = height - 1; y > 0 ; y--){
            int i = (x + (width * y)) * 4;
            int r = buffer.get(i) & 0xFF;
            int g = buffer.get(i + 1) & 0xFF;
            int b = buffer.get(i + 2) & 0xFF;
            int a = buffer.get(i + 3) & 0xFF;
            image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
         }//End for
      }//End for
      return image;
   }//End method getBufferedImageFromData
   
   /**
    * Gets the colour data for this texture.
    * @return the {@link Colour} data for this texture as a 2D array.
    */
   public Colour[][] getColourData(){
      if(colourData == null){
         colourData = getSubImageColourData(0, 0, width, height);
      }//End if
      return colourData;
   }//End method getColourData
   
   /**
    * Gets the {@link Colour} data for the sub section specified of this {@link Texture2D}.
    * @param startX the x pixel to start {@link Colour} data from.
    * @param startY the y pixel to start {@link Colour} data from.
    * @param width the width of the area to get the {@link Colour} data from.
    * @param height the height of the area to get the {@link Colour} data from.
    * @return a 2 dimensional array containing the {@link Colour} data of the sub section of this {@link Texture2D}.
    */
   public Colour[][] getSubImageColourData(int startX, int startY, int width, int height){
      Colour[][] colourData = new Colour[width][height];
      ByteBuffer buffer = BufferUtils.createByteBuffer(this.width * this.height * 4);
      bind();
      glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      for(int x = startX; x < startX + width; x++){
         for(int y = startY; y < startY + height; y++){
            int i = (x + (this.width * y)) * 4;
            int r = unsignedByteToInt(buffer.get(i));
            int g = unsignedByteToInt(buffer.get(i + 1));
            int b = unsignedByteToInt(buffer.get(i + 2));
            int a = unsignedByteToInt(buffer.get(i + 3));
            colourData[x - startX][y - startY] = new Colour(r / 255f, g / 255f, b / 255f, a / 255f); 
         }//End for
      }//End method for
      return colourData;
   }//End method getColourDataForArea
   
   /**
    * Converts a byte to an integer, while treating the 
    * bits of the byte as if it were unsigned.
    * @param b the byte to convert to an int.
    * @return the byte converted to an integer.
    */
   public static int unsignedByteToInt(byte b) {
      return b & 0xFF;
    }//End method unsignedByteToInt
   
   /**
    * Gets the sub image within the selected area of this texture as a {@link BufferedImage}.
    * @param x the x area to grab the sub image from.
    * @param y the y area to grab the sub image from.
    * @param w the width of the area to grab the sub image from.
    * @param h the height of the area to grab the sub image from.
    * @return a {@link BufferedImage} containing the colour data from the specified sub area 
    * on this {@link Texture2D}.
    * @see #getColourData()
    */
   public BufferedImage getSubImage(int x, int y, int w, int h){
      return getBufferedImageFromData().getSubimage(x, y, w, h);
   }//End method getSubImage
   
   /**
    * Method to bind this texture to OpenGL
    */
   public void bind(){
      glBindTexture(GL_TEXTURE_2D, glRef);
   }//End method bind

   /**
    * Method to unbind this texture from OpenGL
    */
   public void unbind(){
      glBindTexture(GL_TEXTURE_2D, 0);
   }//End method unbind

   /**
    * Gets the width of this texture
    * @return the width of this texture
    */
   public int getWidth() {
      return width;
   }//End method getWidth

   /**
    * Sets the width of this texture, should only be done by the core software
    * @param width the width to set
    */
   void setWidth(int width){
      this.width = width;
   }//End method setWidth

   /**
    * Gets the height of this texture
    * @return the height of this texture
    */
   public int getHeight(){
      return height;
   }//End method getHeight

   /**
    * Sets the height of this texture, should only be done by the core software
    * @param height the height to set
    */
   void setHeight(int height){
      this.height = height;
   }//end method setHeight

   /**
    * Gets the OpenGL reference of this texture
    * @return the OpenGL reference of this texture
    */
   public int getRef() {
      return glRef;
   }//End method getRef

   /**
    * {@inheritDoc}
    */
   @Override public List<String> validExtensions() {
      return Arrays.asList(".PNG");
   }//End method validExtensions
}//End class Texture2D
