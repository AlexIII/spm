package spm;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class LoginDialog extends Dialog {
    private JLabel profileLabel = new JLabel("Profile");
    private JComboBox<String> profileComboBox = new JComboBox<String>();
    private JButton renameProfileButton = new JButton("✎");
    private JButton newProfileButton = new JButton("+");
    private JLabel passwordLabel = new JLabel("Enter master password");
    private JPasswordField passwordField = new JPasswordField();
    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");

    private boolean isConfirmed = false;
    public String renameProfile = null;
    public AddProfileResult addProfile = null;

    private static Image windowIcon = null;
    public static void setWindowIcon(Image icon) {
        windowIcon = icon;
    }

    static class AddProfileResult {
        public final String name;
        protected final char[] password;
        public AddProfileResult(String name, char[] password) {
            this.name = name;
            this.password = password;
        }
    }

    static class Result {
        public final int profileIndex;
        public final String renameProfile;
        public final AddProfileResult addProfile;
        protected final char[] password;
        public Result(int profileIndex, char[] password, String renameProfile, AddProfileResult addProfile) {
            this.profileIndex = profileIndex;
            this.password = password;
            this.renameProfile = renameProfile;
            this.addProfile = addProfile;
        }
    }

    public static Result show(Frame parent, String title, Collection<String> profiles, int selectedIndex) {
        final LoginDialog dialog = new LoginDialog(parent, title, profiles, selectedIndex);
        dialog.setVisible(true);
        final Result result = dialog.isConfirmed
            ? new Result(dialog.profileComboBox.getSelectedIndex(), dialog.passwordField.getPassword(), dialog.renameProfile, dialog.addProfile)
            : null;
        dialog.dispose();
        return result;
    }

    private LoginDialog(Frame parent, String title, Collection<String> profiles, int selectedIndex) {
        super(parent, true);
        profileComboBox.setModel(new DefaultComboBoxModel<>(profiles.toArray(new String[0])));
        if(selectedIndex >= 0 && selectedIndex < profiles.size()) profileComboBox.setSelectedIndex(selectedIndex);
        initializeComponents(title);
    }

    public static String promptChangeProfileName(String oldName) {
        return (String)InputDialog.show(
            new java.awt.Frame(),
            "Rename profile",
            new InputDialog.Field("Reaname " + oldName + " to", InputDialog.FIELD_NOTEMPTY)
        );
    }

    public static AddProfileResult promptNewProfile() {
        Object[] result = InputDialog.show(
            new java.awt.Frame(),
            "Create new profile",
            List.of(
                new InputDialog.Field("New profile name", InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Master password", InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Confirm master password", InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY)
            ),
            fields -> Arrays.equals((char[]) fields[1], (char[]) fields[2]) ? null : "Entered passwords do not match"
        );
        if(result == null) {
            return null;
        }
        return new AddProfileResult((String)result[0], (char[])result[1]);
    }

    private void initializeComponents(String title) {
        setResizable(false);
        setTitle(title);
        setIconImage(windowIcon);

        passwordField.setColumns(25);
        Insets buttonMargin = renameProfileButton.getMargin();
        renameProfileButton.setMargin(new Insets(buttonMargin.top, 5, buttonMargin.bottom, 5));
        renameProfileButton.setToolTipText("Rename profile");
        newProfileButton.setMargin(new Insets(buttonMargin.top, 5, buttonMargin.bottom, 5));
        newProfileButton.setToolTipText("Create new profile");

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(false);
            }
        });

        cancelButton.addActionListener(evt -> closeDialog(false));
        okButton.addActionListener(evt -> closeDialog(true));
        passwordField.addActionListener(evt -> closeDialog(true));
        renameProfileButton.addActionListener(evt -> {
            renameProfile = promptChangeProfileName((String)profileComboBox.getSelectedItem());
            if (renameProfile == null) {
                return;
            }
            closeDialog(true);
        });
        newProfileButton.addActionListener(evt -> {
            addProfile = promptNewProfile();
            if (addProfile == null) {
                return;
            }
            closeDialog(true);
        });

        setLayout(createLayout());
        pack();
        setLocationRelativeTo(null);
        passwordField.requestFocus();
    }

    private GroupLayout createLayout() {
        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(profileLabel)
                    .addComponent(profileComboBox)
                    .addComponent(renameProfileButton)
                    .addComponent(newProfileButton))
                .addComponent(passwordLabel)
                .addComponent(passwordField)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(profileLabel)
                    .addComponent(profileComboBox)
                    .addComponent(renameProfileButton)
                    .addComponent(newProfileButton))
                .addComponent(passwordLabel)
                .addComponent(passwordField)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );

        return layout;
    }

    private void closeDialog(boolean ok) {
        isConfirmed = ok;
        setVisible(false);
    }
}
