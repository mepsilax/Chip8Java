package core.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.resource.Resource;

public class SpriteFont extends Resource{
   /**ASCII code for space.*/
   private static final int SPACE = 32;
   /**ASCII Code for ! character, first printable character in set**/
   private int base = 33;
   /**ASCII Code for ~ character, last printable character in set**/
   private int max = 126;
   /**The {@link Texture2D} containing all of the prerendered characters.*/
   private Texture2D characterTexture;
   /**Character map for mapping a character to its position in its {@link Texture2D}.**/
   private Map<Character, Rectangle> characterMap;
   /**The {@link FontMetrics} for getting information about this font.*/
   private FontMetrics metrics;

   /**
    * Constructs a new {@link SpriteFont}
    */
   public SpriteFont(){
      characterMap = new HashMap<Character, Rectangle>();
   }//End constructor

   /**
    * Loads this sprite font
    */
   public void load(String fontName){
      String[] fontType = fontName.substring(0, fontName.lastIndexOf(".")).split("-");
      Font font = new Font(fontType[1], Font.PLAIN, new Integer(fontType[0]));
      BufferedImage fontBuffer = createBufferedImage(font);
      
      Graphics2D graphics = fontBuffer.createGraphics();
      graphics.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
      graphics.setBackground(Color.white);
      graphics.setFont(font);
      metrics = graphics.getFontMetrics();
      graphics.setColor(Color.white);
      int currentXPos = 0;
      int line = 1;
      for(int i = base; i <= max; i++){
         int width = metrics.charWidth(i);
         if(currentXPos + width > 512){
            line++;
            currentXPos = 0;
         }//End if
         int y = line * (metrics.getAscent() + metrics.getLeading() + metrics.getDescent());
         graphics.drawString(new Character((char) i).toString(), currentXPos, y);
         int yPos = y  - metrics.getLeading() - metrics.getAscent(); 
         int height = metrics.getLeading() + metrics.getAscent()  + (hasDescent((char)i) ? metrics.getDescent() : 0) ;
         characterMap.put((char) i, new Rectangle(currentXPos , yPos, width,  height));
         currentXPos += width + 1;
      }//End for
      characterTexture = new Texture2D();
      characterTexture.load(fontBuffer);
   }//End method load
   
   private boolean hasDescent(char c){
      return ("gjpqy").contains(Character.toString(c));
   }
   /**
    * Creates a {@link BufferedImage} of the correct width and height for this {@link SpriteFont}.
    * @param font the {@link Font} to for creating the {@link SpriteFont}
    * @return the empty {@link BufferedImage} of the correct width and height for this {@link SpriteFont}.
    */
   private BufferedImage createBufferedImage(Font font) {
      BufferedImage fontBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D graphics = fontBuffer.createGraphics();
      graphics.setFont(font);
      FontMetrics metrics = graphics.getFontMetrics();
      int currentXPos = 0;
      int line = 1;
      for(int i = base; i <= max; i++){
         int width = metrics.charWidth(i);
         if(currentXPos + width > 512){
            line++;
            currentXPos = 0;
         }//End if
         currentXPos += width + 1;
      }//End for
      
      fontBuffer = new BufferedImage(512, (int) ((line + 0.5)* metrics.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
      return fontBuffer;
   }//End method createBufferedImage
   
   
   public void drawTextString(int x, int y, String string, SpriteBatch batch){
      for(int i = 0; i < string.length(); i++){
         char character = string.charAt(i);
         if(character == SPACE){
            x += metrics.stringWidth(" ");
            continue;
         }//End if
         if(!characterMap.containsKey(character)){
            continue;
         }//End if
         Rectangle bounds = characterMap.get(character);
         batch.draw(characterTexture, x, y, bounds.width, bounds.height,bounds.x, bounds.y, 0);
         x += bounds.width;
      }//End for
   }//End method drawTextString
   
   public boolean validCharacter(char character){
      if(character >= base && character <= max){
         return true;
      }
      return false;
   }//End method validCharacter
   
   @Override public List<String> validExtensions() {
      return null;
   }
   
   public FontMetrics getMetrics(){
      return metrics;
   }
   
   public int measureStringWidth(String string){
      return metrics.stringWidth(string);
   }
   
   public int getHeight(){
      return metrics.getHeight();
   }
}
