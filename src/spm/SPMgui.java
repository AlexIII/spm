package spm;

import java.awt.Point;
import javax.swing.ImageIcon;
import java.awt.event.MouseEvent;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.prefs.Preferences;
import java.net.URL;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SPMgui extends javax.swing.JFrame {
    private final String ICON_PATH = "/ico.png";
    private final int PASSWORD_COPY_TIMEOUT_SECONDS = 30;
    private final String PROGRAM_TITLE = "Simple Password Manager";
    private final String VERSION = "v1.5";
    private DataTable dataTable = null;
    private final String[] TABLE_HEADERS = {"Site", "Login", "Comment", "Creation date"};
    private ImageIcon windowIcon;

    public SPMgui() {
        initComponents();
        setMinimumSize(getSize());
        setTitle(PROGRAM_TITLE);
        setWindowIcon();
        setEventListeners();
        initializeDatabase();
        refreshTable();
    }

    private String preferencesGet(String key, String defaultValue) {
        Preferences prefs = Preferences.userNodeForPackage(SPMgui.class);
        return prefs.get(key, defaultValue);
    }
    
    private void preferencesPut(String key, String value) {
        Preferences prefs = Preferences.userNodeForPackage(SPMgui.class);
        prefs.put(key, value);
    }

    /**
     * Sets the window icon.
     */
    private void setWindowIcon() {
        try {
            URL url = SPMgui.class.getResource(ICON_PATH);
            windowIcon = new ImageIcon(url);
            setIconImage(windowIcon.getImage());
            AppDialogs.setWindowIcon(windowIcon.getImage());
        } catch (Exception ex) {
            // Handle exception
        }
    }

    /**
     * Sets the event listeners for the window and filter field.
     */
    private void setEventListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onClose();
            }
        });
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed
            }
        });
    }

    /**
     * Initializes the database.
     */
    private void initializeDatabase() {
        List<ProfileManager.Profile> profiles = ProfileManager.find(".");

        if (profiles.size() == 0) {
            JOptionPane.showMessageDialog(null, "No database files found.", "Warning", JOptionPane.WARNING_MESSAGE);
            AppDialogs.CreateProfileResult newProfile = AppDialogs.promptForCreateProfile();
            if (newProfile == null) System.exit(0);
            String newFile = DataTable.createProfile(ProfileManager.FILE_NAME_PREFIX, newProfile.name, newProfile.hashedPassword);
            profiles = ProfileManager.addAndSort(profiles, newProfile.name, newFile);
        }

        try {
            String lastSelectedProfileFilePath = preferencesGet("lastSelectedProfileFilePath", profiles.get(0).path);
            int selectedProfileIdx = profiles.stream().map(profile -> profile.path).toList().indexOf(lastSelectedProfileFilePath);
            while (true) {
                try {
                    AppDialogs.LoginResult result = AppDialogs.promptForMasterPassword(
                        PROGRAM_TITLE,
                        profiles.stream().map(profile -> profile.name).toList(),
                        selectedProfileIdx
                    );

                    // result == null means the user cancelled the dialog
                    if (result == null) System.exit(0);
                    selectedProfileIdx = result.profileIndex;

                    // result.addProfile != null means the user wants to create a new profile, result.hashedPassword holds the key for the new profile
                    if (result.addProfile != null) {
                        final String newFile = DataTable.createProfile(ProfileManager.FILE_NAME_PREFIX, result.addProfile.name, result.hashedPassword);
                        profiles = ProfileManager.addAndSort(profiles, result.addProfile.name, newFile);
                        final int newProfileIdx = profiles.stream().map(profile -> profile.path).toList().indexOf(newFile);
                        if (newProfileIdx >= 0) selectedProfileIdx = newProfileIdx;
                        continue;
                    }

                    final String selectedDbFile = profiles.get(result.profileIndex).path;
                    // result.renameProfile != null means the user wants to rename the profile
                    if (result.renameProfile != null) {
                        DataTable.renameProfile(selectedDbFile, result.renameProfile);
                        profiles.get(result.profileIndex).name = result.renameProfile;
                        continue;
                    }

                    Entry.encryptionKey = result.hashedPassword;
                    dataTable = new DataTable(selectedDbFile);
                } catch (InvalidKeyException ex) {
                    continue;
                }
                break;
            }
            setTitle(profiles.get(selectedProfileIdx).name + " - " + PROGRAM_TITLE);
            preferencesPut("lastSelectedProfileFilePath", profiles.get(selectedProfileIdx).path);
        } catch (Exception ex) {
            // Print stacktrace to console
            ex.printStackTrace();
            showError(ex.toString());
        }
        showInfo("");
    }

    /**
     * Shows an error message and exits the application.
     * @param errorMessage the error message
     */
    private void showError(final String errorMessage) {
        showError(errorMessage, 1);
    }

    /**
     * Shows an error message and exits the application with the specified exit code.
     * @param errorMessage the error message
     * @param exitCode the exit code
     */
    private void showError(final String errorMessage, int exitCode) {
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(exitCode);
    }

    /**
     * Shows an informational message.
     * @param message the informational message
     */
    private void showInfo(final String message) {
        infoLabel.setText(message);
    }

    /**
     * Updates the filter for the table.
     */
    private void updateFilter() {
        dataTable.setFilterString(filterField.getText());
        refreshTable();
    }

    /**
     * Refreshes the table with the latest data.
     */
    private void refreshTable() {
        Object[][] content = dataTable.toTableArray();
        ((DefaultTableModel) conTable.getModel()).setDataVector(content, TABLE_HEADERS);
    }

    /**
     * Copies the content of a cell to the clipboard.
     * @param col the column index
     * @param row the row index
     */
    private void copyCellContent(final int col, final int row) {
        Object cellContent = conTable.getModel().getValueAt(row, col);
        Clipboard.copyToClipboard(cellContent.toString());
    }

    /**
     * Copies the password of an entry to the clipboard.
     * @param row the row index
     */
    private void copyPassword(final int row) {
        int absoluteId = dataTable.getAbsoluteId(row);
        String password = dataTable.get(absoluteId).getPassword();
        Clipboard.copyToClipboard(password, PASSWORD_COPY_TIMEOUT_SECONDS);
        showInfo("Password for " + dataTable.get(absoluteId).getFormattedName() + " copied to the clipboard");
    }

    /**
     * Deletes an entry from the table.
     * @param row the row index
     */
    private void deleteEntry(final int row) {
        int absoluteId = dataTable.getAbsoluteId(row);
        if (JOptionPane.showConfirmDialog(null, "Delete entry for \"" + dataTable.get(absoluteId).getFormattedName() + "\"?", "Delete entry", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        dataTable.deleteEntry(absoluteId);
        refreshTable();
    }

    /**
     * Edits an entry in the table.
     * @param row the row index
     */
    private void editEntry(final int row) {
        int absoluteId = dataTable.getAbsoluteId(row);
        Entry entry = AppDialogs.promptForEntryEdit(dataTable.get(absoluteId));
        if (entry == null) return;
        dataTable.updateEntry(absoluteId, entry);
        refreshTable();
    }

    /**
     * Adds a new entry to the table.
     */
    private void addNewEntry() {
        Entry entry = AppDialogs.promptForNewEntry();
        if (entry == null) return;
        try {
            dataTable.insertEntry(entry);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Can't create record", "Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTable();
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

    private abstract static class PopUpMouseListener implements java.awt.event.MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    /**
     * Opens a link in the default browser.
     * @param link the link to open
     */
    private void openLink(String link) {
        try {
            if (!link.startsWith("http"))
                link = "http://" + link;
            java.awt.Desktop.getDesktop().browse(new java.net.URI(link));
        } catch (Exception ex) {
            // Handle exception
        }
    }

    /**
     * Displays a popup menu with options for the selected cell.
     * @param evt the mouse event
     * @param col the column index
     * @param row the row index
     */
    private void showPopupMenu(java.awt.event.MouseEvent evt, int col, int row) {
        PopUpMouseListener[] popUpListeners = new PopUpMouseListener[]{
            new PopUpMouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    copyCellContent(col, row);
                }
            },
            new PopUpMouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    copyPassword(row);
                }
            },
            new PopUpMouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    editEntry(row);
                }
            },
            new PopUpMouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    deleteEntry(row);
                }
            },
        };
        String[] popUpNames = new String[]{"Copy", "Copy password", "Edit", "Delete"};

        JPopupMenu menu = new JPopupMenu();
        if (col == 0) {
            String link = (String) conTable.getModel().getValueAt(row, col);
            JMenuItem menuItem = new JMenuItem("Open link");
            menuItem.addMouseListener(new PopUpMouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    openLink(link);
                }
            });
            menu.add(menuItem);
        }

        for (int i = 0; i < popUpNames.length; ++i) {
            JMenuItem menuItem = new JMenuItem(popUpNames[i]);
            menuItem.addMouseListener(popUpListeners[i]);
            menu.add(menuItem);
        }

        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void conTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_conTableMouseReleased
        int col = conTable.columnAtPoint(new Point(evt.getX(), evt.getY()));
        int row = conTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
        if (col == -1 || row == -1) return;
        conTable.changeSelection(row, col, false, false);
        if (SwingUtilities.isRightMouseButton(evt)) showPopupMenu(evt, col, row);
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) copyPassword(row);
    }//GEN-LAST:event_conTableMouseReleased

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addNewEntry();
    }//GEN-LAST:event_addButtonActionPerformed

    private void cmpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmpButtonActionPerformed
        AppDialogs.KeyPair keyPair = AppDialogs.promptForPasswordChange((key) -> dataTable.verifyMasterPassword((byte[]) key) ? "ok" : null);
        if (keyPair == null) return;
        if (dataTable.updateMasterPassword(keyPair.oldKey, keyPair.newKey))
            JOptionPane.showMessageDialog(null, "Master password changed successfully", "Info", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, "Master password change error", "Error", JOptionPane.ERROR_MESSAGE);
        refreshTable();
    }//GEN-LAST:event_cmpButtonActionPerformed

    private void clrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clrButtonActionPerformed
        filterField.setText("");
    }//GEN-LAST:event_clrButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        AppDialogs.showAboutDialog(PROGRAM_TITLE, VERSION, windowIcon);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Handles the window close event.
     */
    private void onClose() {
        Clipboard.wipeClipboard();
    }

    /**
     * Sets the look and feel of the application.
     */
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
            } catch (Exception ex2) {
                // Handle exception
            }
        }
    }

    public static void main(String args[]) {
        setLookAndFeel();
        java.awt.EventQueue.invokeLater(() -> new SPMgui().setVisible(true));
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
