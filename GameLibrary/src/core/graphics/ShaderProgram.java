package core.graphics;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.Display;

import core.event.Event;
import core.event.EventListener;
import core.event.EventManager;
import core.event.WindowResizedEvent;

public class ShaderProgram {
   
   public static final String RESOLUTION = "resolution";
   /**The OpenGL object reference to this program**/
   private int program;
   
   private static final List<VertexAttribute> DEFAULT_ATTRIBUTE =
         Arrays.asList(new VertexAttribute[]{
               new VertexAttribute(0, "Position", 3),
               new VertexAttribute(1, "Colour", 4),
               new VertexAttribute(2, "TexCoord", 2)
         });

   
   private class ResizeListener implements EventListener{

      @Override
      public void notify(Event event) {
         setResolutionUniform();
      }
   }
   
   public ShaderProgram(String vertexPath, String fragmentPath){
      this(vertexPath, fragmentPath, DEFAULT_ATTRIBUTE);
      EventManager.getEventManager().registerFor(WindowResizedEvent.class, new ResizeListener());
      setResolutionUniform();
   }
  
   /**
    * Constructs and compiles a new {@link ShaderProgram}
    * @param vertexPath the file name of the vertex shader
    * @param fragmentPath the file name of the fragment shader
    * @param attributes the {@link VertexAttribute}s to bind to the program.
    */
   public ShaderProgram(String vertexPath, String fragmentPath, List<VertexAttribute> attributes){

      //Creates and compiles the vertex shader
      int vs = glCreateShader(GL_VERTEX_SHADER);
      glShaderSource(vs, readFromFile(vertexPath));
      glCompileShader(vs);
      if(glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE){
         System.err.println("Failed to compile the vertex shader " + vertexPath);
         String log = glGetShaderInfoLog(vs, glGetShaderi(vs, GL_INFO_LOG_LENGTH));
         System.err.println(log);
         System.exit(-1);
      }//End if

      //Creates and compiles the fragment shader
      int fs = glCreateShader(GL_FRAGMENT_SHADER);
      glShaderSource(fs, readFromFile(fragmentPath));
      glCompileShader(fs);
      if(glGetShaderi(fs, GL_COMPILE_STATUS) == GL_FALSE){
         System.err.println("Failed to compile the fragment shader " + fragmentPath);
         String log = glGetShaderInfoLog(fs, glGetShaderi(fs, GL_INFO_LOG_LENGTH));
         System.err.println(log);
         System.exit(-1);
      }//End if

      //Creates the shader program
      program = glCreateProgram();

      //Binds the vertex attributes to positions in the program
      for(VertexAttribute attribute : attributes){
         glBindAttribLocation(getProgram(), attribute.getLocation(), attribute.getName());
      }//End for

      //Attach the shaders
      glAttachShader(program, vs);
      glAttachShader(program, fs);

      //Link the program
      glLinkProgram(program);
      if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
         System.out.println("Failed to link shader program");
         String log = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
         System.out.println(log);
      }//End if

      //Cleanup
      glDetachShader(program, vs);
      glDetachShader(program, fs);
      glDeleteShader(vs);
      glDeleteShader(fs);
   }//End constructor

   private void setResolutionUniform() {
      begin();
      int res = glGetUniformLocation(program, RESOLUTION);
      if(res != -1){
         glUniform2f(res, Display.getWidth(), Display.getHeight());
      }
      end();
   }
   
   /**
    * Gets the OpenGL object reference to this shader program
    * @return the OpenGL object reference to this shader program
    */
   public int getProgram(){
      return program;
   }//End method getProgram

   /**
    * Method to read the text file at the given file name.
    * @param file the file to read.
    * @return a string containing the text from the file.
    */
   private String readFromFile(String file) {
      try {
         return readFully(getClass().getClassLoader().getResourceAsStream(file));
      } catch(Exception e) {
         throw new RuntimeException("Error reading file" + file, e);
      }//End try/catch
   }//End method readFromFile

   /**
    * Reads all the text in the input stream and returns it as a {@link String}.
    * @param is the {@link InputStream} to read from.
    * @return the {@link String} contents of the {@link InputStream}.
    */
   private static String readFully(InputStream is) {
      try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
         StringBuilder stringBuilder = new StringBuilder();
         String line;
         while((line = reader.readLine()) != null){
            stringBuilder.append(line).append('\n');
         }//End while
         return stringBuilder.toString();
      } catch(Exception e) {
         throw new RuntimeException("Error reading input stream", e);
      }//End try/catch
   }//End method readFully

   /**
    * Binds this program to the OpenGL context,
    * will take over any previously binded programs
    */
   public void begin(){
      glUseProgram(program);
   }//End method begin

   /**
    * Unbinds this program from the OpenGL context.
    */
   public void end(){
      glUseProgram(0);
   }//End method end

   /**
    * Gets the OpenGL reference to the specified uniform in this program
    * @param string the name of the uniform to get
    * @return An integer containing the OpenGL reference to the uniform if it is found,
    *    otherwise -1.
    */
   public int getUniform(String string) {
      int uniform = glGetUniformLocation(program, string);
      if(uniform == -1){
         System.out.println("Unable to find uniform " + string);
      }//End if
      return uniform;
   }//End method getUniform
   
}//End class ShaderProgram
