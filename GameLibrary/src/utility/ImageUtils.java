package utility;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for helper methods relating to images.
 */
public class ImageUtils {
   /**
    * Creates a {@link ByteBuffer} from the given image.
    * @param image the {@link BufferedImage} to create the {@link ByteBuffer} from.
    * @return a {@link ByteBuffer} containing pixel information from the buffered image.
    */
   public static ByteBuffer createBufferFromImage(BufferedImage image) {
      int[] pixels = new int[image.getWidth() * image.getHeight()];
      image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
      ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4).order(ByteOrder.nativeOrder());
      for(int y = 0; y < image.getHeight(); y++){
         for(int x = 0; x < image.getWidth(); x++){
            int pixel = pixels[y * image.getWidth() + x];
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
         }//End for
      }//End for
      buffer.flip(); 
      return buffer;
   }//End for
}//End class ImageUtils
