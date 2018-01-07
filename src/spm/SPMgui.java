/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import java.awt.Point;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;
import java.security.InvalidKeyException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.net.URL;

/**
 *
 * @author Alex
 */
public class SPMgui extends javax.swing.JFrame {
    private final String iconPath = "/ico.png";
    private final String dbPath = "spmdb.xml";
    private final int passwordCopyTimeoutSec = 30;
    
    private final String progTitle = "Simple Password Manager";
    private final String version = "v1.3";
    
    private DataTable dt = null;
    private final String[] rowHeader = {"Site","Login","Comment","Creation date"};
    private ImageIcon icon; 
    
    //Creates new form SPMgui
    public SPMgui() {
        //init main window
        initComponents();
        setMinimumSize(getSize());
        setTitle(progTitle);
        
        //set window icon
        try {
            URL url = SPMgui.class.getResource(iconPath);
            icon = new ImageIcon(url);
            setIconImage(icon.getImage());
        } catch (Exception ex) {} 
        
        //set listeners
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {close();}
        });
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {updateFilter();}
            @Override public void removeUpdate(DocumentEvent e) {updateFilter();}
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        
        //check if DB is present and create a new one if not
        if(!testFile(dbPath)) {
            JOptionPane.showMessageDialog(null, 
                "DataBase file \"" + dbPath + "\" not found.",
            "Warning", JOptionPane.WARNING_MESSAGE);
            byte[] key = AppDialogs.newPasswd();
            if(key == null) System.exit(0);
            DataTable.createDB(dbPath, key);
        }
        
        //get master password, init DB
        try {
            InvalidKeyException errPass = null;
            do {
                errPass = null;
                try {
                    Entry.key = AppDialogs.passwd(progTitle);
                    if(Entry.key == null) System.exit(0);
                    dt = new DataTable(dbPath);
                } catch (InvalidKeyException ex) {
                    errPass = ex;
                }
            } while(errPass != null);
        } catch (Exception ex) {
            error(ex.toString());
        }
        info("");
         
        updateTable();
    }
    
    //general methods
    private boolean testFile(String fname) {
        File f = new File(fname);
        return f.exists() && f.isFile();
    }
    private void error(final String err) {
        error(err, 1);
    }
    private void error(final String err, int exitCode) {
        JOptionPane.showMessageDialog(null, err, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(exitCode);
    }
    
    //gui-specific methods
    private void info(final String str) {
        infoLabel.setText(str);
    }
    private void updateFilter() {
        dt.setFilter(filterField.getText());
        updateTable();
    }
    private void updateTable() {
        Object[][] con = dt.toTableArray();
        ((DefaultTableModel)conTable.getModel()).setDataVector(con, rowHeader);
    }
    private void copyCell(final int col, final int row) {
        Object cb = conTable.getModel().getValueAt(row, col);
        Clipboard.copy(cb.toString());
    }
    private void copyPass(final int row) {
        int absId = dt.getAbsId(row);
        String cb = dt.get(absId).getPassword();
        Clipboard.copy(cb, passwordCopyTimeoutSec);
        info("Password for "+dt.get(absId).name()+" copied to the clipboard");
    }
    private void deleteEntry(final int row) {
        int absId = dt.getAbsId(row);
        if(JOptionPane.showConfirmDialog(null, 
            "Delete entry for \"" + dt.get(absId).name() + "\"?", 
            "Delete entry", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        dt.removeEntry(absId);
        updateTable();
    }
    private void editEntry(final int row) {
        int absId = dt.getAbsId(row);
        Entry e = AppDialogs.changeEntry(dt.get(absId));
        if(e == null) return;
        dt.replaceEntry(absId, e);
        updateTable();
    }
    private void newEntry() {
        Entry e = AppDialogs.newEntry();
        if(e == null) return;
        try {
            dt.addEntry(e);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "Can't create record",
            "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateTable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        conTable = new javax.swing.JTable();
        infoLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cmpButton = new javax.swing.JButton();
        filterField = new javax.swing.JTextField();
        clrButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 450));

        conTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Site", "Login", "Comment", "Last accessed"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        conTable.setColumnSelectionAllowed(true);
        conTable.getTableHeader().setReorderingAllowed(false);
        conTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                conTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(conTable);
        conTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (conTable.getColumnModel().getColumnCount() > 0) {
            conTable.getColumnModel().getColumn(0).setPreferredWidth(120);
            conTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            conTable.getColumnModel().getColumn(2).setPreferredWidth(250);
            conTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        }

        infoLabel.setText("info");

        addButton.setText("New Entry");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Filter:");

        cmpButton.setText("Change master password");
        cmpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmpButtonActionPerformed(evt);
            }
        });

        filterField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        filterField.setMaximumSize(new java.awt.Dimension(2147483647, 22));

        clrButton.setText("X");
        clrButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        clrButton.setPreferredSize(new java.awt.Dimension(28, 22));
        clrButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clrButtonActionPerformed(evt);
            }
        });

        jButton1.setText("About");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(28, 28, 28)
                        .addComponent(cmpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filterField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(clrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(addButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmpButton)
                    .addComponent(infoLabel)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //listener methods
    private abstract static class PopUpMouseListener implements java.awt.event.MouseListener {
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
    private void openLink(String l) {
        try {
            if(!l.startsWith("http"))
                l = "http://" + l;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(l));
        } catch(Exception ex){}
    }
    private void PopUpMenu(java.awt.event.MouseEvent evt, int col, int row) {
        PopUpMouseListener[] popUpListeners = new PopUpMouseListener[] {
            new PopUpMouseListener() {
                @Override public void mouseReleased(MouseEvent e) {copyCell(col, row);}
            },
            new PopUpMouseListener() {
                @Override public void mouseReleased(MouseEvent e) {copyPass(row);}
            },
            new PopUpMouseListener() {
                @Override public void mouseReleased(MouseEvent e) {editEntry(row);}
            },
            new PopUpMouseListener() {
                @Override public void mouseReleased(MouseEvent e) {deleteEntry(row);}
            },            
        };
        String[] popUpNames = new String[] {"Copy", "Copy password", "Edit", "Delete"};
        
        JPopupMenu menu = new JPopupMenu();
        if(col == 0) {
            String l = (String)conTable.getModel().getValueAt(row, col);
            JMenuItem it = new JMenuItem("Open link");
            it.addMouseListener(new PopUpMouseListener() {
                @Override public void mouseReleased(MouseEvent e) {openLink(l);}
            });
            menu.add(it);
        }        
        
        for(int i = 0; i < popUpNames.length; ++i) {
            JMenuItem it = new JMenuItem(popUpNames[i]);
            it.addMouseListener(popUpListeners[i]);
            menu.add(it);
        }
        
        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
    private void conTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conTableMouseReleased
        int col = conTable.columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row = conTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if(col == -1 || row == -1) return;
        conTable.changeSelection(row, col, false, false);
        if(SwingUtilities.isRightMouseButton(evt)) PopUpMenu(evt, col, row);
        if(SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) copyPass(row);
    }//GEN-LAST:event_conTableMouseReleased
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        newEntry();
    }//GEN-LAST:event_addButtonActionPerformed
    private void cmpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmpButtonActionPerformed
        AppDialogs.KeyPair kp = AppDialogs.changePasswd((key)->{return dt.checkMasterPass((byte[])key)? "ok" : null;});
        if(kp == null) return;
        if(dt.changeMasterPass(kp.oldKey, kp.newKey))
            JOptionPane.showMessageDialog(null, "Password change ok",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        else JOptionPane.showMessageDialog(null, "Password change error",
                "Error", JOptionPane.ERROR_MESSAGE);
        updateTable();
    }//GEN-LAST:event_cmpButtonActionPerformed
    private void clrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrButtonActionPerformed
        filterField.setText("");
    }//GEN-LAST:event_clrButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        AppDialogs.about(progTitle, version, icon);
    }//GEN-LAST:event_jButton1ActionPerformed
    private void close() {
        Clipboard.wipe();
    }

    //static methods    
    public static void setLookAndFeel() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
            } catch (Exception ex2) {}       
        }
    }
    public static void main(String args[]) {
        setLookAndFeel();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SPMgui().setVisible(true);
            }
        });
    } 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton clrButton;
    private javax.swing.JButton cmpButton;
    private javax.swing.JTable conTable;
    private javax.swing.JTextField filterField;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
