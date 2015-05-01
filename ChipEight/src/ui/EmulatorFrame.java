package ui;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import vm.Chip8VM;
import core.event.EventManager;
import core.input.InputManager;
import event.DebugStepEvent;
import event.GameInitialisedEvent;
import event.LoadRomEvent;
import event.RomLoadedEvent;
import event.ToggleDebugEvent;

import java.awt.Canvas;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class EmulatorFrame extends JFrame{
   private static final long serialVersionUID = -268081430672847435L;
   
   private Canvas canvas;
   private JMenuItem mntmOpen;
   private Chip8VM vm;

   private JMenuItem mntmShowDebug;
   private JMenuItem mntmPause;
   private JMenuItem mntmSkip;

   /**
    * Constructs a new EmulatorFrame
    */
   public EmulatorFrame(Chip8VM vm) {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle(Display.getTitle());
      initialiseComponents();
      registerSubscriptions();
      this.vm = vm;
   }//End constructor
   
   /**
    * Initialises the swing component for this thread.
    */
   public void initialiseComponents(){
      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);
      
      JMenu mnFile = new JMenu("File");
      menuBar.add(mnFile);
      
      mntmOpen = new JMenuItem("Load Rom");
      

      mnFile.add(mntmOpen);
      
      JMenu mnDebug = new JMenu("Debug");
      menuBar.add(mnDebug);
      
      mntmShowDebug = new JMenuItem("Show Debug");
      mnDebug.add(mntmShowDebug);
      
      mntmPause = new JMenuItem("Pause Emulation");
      mnDebug.add(mntmPause);
      
      mntmSkip = new JMenuItem("Single Cycle Step");
      mnDebug.add(mntmSkip);
      
      canvas = new Canvas();
      canvas.setSize(new Dimension(340, 320));
      canvas.setMinimumSize(new Dimension(640, 320));
      canvas.setPreferredSize(new Dimension(640, 320));
      getContentPane().add(canvas, BorderLayout.CENTER);
   }//End method initialiseComponents
   
   /**
    * Registers event subscriptions and keyboard shortcuts for this frame
    */
   public void registerSubscriptions(){
      EventManager event = EventManager.getEventManager();
      InputManager input = InputManager.getInputManager();
      event.registerFor(GameInitialisedEvent.class, (e)->setDisplayToCanvas());
      event.registerFor(RomLoadedEvent.class, ((e)->setTitle(((RomLoadedEvent)e).getRomName())));
      
      mntmOpen.addActionListener((ae)->loadRom());
      mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
      input.addKeyboardShorcut(Keyboard.KEY_LCONTROL, Keyboard.KEY_O, (e,i)->loadRom());
      
      mntmShowDebug.addActionListener((ae)->new DebugFrame(vm).setVisible(true));
      mntmShowDebug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
      input.addKeyboardShorcut(Keyboard.KEY_LCONTROL, Keyboard.KEY_D, (e,i)->mntmShowDebug.doClick());
      
      mntmPause.addActionListener(ae->EventManager.getEventManager().fireEvent(new ToggleDebugEvent()));
      mntmPause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
      input.addKeyboardShorcut(Keyboard.KEY_LCONTROL, Keyboard.KEY_P, (e,i)->mntmPause.doClick());
      
      
      mntmSkip.addActionListener(ae->EventManager.getEventManager().fireEvent(new DebugStepEvent()));
      mntmSkip.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
      input.addKeyboardShorcut(0, Keyboard.KEY_F6, (e,i)->mntmSkip.doClick());
   }//End method registerSubscriptions
   
   /**
    * Sets the LWJGL Display's parent to the canvas in this frame.
    */
   private void setDisplayToCanvas(){
      try {
         Display.setParent(canvas);
         pack();
         setLocationRelativeTo(null);
      } catch (LWJGLException e) {
         e.printStackTrace();
      }//End try/catch
   }//End method setDisplayToCanvas
   
   /**
    * Opens a file chooser for roms and notifies an event to load the selected file
    */
   private void loadRom(){
      SwingUtilities.invokeLater(new Runnable() {
         @Override public void run() {
            JFileChooser chooser = new JFileChooser();   
            if(chooser.showOpenDialog(EmulatorFrame.this) == JFileChooser.APPROVE_OPTION){
               File file = chooser.getSelectedFile();
               EventManager.getEventManager().fireEvent(new LoadRomEvent(file));
            }//End if
         }//End method run
      });
   }//End method loadRom
   
}//End class EmulatorFrame
