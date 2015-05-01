package ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;


import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import core.event.EventManager;

import javax.swing.JMenu;

import vm.Chip8Memory;
import vm.Chip8VM;
import vm.Chip8Memory.Change;
import event.CycleCompleteEvent;
import event.DebugStepEvent;
import event.ToggleDebugEvent;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class DebugFrame extends JFrame {
   private static final long serialVersionUID = -395547754627517058L;
   private JTable memoryTable;
   private JTable registerTable;
   private JMenuBar menuBar;
   private JMenu mntmdebug;
   private JMenuItem mntmToggleDebug;
   private JMenuItem mntmStepForward;
   private int pc = 0x200;
   private Chip8VM vm;

   private class CellRenderer extends DefaultTableCellRenderer{
      private static final long serialVersionUID = 4113262082580460652L;
      
      private CellRenderer() {
         super();
         setHorizontalAlignment(JLabel.CENTER);
      }
      
      @Override public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         
         Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         int x = pc % 16 + 1;
         int y = (int) Math.floor(pc / 16);
         int x1 = (pc + 1) % 16 + 1;
         int y1 = (int) Math.floor((pc + 1)/ 16) ;
         if((x == column && y == row) || (x1 == column && y1 == row)){
            cell.setBackground(Color.CYAN);
         } else if(!isSelected){
            cell.setBackground(table.getBackground());
         }//End if
         return cell;
      }//End method getTableCellRendererComponent
   }//End class CellRenderer

   public DebugFrame(Chip8VM vm) {
      setTitle("Chip 8 Debug");
      this.vm = vm;
      initialiseComponents();
      setMem(vm.getMemory());
      EventManager.getEventManager().registerFor(CycleCompleteEvent.class, (e)->updateViews(vm));
   }

   private void initialiseComponents() {
      GridBagLayout gridBagLayout = new GridBagLayout();
      gridBagLayout.columnWidths = new int[] {0, 0};
      gridBagLayout.columnWeights = new double[]{0.0};
      gridBagLayout.rowWeights = new double[]{0.0, 0.0};
      getContentPane().setLayout(gridBagLayout);

      JScrollPane scrollPane_1 = new JScrollPane();
      GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
      gbc_scrollPane_1.weightx = 1.0;
      gbc_scrollPane_1.weighty = 0.1;
      gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
      gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
      gbc_scrollPane_1.gridx = 0;
      gbc_scrollPane_1.gridy = 0;
      getContentPane().add(scrollPane_1, gbc_scrollPane_1);

      registerTable = new JTable();
      registerTable.setModel(new DefaultTableModel(
            new Object[][] {
                  {"00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00", "00"},
                  {"0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"}
            },
            new String[] {
                  "v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "vA", "vB", "vC", "vD", "vE", "vF", "I", "Delay"
            }
            ));
      registerTable.setFillsViewportHeight(true);
      scrollPane_1.setViewportView(registerTable);

      JScrollPane scrollPane = new JScrollPane();
      GridBagConstraints gbc_scrollPane = new GridBagConstraints();
      gbc_scrollPane.weightx = 1.0;
      gbc_scrollPane.weighty = 0.8;
      gbc_scrollPane.fill = GridBagConstraints.BOTH;
      gbc_scrollPane.gridx = 0;
      gbc_scrollPane.gridy = 1;
      getContentPane().add(scrollPane, gbc_scrollPane);

      memoryTable = new JTable();
      memoryTable.setDefaultRenderer(Object.class, new CellRenderer());
      memoryTable.setCellSelectionEnabled(true);
      memoryTable.setShowGrid(false);
      memoryTable.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] {
                  "v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "vA", "vB", "vC", "vD", "vE", "vF", "I"
            }
            ));
      memoryTable.setFillsViewportHeight(true);
      scrollPane.setViewportView(memoryTable);
      setSize(800, 600);

      menuBar = new JMenuBar();
      setJMenuBar(menuBar);

      mntmdebug = new JMenu("Debug");
      menuBar.add(mntmdebug);

      mntmToggleDebug = new JMenuItem("Pause Emulation");
      mntmdebug.add(mntmToggleDebug);
      mntmToggleDebug.addActionListener(ae->EventManager.getEventManager().fireEvent(new ToggleDebugEvent()));
      mntmToggleDebug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P , Event.CTRL_MASK));

      mntmStepForward = new JMenuItem("Step Forward");
      mntmdebug.add(mntmStepForward);
      mntmStepForward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
      mntmStepForward.addActionListener(ae->EventManager.getEventManager().fireEvent(new DebugStepEvent()));
   }

   public void setMem(Chip8Memory mem){
      for(int i = 0; i < mem.getMemorySize(); ){
         String[] data = new String[17];
         data[0] = Integer.toHexString(i &0xFFFF0).toUpperCase();
         for(int x = 1; x < data.length && i < mem.getMemorySize(); x++){
            String str = Integer.toHexString(mem.getValueAt(i)).toUpperCase();
            String formatted = ("00" + str).substring(str.length());
            data[x] = formatted;
            i++;
         }
         ((DefaultTableModel)memoryTable.getModel()).addRow(data);
      }
      updateRegisterView(vm);
      repaint();
   }

   public void updateViews(Chip8VM vm){
      updateRegisterView(vm);
      updateMemoryView(vm);
   }

   private void updateMemoryView(Chip8VM vm) {
      for(Change change : vm.getMemory().getChanges()){
         int x = change.getChangeLocation() % 16 + 1;
         int y = (int) Math.floor(change.getChangeLocation() / 16);
         String str = Integer.toHexString(change.getNewValue()).toUpperCase();
         String formatted = ("00" + str).substring(str.length());
         ((DefaultTableModel)memoryTable.getModel()).setValueAt(formatted, y, x);
      }
      if(vm.getProgramCounter() != this.pc){
         this.pc = vm.getProgramCounter();
         memoryTable.repaint();
      }
   }

   private void updateRegisterView(Chip8VM vm) {
      DefaultTableModel model = (DefaultTableModel)registerTable.getModel();
      for(int i = 0; i < 0xF; i++){
         model.setValueAt(Integer.toHexString(vm.getRegisterValue(i)).toUpperCase(), 0, i);
         model.setValueAt(vm.getRegisterValue(i), 1, i);
         registerTable.repaint();
      }
      model.setValueAt(Integer.toHexString(vm.getAddressRegister()).toUpperCase(), 0, 16);
      model.setValueAt(vm.getAddressRegister(), 1, 16);
      model.setValueAt(Integer.toHexString(vm.getDelayTimer()), 0, 17);
      model.setValueAt(vm.getDelayTimer(), 1, 17);
   }
}
