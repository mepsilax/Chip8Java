package vm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

import core.event.EventManager;
import event.CycleCompleteEvent;
import event.RomLoadedEvent;

public class Chip8VM {

   /**Callback interface for function pointers.*/
   private interface Callback{public void run();}
   /**Key inputs for the VM, has a 16 button keyboard with keys ranging from 0-F*/
   protected Keypad keypad;
   /**Display buffer.*/
   protected boolean[][] displayBuffer = new boolean [64][32];
   /**Memory module for the VM.*/
   protected Chip8Memory memory;
   /**16 "v" registers.*/
   protected short[] v = new short[16];
   /**Stack for subroutines.*/
   protected int[] stack = new int[16];
   /**Random seed.*/
   protected long seed = System.currentTimeMillis();
   /**Current opcode ready to execute.*/
   protected int opcode;
   /**Sound timer, should decrement 60 times a second, plays a sound when != 0*/
   protected short soundTimer = 0;
   /**Delay timer, should decrement 60 times a second.*/
   protected short delayTimer = 0;
   /**I Register.*/
   protected int i;
   /**Program counter pointer.*/
   protected int pc;
   /**Stack pointer.*/
   protected int sp;
   /**Whether this vm is ready to perform cycles.*/
   protected boolean ready;
  
   /**
    * Font for characters 0 to F.
    */
   protected short [] font = new short[]{
         0xF0, 0x90, 0x90, 0x90, 0xF0, //0
         0x20, 0x60, 0x20, 0x20, 0x70, //1
         0xF0, 0x10, 0xF0, 0x80, 0xF0, //2
         0xf0, 0x10, 0xf0, 0x10, 0xf0, //3
         0x90, 0x90, 0xf0, 0x10, 0x10, //4
         0xf0, 0x80, 0xf0, 0x10, 0xf0, //5
         0xf0, 0x80, 0xf0, 0x90, 0xf0, //6
         0xf0, 0x10, 0x20, 0x40, 0x40, //7
         0xf0, 0x90, 0xf0, 0x90, 0xf0, //8
         0xf0, 0x90, 0xf0, 0x10, 0xf0, //9
         0xf0, 0x90, 0xf0, 0x90, 0x90, //A
         0xe0, 0x90, 0xe0, 0x90, 0xe0, //B
         0xf0, 0x80, 0x80, 0x80, 0xf0, //C
         0xe0, 0x90, 0x90, 0x90, 0xe0, //D
         0xf0, 0x80, 0xf0, 0x80, 0xf0, //E
         0xf0, 0x80, 0xf0, 0x80, 0x80, //F
   };

   /**Default {@link Callback} for an unknown opcode.*/
   private Callback nop = this::nullOP;

   /**
    * Top level set of chip8 instructions.
    */
   private Callback[] instructions = new Callback[]{
         this::run0XXX, this::run1NNN, this::run2NNN, this::run3XNN, 
         this::run4XNN, this::run5XY0, this::run6XNN, this::run7XNN, 
         this::run8XXX, this::run9XY0, this::runANNN, this::runBNNN, 
         this::runCXNN, this::runDXYN, this::runEXXX, this::runFXXX};

   /**
    * Chip 8 opcodes relating to the system.
    */
   private Callback[] system = new Callback[]{
         this::run00E0,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop, this::run00EE,nop};

   /**
    * Arithmetic chip 8 opcodes
    */
   private Callback[] math = new Callback[]{
         this::run8XY0,this::run8XY1,this::run8XY2,this::run8XY3,
         this::run8XY4,this::run8XY5,this::run8XY6,this::run8XY7,
         nop,nop,nop,nop,nop,nop,this::run8XYE,nop};

   /**
    * Input related chip 8 opcodes
    */
   private Callback[] input = new Callback[]{
         nop,this::runEXA1,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,nop,this::runEX9E,nop};

   /**
    * Chip 8 opcodes that control registers and memory
    */
   private Callback[] register = new Callback[0x66];

   /**
    * Constructor
    */
   public Chip8VM(){
      Arrays.fill(register, nop);
      register[0x7]  = this::runFX07;
      register[0xA]  = this::runFX0A;
      register[0x15] = this::runFX15;
      register[0x18] = this::runFX18;
      register[0x1E] = this::runFX1E;
      register[0x29] = this::runFX29;
      register[0x33] = this::runFX33;
      register[0x55] = this::runFX55;
      register[0x65] = this::runFX65;
   }//End constructor

   /**
    * Initialises this VM.
    */
   public void initialise(){
      keypad = new Keypad();
      displayBuffer = new boolean [64][32];
      memory = new Chip8Memory();
      v = new short[16];
      soundTimer = 0;
      delayTimer = 0;
      i = 0;
      sp = 0;
      ready = false;
      pc = 0x200;
      loadFonts();
   }//End method initialise

   /**
    * Loads the game from the given {@link File}e
    * @param game the game file to load.
    * @throws IOException
    */
   public void loadGame(File game){
      try{
         initialise();
         FileReader reader = new FileReader(game);
         int pc = 0x200;
         byte[] buf = Files.readAllBytes(game.toPath());
         for(byte c : buf){
            memory.setMemory(pc, (short) (c & 0xFF));
            pc++;
         }//End for
         reader.close();
         ready = true;
         EventManager.getEventManager().fireEvent(new RomLoadedEvent(game.getName()));
      } catch (IOException e) {
         e.printStackTrace();
      }//End try/catch
   }//End method loadGame

   /**
    * Loads the font into memory from 0x0000 to 0x0080
    */
   public void loadFonts(){
      for(int i = 0; i < 80; i++){
         memory.setMemory(i, font[i]);
      }//End for
   }//End method loadFonts

   /**
    * Gets the next opcode and increments the program counter by 2
    */
   protected void getOpcode(){
      opcode = ((int)memory.getValueAt(pc) << 8) | memory.getValueAt(pc+1);
      pc+=2;
   }//End method getOpcode

   /**
    * Cycles this VM, gets the opcode in memory at the address loacted at the program counter and executes it.
    */
   public void cycle(){
      if(ready){
         keypad.update();
         getOpcode();
         try{
            instructions[(opcode & 0xF000) >> 12].run();
         } catch (Exception e){
            System.out.println("Exception at opcode " + Integer.toHexString(opcode) + " at memory location 0x" + Integer.toHexString(pc - 2) + ".");
            e.printStackTrace();
         }//End try/catch
         EventManager.getEventManager().fireEvent(new CycleCompleteEvent());
      }//End if
   }//End method cycle

   /**
    * Default operation for an unreconized op code.
    */
   public void nullOP() {
      System.out.println("Unsupported opcode " + Integer.toHexString(opcode));
   }//End method nullOP

   /**
    * Runs an opcode from the 0XXX table
    */
   public void run0XXX(){
      system[(opcode & 0xF)].run();
   }//End method perform0XXX

   /**
    * Runs the opcode 00E0.
    * CLS, Clears the display.
    */
   public void run00E0(){
      for(int x = 0; x < 64; x++){
         for(int y = 0; y < 32; y++){
            displayBuffer[x][y] = false;
         }//End for
      }//End for
   }//End method run00E0

   /**
    * Runs the opcode 00EE.
    * RET, Return from a subroutine.
    */
   public void run00EE(){
      pc = stack[sp - 1]; //Set the program counter to be the top of the stack.
      sp--; //Decrement the stack counter
   }//End method run00EE
   
   /**
    * Runs the opcode 1NNN.
    * JP addr, Jump to location NNN.
    */
   public void run1NNN(){
      pc = opcode & 0x0FFF;
   }//End method run1NNN

   /**
    * Runs the opcode 2NNN.
    * CALL addr, Call subroutine at NNN.
    */
   public void run2NNN(){
      stack[sp] = pc;
      sp++;
      pc = opcode & 0x0FFF;
   }//End method run2NNN

   /**
    * Runs the opcode 3XNN
    * SNE Vx, byte, Skip next instruction if Vx != nn 
    */
   public void run3XNN(){
      short vx = v[(opcode & 0x0F00) >> 8];
      if(vx == (opcode & 0x00FF)){
         pc+=2;
      }//End if
   }//End method run3XNN

   /**
    * Runs the opcode 4XNN.
    * SE Vx, byte, Skip next instruction if Vx == nn
    */
   public void run4XNN(){
      short vx = v[(opcode & 0x0F00) >> 8];
      if(vx != (opcode & 0x00FF)){
         pc+=2;
      }//End if
   }//End method run4XNN

   /**
    * Runs the opcode5XY0
    * SE vX, vY Skip the next instruction if vX == vY
    */
   public void run5XY0(){
      short vx = v[(opcode & 0x0F00) >> 8];
      short vy = v[(opcode & 0x00F0) >> 4];
      if(vx == vy){
         pc += 2;
      }//End if
   }//End method run5XY0

   /**
    * Runs the opcode 6XNN
    * LD vx, byte. Set Vx = NN.
    */
   public void run6XNN(){
      v[(opcode & 0x0F00) >> 8] = (short) (0xFF & opcode);
   }//End method run6XNN

   /**
    * Runs the opcode 7XNN.
    * ADD vx, byte. Set Vx = Vx + NN
    */
   public void run7XNN(){
      v[(opcode & 0x0F00) >> 8] = (short) (v[(opcode & 0xF00) >> 8] + (0xFF & opcode) & 0xFF);
   }//End method run7XNN

   /**
    * Runs an opcode from the 8XXX table.
    */
   public void run8XXX(){
      math[opcode&0xF].run();
   }//End method run8XXX

   /**
    * Runs the opcode 8XY0.
    * LD vx, vy. Set Vx = Vy
    */
   public void run8XY0(){
      v[(opcode & 0xF00) >> 8] = v[(opcode & 0xF0) >> 4];
   }//End method run8XY0

   /**
    * Runs the opcode run8XY1.
    * OR vx, vy. Set Vx = Vx OR Vy
    */
   public void run8XY1(){
      int x = (opcode & 0xF00) >> 8;
      short vX = v[x];
      v[x] = (short) (vX | v[(opcode & 0xF0) >> 4]);
   }//End method run8XY1

   /**
    * Runs the opcode 8XY2.
    * AND Vx, Vy. Set Vx = Vx AND Vy.
    */
   public void run8XY2(){
      int x = (opcode & 0xF00) >> 8;
      short vX = v[x];
      v[x] = (short) (vX & v[(opcode & 0xF0) >> 4]);
   }//End method run8XY2

   /**
    * Runs the opcode 8XY3.
    * XOR vx, vy. Set Vx = Vx OR Vy.
    */
   public void run8XY3(){
      int x = (opcode & 0xF00) >> 8;
      short vX = v[x];
      v[x] = (short) (vX ^ v[(opcode & 0xF0) >> 4]);
   }//End method run8XY3

   /**
    * Runs the opcode 8XY4.
    * ADD vx, vy. Set Vx = Vx + Vy, set VF = carry.
    */
   public void run8XY4(){
      int x = (opcode & 0xF00) >> 8;
      short vxy =  (short) (v[x] + v[(opcode & 0xF0) >> 4]);
      v[0xF] = (short) ((vxy & 0x100) >> 8);
      v[x] = (short) (0xFF & vxy);
   }//End method run8XY4

   /**
    * Runs the opcode 8XY5.
    * SUB vx, vy. Set vx = vx - vy, set VF = NOT Borrow
    */
   public void run8XY5(){
      int x = (opcode & 0xF00) >> 8;
      short vX = v[x];
      short vY = v[(opcode & 0xF0) >> 4];
      v[0xF] = (short) (vX > vY ? 1 : 0);
      v[x] = (short) ((vX - vY) & 0xFF);
   }//End method run8XY5   

   /**
    * Runs the opcode 8XY6.
    * SHR vx. Shifts VX right by one, sets VF to be the least significant bit before the shift. 
    */
   public void run8XY6(){
      int x = (opcode & 0xF00) >> 8;
      v[0xF] = (short) (v[x] & 0x1);
      v[x] = (short)(v[x] >> 1);
   }//End method run8XY6

   /**
    * Runs the opcode 8XY7.
    * SUBN vx, vy. Set Vx = Vy - Vx, set VF = NOT Borrow.
    */
   public void run8XY7(){
      int x = (opcode & 0xF00) >> 8;
      short vX = v[x];
      short vY = v[(opcode & 0xF0) >> 4];
      v[0xF] = (short) (vY > vX ? 1 : 0);
      v[x] = (short) ((vY - vX) & 0xFF);
   }//End method run8XY7

   /**
    * Runs the opcode 8XYE.
    * SHL vx. Shifts Vx lefr by one, sets VF to be the most significant bit before the shift.
    */
   public void run8XYE(){
      int x = (opcode & 0xF00) >> 8;
      v[0xF] = (short)(v[x] >> 7);
      v[x] = (short) ((v[x] << 1) & 0xFF);
   }//End method run8XYE

   /**
    * Runs the opcode run 9XY0.
    * SNE Vx, Vy. Skip the next instruction if Vx != Vy.
    */
   public void run9XY0(){
      if(v[(opcode & 0xF00) >> 8] != v[(opcode & 0xF0) >> 4]){
         pc += 2;
      }//End if
   }//End method run9XY0

   /**
    * Runs the opcode ANNN.
    * LD I, addr. The value of the regster I is set to NNN.
    */
   public void runANNN(){
      i = opcode & 0xFFF;
   }//End method runANNN

   /**
    * Runs the opcode BNNN
    * JP v0, addr. Jumps to the address NNN + v0
    */
   public void runBNNN(){
      pc = ((opcode & 0xFFF) + v[0]) & 0xFFF;
   }//End method BNNN

   /**
    * Runs the opcode CXNN.
    * RND vX, byte. Set Vx = Random Number AND NN
    */
   public void runCXNN(){
      v[(opcode & 0xF00) >> 8] = (short) (new Random().nextInt(255) & (opcode & 0xFF));
   }//End method runCXNN

   /**
    * Runs the opcode DXYN.
    * DRW vx, vy, nibble. Display n-byte sprite starting at memory location I at (Vx, Vy), set VF = collision.
    */
   public void runDXYN(){
      int vX = v[(opcode & 0x0f00) >> 8];
      int vY = v[(opcode & 0x00f0) >> 4];
      int n = opcode & 0xf;
      int mLoc = i;
      v[0xF] = 0;
      for(int bytes = 0; bytes < n; bytes++){
         short sprite = memory.getValueAt(mLoc);
         for(int b = 0; b < 8; b++){
            int x = (vX + b) % 64;
            int y = vY % 32;
            boolean on = (((sprite >>  7 - b) & 0x1) == 1 ? true : false);
            boolean current =  displayBuffer[x][y];
            displayBuffer[x][y] = on ^ current;
            v[0xF] = (short) ((on && current ? 1 : 0) | v[0xF]);
         }//End for
         mLoc++;
         vY++;
      }//End for
   }//End method runDXYN

   /**
    * Runs an opcode from the EXXX table.
    */
   public void runEXXX(){
      input[opcode&0xF].run();
   }//End method EXXX

   /**
    * Runs the opcode EX9E.
    * SKP vX. Skip next instruction if the key with the value of Vx is pressed.
    */
   public void runEX9E(){
      if(keypad.isKeyPressed((v[(opcode &0xf00) >> 8] & 0xf))){
         pc+=2;
      }//End if
   }//End method runEX9E

   /**
    * Runs the opcode EXA1.
    * SKNP vx. Skip the next instruction if the key with the value of Vx is not pressed.
    */
   public void runEXA1(){
      if(!keypad.isKeyPressed((v[(opcode &0xf00) >> 8] & 0xf))){
         pc+=2;
      }//End if
   }//End method runEXA1

   /**
    * Runs an opcode from the FXXX Table
    */
   public void runFXXX(){
      register[opcode & 0xFF].run();
   }//End method runFXXX

   /**
    * Runs the opcode FX07
    * LD Vx, DT. Set Vx = delay timer value.
    */
   public void runFX07(){
      v[(opcode & 0xF00) >> 8] = delayTimer;
   }//End method runFX07

   /**
    * Runs the opcode FX15.
    * LD DT, vX. Set delay timer = vX
    */
   public void runFX15(){
      delayTimer = v[(opcode & 0xF00) >> 8];
   }//End method runFX15

   /**
    * Runs the opcode FX18.
    * LD ST, vX. Set sound timer = vx.
    */
   public void runFX18(){
      soundTimer = v[(opcode & 0xF00) >> 8];
   }//End method runFX18
   
   /**
    * Runs the opcode FX0A.
    * LD Vx, K. Wait for a keypress, Set Vx = key value.
    */
   public void runFX0A(){
      boolean key = false;
      Integer keyPress = keypad.getKeyPress();
      if(keyPress != null){
         v[(opcode & 0xF00) >> 8] = (short)keyPress.intValue();
         key = true;
      } //End if
      if(!key){
         pc-=2;
      }//End if
   }//End method runFX0A

   /**
    * Runs the opcode FX1E
    * ADD I, Vx. Set I = I + Vx
    */
   public void runFX1E(){
      i = (v[(opcode & 0xF00) >> 8] + i) & 0xFFF;
   }//End method runFX1E

   /**
    * Runs the opcode FX29.
    * LD F, Vx. Set I = I + Vx.
    */
   public void runFX29(){
      i = v[(opcode & 0xf00) >> 8] * 5;
   }//End method runFX29
   
   /**
    * Runs the opcode FX33.
    * LD B, Vx. Store BCD representation of Vx in memory locations I, I+1 and I+2.
    */
   public void runFX33(){
      short vX = v[(opcode & 0xF00) >> 8];
      memory.setMemory(i, (short) (vX / 100));
      memory.setMemory(i + 1, (short) ((vX - memory.getValueAt(i) * 100) / 10));
      memory.setMemory(i + 2, (short) (vX -  memory.getValueAt(i)*100 - memory.getValueAt(i+1) * 10));
   }//End method runFX33

   /**
    * Runs the opcode FX55.
    * LD [I], Vx. Store registers V0 through Vx in memory starting at location I.
    */
   public void runFX55(){
      int memLoc = i;
      for(int i = 0; i <= (opcode & 0xf00) >> 8; i++){
         memory.setMemory(memLoc, v[i]);
         memLoc++;;
      }//End for
   }//End method runFX55

   /**
    * Runs the opcode FX65.
    * LD Vx, [I]. Read registers V0 through Vx from memory starting at location I.
    */
   public void runFX65(){
      int memLoc = i;
      for(int i = 0; i <= (opcode & 0xf00) >> 8; i++){
         v[i] = memory.getValueAt(memLoc);
         memLoc++;
      }//End for
   }//End method runFX65
   
   /**
    * Gets the contents of the Address (I) register.
    * @return the contents of the address register.
    */
   public int getAddressRegister(){
      return i;
   }//End method getAddressRegister
   
   /**
    * Gets the value of the program counter.
    * @return the value of the program counter.
    */
   public int getProgramCounter(){
      return pc;
   }//End method getProgramCounter

   /**
    * Gets the current value of the stack pointer.
    * @return the value of the stack pointer.
    */
   public int getStackPointer(){
      return sp;
   }//End method getStackPointer
   
   /**
    * Gets the current value of the sound timer.
    * @return the current value of the sound timer.
    */
   public short getSoundTimer(){
      return soundTimer;
   }//End method getSoundTimer
   
   /**
    * Decrements the sound timer by one.
    */
   public void decrementSoundTimer(){
      soundTimer = (short)Math.max(soundTimer - 1, 0);
   }//End method decrementSoundTimer
   
   /**
    * Gets the value of the delay timer. 
    * @return the current value of the delay timer.
    */
   public short getDelayTimer(){
      return delayTimer;
   }//End method getDelayTimer
   
   /**
    * Decrements the delay timer by one.
    */
   public void decrementDelayTimer(){
      delayTimer = (short)Math.max(delayTimer - 1, 0);
   }//End method decrement delayTimer
   
   /**
    * Gets the value of the register vX.
    * @param x the index of the register to get.
    * @return the value of the register vX.
    */
   public short getRegisterValue(int x){
      return v[x];
   }//End method getRegisterValue
   
   /**
    * Gets the display buffer.
    * @return a 64 x 32 boolean array representing the display buffer.
    */
   public boolean[][] getDisplayBuffer(){
      return displayBuffer;
   }//End method getDisplayBuffer.
   
   /**
    * Gets the memory of this VM.
    * @return the memory of this VM.
    */
   public Chip8Memory getMemory(){
      return memory;
   }//End method getMemory
}//End class Chip8VM
