package vm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test to check that each Chip8 opcode,
 * when run through the {@link Chip8VM} behave as expected.
 */
public class Chip8OpcodeTest {
	/**The default Program Counter start point for a Chip8 Program.*/
   private static final int PC_START = 0x200;
   /**The {@link Chip8VM} to be tested.*/
   private static Chip8VM vm;
   
   /**
    * Builds the {@link Chip8VM} for testing.
    */
   @BeforeClass public static void buildVM(){
      vm = new Chip8VM();
   }//End method buildVM
   
   /**
    * Reinitialises the {@link Chip8VM} for each test.
    */
   @Before public void setupVM(){
      vm.initialise();
      vm.forceReady();
   }//End method setupVM
   
   /**
    * Loads an opcode into the {@link Chip8VM}s memory.
    * @param location the location in memory to load the opcode.
    * @param firstHexit the first hexit of the opcode to load.
    * @param secondHexit the second hexit of the opode to load
    */
   public void loadOpcode(int location, int firstHexit, int secondHexit){
      vm.memory.setMemory(location, (short)firstHexit);
      vm.memory.setMemory(location + 1 ,(short)secondHexit);
   }//End method loadOpcode
   
   /**
    * Loads an opcode into the default Program Counter start position.
    * @param firstHexit the first hexit of the opcode to load.
    * @param secondHexit the second hexit of the opcode to load.
    */
   public void loadOpcode(int firstHexit, int secondHexit){
      loadOpcode(PC_START, firstHexit, secondHexit);
   }//End method loadOpcode
   
   /**
    * Puts an opcode into the start position, 
    * requests the VM to retrieve it.
    * 
    * Checks that the current VM opcode is the one that was loaded and that the
    * program counter is in the expected position.
    */
   @Test public void testGetOpcode(){
      loadOpcode(PC_START, 0x1A, 0xAA);
      vm.getOpcode();
      assertEquals(0x1AAA, vm.opcode);
      assertEquals(PC_START + 2, vm.pc);
   }//End method testGetOpcode.
   
   @Test public void test00E0(){
   }
   
   @Test public void test00EE(){
      
   }
   
   /**
    * Tests 1NNN, should set the PC address to NNN.
    */
   @Test public void test1NNN(){  
      loadOpcode(0x10, 0xAA);
      vm.cycle();
      assertEquals(0xAA, vm.pc);
      loadOpcode(0x0AA, 0x1A, 0xAA);
      vm.cycle();
      assertEquals(0xAAA, vm.pc);
   }//End method test1NNN
   
   /**
    * Tests the opcode 2NNN, should call a subroutine at address NNN.
    */
   @Test public void test2NNN(){
      loadOpcode(0x2A, 0xAA); //2AAA at location 0x200 - 0x201
      loadOpcode(0xAAA, 0x2B, 0xBB); //2BBB at location 0xAAA-0xAAB
      vm.cycle(); //Should perform 2AAA
      assertEquals(vm.sp, 1); //Stack pointer should have increased by 1.
      assertEquals(0x202, vm.stack[0]); //Address 0x202 (Start address + 2) should be on the stack.
      assertEquals(vm.pc, 0xAAA); //PC should point to 0xAAA as specified in the opcode 2AAA
      vm.cycle(); //Should perform 2BBB
      assertEquals(vm.sp, 2); //Stack pointer should have increased by 1.
      assertEquals(0xAAC, vm.stack[1]); //Address 0xAAC (0xAAA + 2) should be on the stack.
      assertEquals(vm.pc, 0xBBB);//PC should point to 0xBBB as specified by the opcode 2BBB
   }//End method test2NNN
   
   /**
    * Tests the opcode 3XNN.
    * Should skip the next instruction if vX is equal to NN.
    */
   @Test public void test3NNN(){
      vm.v[0] = 0x16; //Set v0 to 0x16
      vm.v[1] = 0x18; //Set v1 to 0x18
      loadOpcode(0x30, 0x16); //Load opcode 0x3016 in 0x200
      loadOpcode(PC_START + 4, 0x31, 0x18); //Load opcode 0x3118 in 0x204
      loadOpcode(PC_START + 8, 0x32, 0xFF); //Load opcode 0x32FF in 0x208
      vm.cycle();//Should run 0x3016
      assertEquals(0x204, vm.pc); //v0 should equal 0x16 so the PC should skip the next instruction 
      vm.cycle();//Should run 0x3118
      assertEquals(0x208, vm.pc); //v1 should equal 0x18 so the PC should skip the next instruction
      vm.cycle();//Should run 0x32FF
      assertEquals(0x20A, vm.pc);//v2 is 0, so shouldnt equal 0xFF, so the PC should not skip the next instruction
   }//End method test3NNN
   
   /**
    * Tests the opcode 4XNN.
    * Should skip the next instruction if vX is not equal to NN.
    */
   @Test public void test4NNN(){
      vm.v[0] = 0x16; //Set v0 to 0x16
      vm.v[1] = 0x18; //Set v1 to 0x18
      loadOpcode(0x40, 0x16); //Load opcode 0x4016 in 0x200
      loadOpcode(PC_START + 2, 0x41, 0x18); //Load opcode 0x4118 in 0x204
      loadOpcode(PC_START + 4, 0x42, 0xFF); //Load opcode 0x42FF in 0x208
      vm.cycle();//Should run 0x3016
      assertEquals(0x202, vm.pc); //v0 should equal 0x16 so the PC shouldnt skip the next instruction 
      vm.cycle();//Should run 0x3118
      assertEquals(0x204, vm.pc); //v1 should equal 0x18 so the PC shouldnt skip the next instruction
      vm.cycle();//Should run 0x32FF
      assertEquals(0x208, vm.pc);//v2 is 0, so shouldnt equal 0xFF, so the PC should skip the next instruction
   }//End method test3NNN
   
   /**
    * Tests the opcode 5XY0.
    * Should skip the next instruction if vX is equal to vY
    */
   @Test public void test5XY0(){
      vm.v[0] = 0x16; 
      vm.v[1] = 0x18;
      vm.v[3] = 0x16;
      vm.v[4] = 0x18;
      vm.v[5] = 0xFF;
      loadOpcode(0x50, 0x30); //Load opcode 0x5030 in 0x200
      loadOpcode(PC_START + 4, 0x51, 0x40); //Load opcode 0x5140 in 0x204
      loadOpcode(PC_START + 8, 0x52, 0x50); //Load opcode 0x5250 in 0x208
      vm.cycle();//Should run 0x3016
      assertEquals(0x204, vm.pc); //v0 should equal v3 so the PC should skip the next instruction 
      vm.cycle();//Should run 0x3118
      assertEquals(0x208, vm.pc); //v1 should equal v4 so the PC should skip the next instruction
      vm.cycle();//Should run 0x32FF
      assertEquals(0x20A, vm.pc);//v2 shouldnt equal v5, so the PC should not skip the next instruction
   }//End method test5XY0
   
   /**
    * Tests the opcode 6XNN.
    * Should assign NN to register vX.
    */
   @Test public void test6XNN(){
      loadOpcode(0x60, 0xFF); //Load opcode 0x60FF in 0x200
      loadOpcode(PC_START + 2, 0x61, 0xBB); //Load opcode 0x61BB in 0x202
      loadOpcode(PC_START + 4, 0x60, 0xAA); 
      vm.cycle();
      assertEquals(0xFF, vm.v[0]);
      vm.cycle();
      assertEquals(0xBB, vm.v[1]);
      vm.cycle();
      assertEquals(0xAA, vm.v[0]);
   }//End method test6XNN
   
   /**
    * Tests the opcode 7XNN
    * Should add NN to vX
    */
   @Test public void test7XNN(){
      loadOpcode(0x70, 0x01); //Load opcode 0x60FF in 0x200
      loadOpcode(PC_START + 2, 0x70, 0x01); //Load opcode 0x61BB in 0x202
      loadOpcode(PC_START + 4, 0x71, 0x02);
      loadOpcode(PC_START + 6, 0x71, 0x02);
      vm.cycle();
      assertEquals(vm.v[0], 0x01);
      vm.cycle();
      assertEquals(vm.v[0], 0x02);
      vm.cycle();
      assertEquals(vm.v[1], 0x02);
      vm.cycle();
      assertEquals(vm.v[1], 0x04);
   }//End method test7XNN
   
   /**
    * Tests the opcode 8XY0
    * Should set the value of vX to be vY
    */
   @Test public void test8XY0(){
      loadOpcode(0x60, 0xFF); //Put 0xFF in v0
      loadOpcode(PC_START + 2, 0x61, 0x11); //Put 0x11 in v1
      vm.cycle();
      assertEquals(0xFF, vm.v[0]);
      vm.cycle();
      assertEquals(0x11, vm.v[1]);
      loadOpcode(PC_START + 4, 0x80, 0x10); //8010 set v0 to be the value of v1
      vm.cycle();
      assertEquals(0x11, vm.v[0]);
      assertEquals(vm.v[0], vm.v[1]);
   }//End method test8XY0
   
   /**
    * Tests the opcode 8XY1
    * Should set the value of vX to be vX OR vY
    */
   @Test public void test8XY1(){
      loadOpcode(0x60, 0xA3); //Put 0xA3 in v0
      loadOpcode(PC_START + 2, 0x61, 0xB2); //Put 0xB2 in v1
      vm.cycle();
      vm.cycle();
      assertEquals(0xA3, vm.v[0]);
      assertEquals(0xB2, vm.v[1]);
      loadOpcode(PC_START + 4, 0x80, 0x11);//Load opcode 0x8011
      vm.cycle();
      assertEquals(0xB3, vm.v[0]); //Assert v0 is 0xB3 (0xA3 | 0xB2)
   }//End method test8XY1
   
   /**
    * Tests the opcode 8XY2
    * Should set the value of vX to be vX AND vY
    */
   @Test public void test8XY2(){
      loadOpcode(0x60, 0xA3); //Put 0xA3 in v0
      loadOpcode(PC_START + 2, 0x61, 0xB2); //Put 0xB2 in v1
      vm.cycle();
      vm.cycle();
      assertEquals(0xA3, vm.v[0]);
      assertEquals(0xB2, vm.v[1]);
      loadOpcode(PC_START + 4, 0x80, 0x12);//Load opcode 0x8012
      vm.cycle();
      assertEquals(0xA2, vm.v[0]); //Assert v0 is 0xA2 (0xA3 & 0xB2)
   }//End method text8XY2
   
   /**
    * Tests the opcode 8XY3
    * Should set the value of vX to be vX XOR vY
    */
   @Test public void test8XY3(){
      loadOpcode(0x60, 0xA3); //Put 0xA3 in v0
      loadOpcode(PC_START + 2, 0x61, 0xB2); //Put 0xB2 in v1
      vm.cycle();
      vm.cycle();
      assertEquals(0xA3, vm.v[0]);
      assertEquals(0xB2, vm.v[1]);
      loadOpcode(PC_START + 4, 0x80, 0x13);//Load opcode 0x8013
      vm.cycle();
      assertEquals(0x11, vm.v[0]); //Assert v0 is 0x11 (0xA3 ^ 0xB2)
   }//End method test8XY3
   
   /**
    * Tests the opcode 8XY4.
    * Should add vY to vX, vF should be set to 1 if theres a carry
    */
   @Test public void test8XY4(){
      loadOpcode(0x60, 0xFF); 
      loadOpcode(PC_START + 2, 0x61, 0x01); 
      loadOpcode(PC_START + 4, 0x80, 0x14);
      vm.cycle();
      vm.cycle();
      assertEquals(0xFF, vm.v[0]);
      assertEquals(0x01, vm.v[1]);
      vm.cycle();
      assertEquals(0x00, vm.v[0]); 
      assertEquals(0x01, vm.v[0xF]); //Assert that there was a carry (255 + 1) should be 0 plus a carry in an 8 bit system.
      
      loadOpcode(PC_START + 6,0x60, 0x0F); 
      loadOpcode(PC_START + 8, 0x61, 0x01);
      loadOpcode(PC_START + 10, 0x80, 0x14);
      vm.cycle();
      vm.cycle();
      assertEquals(0x0F, vm.v[0]);
      assertEquals(0x01, vm.v[1]);
      vm.cycle();
      assertEquals(0x10, vm.v[0]);
      assertEquals(0, vm.v[0xF]);
   }//End method test8xY4
   
   /**
    * Tests the opcode 8XY5
    * Should subtract vY from vX, vF will be set to 1 if theres no borrow.
    */
   @Test public void test8XY5(){
      loadOpcode(0x60, 0xFF);  //Set v0 to 0xFF
      loadOpcode(PC_START + 2, 0x61, 0x01);  //set v1 to 0x01
      loadOpcode(PC_START + 4, 0x80, 0x15); //Load opcode 0x8015
      vm.cycle();
      vm.cycle();
      assertEquals(0xFF, vm.v[0]); //Assert v0 and v1 have been assigned correctly.
      assertEquals(0x01, vm.v[1]);
      vm.cycle(); //Run opcode 0x8015
      assertEquals(0xFE, vm.v[0]);  //Assert 0xFF - 0x01 has been performed correctly.
      assertEquals(0x01, vm.v[0xF]); //Assert there was no borrow (1 = no borrow) 
      
      loadOpcode(PC_START + 6,0x60, 0); //Set v0 to 0 
      loadOpcode(PC_START + 8, 0x61, 1); //Set v1 to 1
      loadOpcode(PC_START + 10, 0x80, 0x15); //Load opcode 0x8015
      vm.cycle();
      vm.cycle();
      assertEquals(0x00, vm.v[0]);//Assert v0 and v1 have been assigned correctly
      assertEquals(0x01, vm.v[1]);
      vm.cycle();
      assertEquals(0xFF, vm.v[0]); //Assert that 0 - 1 rolls back to 255
      assertEquals(0, vm.v[0xF]); //Assert that there was a borrow (0 = borrow)
   }//End method test8XY5
   
   /**
    * Tests the opcode 8XY6
    * Should set vF to the least significant bit of vX, then divide vX by 2
    */
   @Test public void test8XY6(){
      loadOpcode(0x60, 0x8);
      loadOpcode(PC_START + 2, 0x80, 0x16); 
      vm.cycle();
      vm.cycle(); 
      assertEquals(0x04, vm.v[0]);  
      assertEquals(0x00, vm.v[0xF]); 
      
      loadOpcode(PC_START + 4, 0x61, 0x09);
      loadOpcode(PC_START + 6, 0x81, 0x16); 
      vm.cycle();
      vm.cycle(); 
      assertEquals(0x04, vm.v[1]);  
      assertEquals(0x01, vm.v[0xF]); 
   }//End method test8XY6
   
   /**
    * Tests the opcode 8XY7
    * Should set vX to the value of vY minus vX and set vF to 1 if there was no borrow.
    */
   @Test public void test8XY7(){
      loadOpcode(0x60, 0x01);  //Set v0 to 0x1
      loadOpcode(PC_START + 2, 0x61, 0xFF);  //set v1 to 0xFF
      loadOpcode(PC_START + 4, 0x80, 0x17); //Load opcode 0x8017 //v0 = v1 - v0;
      vm.cycle();
      vm.cycle();
      assertEquals(0xFF, vm.v[1]); //Assert v0 and v1 have been assigned correctly.
      assertEquals(0x01, vm.v[0]);
      vm.cycle(); //Run opcode 0x8015
      assertEquals(0xFE, vm.v[0]);  //Assert 0xFF - 0x01 has been performed correctly.
      assertEquals(0x01, vm.v[0xF]); //Assert there was no borrow (1 = no borrow) 
      
      loadOpcode(PC_START + 6,0x60, 1); //Set v0 to 1
      loadOpcode(PC_START + 8, 0x61, 0); //Set v1 to 0
      loadOpcode(PC_START + 10, 0x80, 0x17); //Load opcode 0x8017 (v0 = v1 - v0)
      vm.cycle();
      vm.cycle();
      assertEquals(0x01, vm.v[0]);//Assert v0 and v1 have been assigned correctly
      assertEquals(0x00, vm.v[1]);
      vm.cycle();
      assertEquals(0xFF, vm.v[0]); //Assert that 0 - 1 rolls back to 255
      assertEquals(0, vm.v[0xF]); //Assert that there was a borrow (0 = borrow)
   }//End method 8XY7
   
   /**
    * Tests the opcode 8XYE
    * Should store the most significant bit in vF and multiply vX by 2.
    */
   @Test public void test8XYE(){
      loadOpcode(0x60, 0x4);
      loadOpcode(PC_START + 2, 0x80, 0x0E); 
      vm.cycle();
      vm.cycle(); 
      assertEquals(0x08, vm.v[0]);  
      assertEquals(0x00, vm.v[0xF]); 
      
      loadOpcode(PC_START + 4, 0x61, 0xFF);
      loadOpcode(PC_START + 6, 0x81, 0x0E); 
      vm.cycle();
      vm.cycle(); 
      assertEquals(0xFE, vm.v[1]);  
      assertEquals(0x01, vm.v[0xF]); 
   }//End method test8XYE
   
   /**
    * Tests the opcode 9XY0
    * Should skip the next instruction if vX is not equal to vY
    */
   @Test public void test9XY0(){
      loadOpcode(0x60, 0xFF);
      loadOpcode(PC_START + 2, 0x61, 0xFE);
      loadOpcode(PC_START + 4, 0x90, 0x10);
      vm.cycle();
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 8, vm.pc);
      
      loadOpcode(PC_START + 8, 0x61, 0xFF);
      loadOpcode(PC_START + 10, 0x90, 0x10);
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 12, vm.pc);
   }//End method test9XY0
   
   /**
    * Tests the opcode ANNN
    * Should set the value of the register i to be NNN.
    */
   @Test public void testANNN(){
      loadOpcode(0xAF, 0xFF);
      vm.cycle();
      assertEquals(0xFFF, vm.i);
      
      loadOpcode(PC_START + 2, 0xA0, 0x00);
      vm.cycle();
      assertEquals(0, vm.i);
   }//End method testANNN
   
   /**
    * Tests the opcode BNNN
    * Should set the value of the PC to NNN plus v0
    */
   @Test public void testBNNN(){
      for(int i = 0; i < 100; i++){
         loadOpcode(0x60, 0x05);
         loadOpcode(PC_START + 2,   0xBF, 0xFF);
         vm.cycle();
         vm.cycle();
         assertEquals(0x4, vm.pc);

         loadOpcode(0x4, 0x60, 0x00);
         loadOpcode(0x6, 0xB2, 0x00);
         vm.cycle();
         vm.cycle();
         assertEquals(0x200, vm.pc);
      }//End for
   }//End method testBNNN
   
   /**
    * Tests the opcode CXNN
    * Should produce a random number AND NN and store it in vX
    */
   @Test public void testCXNN(){
      //TODO Difficult to test due to the random nature
   }//End method testCXNN
   
   @Test public void testDXYN(){
      vm.memory.setMemory(0x210, (short)0x7E);
      vm.memory.setMemory(0x211, (short)0xFF);
      loadOpcode(0xA2, 0x10);
      loadOpcode(PC_START + 2, 0xD0, 0x02);
      vm.cycle();
      vm.cycle();
      assertEquals(vm.displayBuffer[0][0], false);
      assertEquals(vm.displayBuffer[1][0], true);
      assertEquals(vm.displayBuffer[2][0], true);
      assertEquals(vm.displayBuffer[3][0], true);
      assertEquals(vm.displayBuffer[4][0], true);
      assertEquals(vm.displayBuffer[5][0], true);
      assertEquals(vm.displayBuffer[6][0], true);
      assertEquals(vm.displayBuffer[7][0], false);
      assertEquals(vm.displayBuffer[0][1], true);
      assertEquals(vm.displayBuffer[1][1], true);
      assertEquals(vm.displayBuffer[2][1], true);
      assertEquals(vm.displayBuffer[3][1], true);
      assertEquals(vm.displayBuffer[4][1], true);
      assertEquals(vm.displayBuffer[5][1], true);
      assertEquals(vm.displayBuffer[6][1], true);
      assertEquals(vm.displayBuffer[7][1], true);
   }//End method testDXYN
   
   /**
    * Tests the opcode EX9E
    * Should skip an instruction if the key stored in vX is pressed
    */
   @Test public void testEX9E(){
      //vm.keyInputs[3] = true;
      loadOpcode(0x63, 0x03);
      loadOpcode(PC_START + 2, 0xE3, 0x9E);
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 6, vm.pc); //TODO: This will fail until Input can be overridden manually.
   //   vm.keyInputs[3] = false;
      loadOpcode(PC_START + 6, 0x64, 0x03);
      loadOpcode(PC_START + 8, 0xE4, 0x9E);
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 10, vm.pc );
   }//End method testEX9E
   
   /**
    * Tests the opcode EXA1
    * Should skip an instruction if the key stored in vX is not pressed
    */
   @Test public void testEXA1(){
      //vm.keyInputs[3] = false;
      //fail();
      loadOpcode(0x63, 0x03);
      loadOpcode(PC_START + 2, 0xE3, 0xA1);
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 6, vm.pc);
      //vm.keyInputs[3] = true;
      loadOpcode(PC_START + 6, 0x64, 0x03);
      loadOpcode(PC_START + 8, 0xE4, 0xA1);
      vm.cycle();
      vm.cycle();
      assertEquals(PC_START + 10, vm.pc ); //TODO This will fail until input can be overridden manually.
   }//End method testEX9E
   
   @Test public void testFX07(){
      vm.delayTimer = 0xFF;
      loadOpcode(PC_START, 0xF0, 0x07);
      vm.cycle();
      assertEquals(vm.v[0], 0xFF);
   }//End method testFX07
   
   @Test public void textFX0A(){
	   //TODO: Test this opcode
   }
   @Test public void testFX15(){
      loadOpcode(0x64, 0xFE);
      loadOpcode(PC_START + 2, 0xF4, 0x15);
      vm.cycle();
      vm.cycle();
      assertEquals(0xFE, vm.delayTimer);
   }//End method testFX15
   
   @Test public void testFX18(){
      loadOpcode(0x64, 0xFE);
      loadOpcode(PC_START + 2, 0xF4, 0x18);
      vm.cycle();
      vm.cycle();
      assertEquals(0xFE, vm.soundTimer);
   }
   
   @Test public void testFX1E(){
      loadOpcode(0x63, 0x02);
      loadOpcode(PC_START + 2, 0xF3, 0x1E);
      vm.i = 3;
      vm.cycle();
      vm.cycle();
      assertEquals(5, vm.i);
   }//End method testFX1E

   
   @Test public void testFX29(){
      loadOpcode(0x60, 0x00);
      loadOpcode(PC_START + 2, 0xf0, 0x29);
      vm.cycle();
      vm.cycle();
      assertEquals(0, vm.i);
      assertEquals(vm.font[0], vm.memory.getValueAt(vm.i));
      loadOpcode(PC_START + 4, 0x61, 0x01);
      vm.cycle();
      loadOpcode(PC_START + 6, 0xf1, 0x29);
      vm.cycle();
      assertEquals(5, vm.i);
      assertEquals(vm.font[5], vm.memory.getValueAt(vm.i));
   }//End method testFX29
   
   
   @Test public void testFX33(){
      fail();
   }
   
   @Test public void testFX55(){
      loadOpcode(0x60, 0x11);
      loadOpcode(PC_START + 2, 0x61, 0x22);
      loadOpcode(PC_START + 4, 0x62, 0x33);
      loadOpcode(PC_START + 6, 0x63, 0x44);
      loadOpcode(PC_START + 8, 0xA2, 0x10);
      loadOpcode(PC_START + 10, 0xF2, 0x55);
      vm.cycle();
      vm.cycle();
      vm.cycle();
      vm.cycle();
      vm.cycle();
      vm.cycle();
      assertEquals(0x11, vm.memory.getValueAt(0x210));
      assertEquals(0x22, vm.memory.getValueAt(0x211));
      assertEquals(0x33, vm.memory.getValueAt(0x212));
      assertEquals(0x00, vm.memory.getValueAt(0x213));
   }//End method testFX55
   
   @Test public void testFX65(){
      vm.memory.setMemory(0x210, (short)0x11);
      vm.memory.setMemory(0x211, (short)0x22);
      vm.memory.setMemory(0x212, (short)0x33);
      loadOpcode(0xA2, 0x10);
      loadOpcode(PC_START + 2, 0xF2, 0x65);
      vm.cycle();
      vm.cycle();
      assertEquals(0x11, vm.v[0]);
      assertEquals(0x22, vm.v[1]);
      assertEquals(0x33, vm.v[2]);
      assertEquals(0x00, vm.v[3]);
   }
}//End class Chip8OpcodeTest
