package utility;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

public class TokenizerUtilities{
   private static final String TRUE = "TRUE";
   public static final String NEW_LINE =  System.getProperty("line.separator");
   public static final String TAB = "   ";

   public static StreamTokenizer createTokenizer(Reader reader){
      StreamTokenizer tokenizer = null;
      tokenizer = new StreamTokenizer(reader);
      tokenizer.wordChars( '_', '_' );
      return tokenizer;
   }//End method createTokenizer
   public static boolean isNextToken( StreamTokenizer tokenStream,  String requiredToken){
      boolean found = false;
      try {
         int token = tokenStream.nextToken();
         if ( token == StreamTokenizer.TT_WORD ) {
            if ( tokenStream.sval.equals ( requiredToken ) ) {
               found = true;
            } // End if token name matches
         } // End if token is a word

         tokenStream.pushBack();
      } catch ( Exception fileError ) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser

      return found;
   } // End method isNextToken

   public static boolean readBoolean ( StreamTokenizer tokenStream ) throws IOException{
      int token = tokenStream.nextToken();
      if (token == StreamTokenizer.TT_WORD) {
         return tokenStream.sval.equalsIgnoreCase( TRUE );
      } else {
         throw new IOException();
      } // end if token is boolean
   } // End readBoolean

   public static boolean readBoolean( StreamTokenizer tokenStream, String requiredToken){
      try {
         int token = tokenStream.nextToken();
         if (token == StreamTokenizer.TT_WORD) {
            if (tokenStream.sval.equals(requiredToken)) {
               return readBoolean( tokenStream );
            } else {
               System.out.println("readBoolean - expected " + requiredToken + " token");
               tokenStream.pushBack();
            } // End if is specified token

         } else {
            System.out.println("-->" + token + "<--");
         } // End if token is word ?

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser

      return false;
   } // End method readBoolean

   public static double readDouble ( StreamTokenizer tokenStream ) throws IOException{
      int token = tokenStream.nextToken();
      if (token == StreamTokenizer.TT_NUMBER) {
         double tempDouble = tokenStream.nval;
         return tempDouble;
      } else {
         throw new IOException();
      } // end if token is numeric ?
   } // End readDouble

   public static double readDouble( StreamTokenizer tokenStream, String requiredToken){
      double followingDouble = 0.0;
      try {
         int token = tokenStream.nextToken();
         if (token == StreamTokenizer.TT_WORD) {
            if (tokenStream.sval.equals(requiredToken)) {
               // Read the accompanying double.
               //
               token = tokenStream.nextToken();
               if (token == StreamTokenizer.TT_NUMBER) {
                  followingDouble = tokenStream.nval;
               } else {
                  System.out.println("readDouble - expected a double number for token " + requiredToken);
                  // Not a number and, therefore, not valid data.
                  //
               } // End if token = numeric?

            } else {
               System.out.println( "readDouble - expected " + requiredToken + " token");
               tokenStream.pushBack();
               // Not the specified token: error reading data.
               //
            } // End if is specified token ?

         } else {
            // Token is not a WORD.  Wrong token type.
            //
            System.out.println("-->" + token + "<--");

         } // End if token is word ?

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser

      return followingDouble;
   } // End method readDouble

   public static int readInteger ( StreamTokenizer tokenStream ) throws IOException{
      int token = tokenStream.nextToken();
      if (token == StreamTokenizer.TT_NUMBER) {
         int tempInt = (int) tokenStream.nval;
         return tempInt;
      } else {
         throw new IOException();
      } // end if token is numeric ?
   } // End readInteger

   public static int readInteger( StreamTokenizer tokenStream, String requiredToken ){
      int followingInt = 0;
      try {
         int token = tokenStream.nextToken();
         if (token == StreamTokenizer.TT_WORD) {
            if (tokenStream.sval.equals(requiredToken)) {
               // Read the accompanying string.
               //
               token = tokenStream.nextToken();
               if (token == StreamTokenizer.TT_NUMBER) {
                  followingInt = (int) tokenStream.nval;
               } else {
                  // Not a number and, therefore, not valid data.
                  //
               } // End if token = numeric?
            } else {
               // Not the specified token: error reading data.
               //
               System.out.println("readInteger - expected " + requiredToken + " token");
               tokenStream.pushBack();
            } // End if is specified token ?

         } else {
            // Token is not a WORD.  Wrong token type.
            //
            System.out.println("-->" + token + "<--");
         } // End if token is word ?

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser
      return followingInt;
   } // End method readInteger

   public static double readOptionalDouble( StreamTokenizer tokenStream, String requiredToken, double defaultValue ){
      try {
         int token = tokenStream.nextToken();
         if ( token == StreamTokenizer.TT_WORD && tokenStream.sval.equals(requiredToken) ) {
            tokenStream.pushBack();
            return readDouble(tokenStream, requiredToken);
         } else {
            tokenStream.pushBack();
            return defaultValue;
         } // End if token is word and token matches

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser

      return defaultValue;
   } // End method readOptionalDouble

   public static int readOptionalInteger( StreamTokenizer tokenStream, String requiredToken, int defaultValue ){
      try {
         int token = tokenStream.nextToken();
         if ( token == StreamTokenizer.TT_WORD && tokenStream.sval.equals(requiredToken) ) {
            tokenStream.pushBack();
            return readInteger(tokenStream, requiredToken);
         } else {
            tokenStream.pushBack();
            return defaultValue;
         } // End if token is word and token matches

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser
      return defaultValue;
   } // End method readInteger

   public static boolean readOptionalString( StreamTokenizer tokenStream, String requiredToken ){
      try {
         int token = tokenStream.nextToken();
         if ( token == StreamTokenizer.TT_WORD && tokenStream.sval.equals(requiredToken) ) {
            return true;
         } else {
            tokenStream.pushBack();
            return false;
         } // End if token is word and token matches
      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser
      return false;
   } // End method readOptionalString


   public static String readString ( StreamTokenizer tokenStream ) throws IOException{
      int token = tokenStream.nextToken();
      if (token == StreamTokenizer.TT_WORD || token == '\"' || token == '\'') {
         String tempString = tokenStream.sval;
         return tempString;
      } else {
         throw new IOException( "Unrecognised token - " + tokenStream.toString() );
      }
   }

   public static String readString( StreamTokenizer tokenStream, String requiredToken){
      String followingString = null;
      try {
         int token = tokenStream.nextToken();
         if (token == StreamTokenizer.TT_WORD) {
            if (tokenStream.sval.equals(requiredToken)) {
               // Read the accompanying string.
               token = tokenStream.nextToken();
               if (token == StreamTokenizer.TT_WORD || token == '\"' || token == '\'') {
                  followingString = tokenStream.sval;
               } else {
                  // Not a string and, therefore, not valid data.
               } // End if
            } else {
               // Not the specified token: error reading data.
               System.out.println("readString - expected " + requiredToken + " token");
               tokenStream.pushBack();
            } // End if 
         } else {
            // Token is not a WORD.  Wrong token type.
            System.out.println("-->" + token + "<--");
         } // End if
      } catch (Exception fileError) {
         System.err.println("Unexpected file exception");
         fileError.printStackTrace();
      } // End catch 
      return followingString;
   } // End method readString

   public static boolean readToken( StreamTokenizer tokenStream, String requiredToken){
      try {
         int token = tokenStream.nextToken();
         if (token == StreamTokenizer.TT_WORD) {
            if (tokenStream.sval.equals(requiredToken)) {
               // Token read ok.
               //
               return true;
            } else {
               // Not the specified token: error reading data.
               //
               System.out.println(
                     "readString - expected " + requiredToken + " token");
               tokenStream.pushBack();
            } // End if is specified token ?

         } else {
            // Token is not a WORD.  Wrong token type.
            //
            System.out.println("-->" + token + "<--");
         } // End if token is word ?

      } catch (Exception fileError) {
         System.out.println("Unexpected file exception");
      } // End catch block for StreamTokeniser
      return false;
   } // End method readToken

   public static boolean isEndOfFile(StreamTokenizer tokenizer) {
      int token;
      try {
         token = tokenizer.nextToken();
         if(token == StreamTokenizer.TT_EOF){
            return true;
         } else {
            tokenizer.pushBack();
            return false;
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

} // End class TokenUtilities
