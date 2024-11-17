package spm;

import java.awt.Image;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Utility class for displaying application dialogs.
 */
public class AppDialogs {

    public static void setWindowIcon(Image icon) {
        LoginDialog.setWindowIcon(icon);
    }

    public static class LoginResult extends LoginDialog.Result {
        public final byte[] hashedPassword;
        public LoginResult(LoginDialog.Result result) {
            super(result.profileIndex, result.password, result.renameProfile, result.addProfile);
            this.hashedPassword = Crypto.generateMd5Hash(this.addProfile != null ? this.addProfile.password : result.password);
            if(this.addProfile != null) {
                clearPassword(this.addProfile.password);
            }
            clearPassword(result.password);
        }
    }

    public static class CreateProfileResult extends LoginDialog.AddProfileResult {
        public final byte[] hashedPassword;
        public CreateProfileResult(String name, char[] password) {
            super(name, password);
            this.hashedPassword = Crypto.generateMd5Hash(password);
            clearPassword(password);
        }
    }

    /**
     * Prompts the user to enter the master password.
     * @param windowTitle the title of the window
     * @param profiles the list of profile names to choose from
     * @return the MD5 hash of the entered password, or null if cancelled
     */
    public static LoginResult promptForMasterPassword(String windowTitle, List<String> profiles, int selectedIndex) {
        LoginDialog.Result result = LoginDialog.show(
            new java.awt.Frame(),
            windowTitle,
            profiles,
            selectedIndex
        );
        if (result == null) return null;
        return new LoginResult(result);
    }

    /**
     * Prompts the user to create a new master password.
     * @return the MD5 hash of the new password, or null if cancelled
     */
    public static CreateProfileResult promptForCreateProfile() {
        LoginDialog.AddProfileResult result = LoginDialog.promptNewProfile();
        if (result == null) return null;
        return new CreateProfileResult(result.name, result.password);
    }

    /**
     * Class representing a pair of old and new keys.
     */
    public static class KeyPair {
        public final byte[] oldKey;
        public final byte[] newKey;

        KeyPair(byte[] oldKey, byte[] newKey) {
            this.oldKey = oldKey;
            this.newKey = newKey;
        }
    }

    /**
     * Prompts the user to change the master password.
     * @param validateOldPassword a callback to validate the old password
     * @return a KeyPair containing the old and new keys, or null if cancelled
     */
    public static KeyPair promptForPasswordChange(InputDialog.FieldValidator validateOldPassword) {
        Object[] inputs = InputDialog.show(
            new java.awt.Frame(),
            "Change master password",
            List.of(
                new InputDialog.Field("Old password", InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("New password", InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Confirm new password", InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY)
            ),
            (fields) -> {
                if (!Arrays.equals((char[]) fields[1], (char[]) fields[2])) {
                    clearPassword((char[]) fields[0]);
                    clearPassword((char[]) fields[1]);
                    clearPassword((char[]) fields[2]);
                    return "Entered passwords do not match";
                }
                if (validateOldPassword.validate(Crypto.generateMd5Hash((char[]) fields[0])) == null) {
                    clearPassword((char[]) fields[0]);
                    clearPassword((char[]) fields[1]);
                    clearPassword((char[]) fields[2]);
                    return "Incorrect old password";
                }
                return null;
            }
        );
        if (inputs == null) return null;
        KeyPair keyPair = new KeyPair(Crypto.generateMd5Hash((char[]) inputs[0]), Crypto.generateMd5Hash((char[]) inputs[1]));
        clearPassword((char[]) inputs[0]);
        clearPassword((char[]) inputs[1]);
        clearPassword((char[]) inputs[2]);
        return keyPair;
    }

    /**
     * Prompts the user to add a new entry.
     * @return the new entry, or null if cancelled
     */
    public static Entry promptForNewEntry() {
        return manageEntry(null, "Add entry");
    }

    /**
     * Prompts the user to edit an existing entry.
     * @param entry the entry to edit
     * @return the edited entry, or null if cancelled
     */
    public static Entry promptForEntryEdit(Entry entry) {
        return manageEntry(entry, "Edit entry");
    }

    /**
     * Displays an "About" dialog with program information.
     * @param programTitle the program title
     * @param version the program version
     * @param icon the icon to display
     */
    public static void showAboutDialog(String programTitle, String version, javax.swing.Icon icon) {
        JOptionPane.showMessageDialog(null,
            programTitle + " \nVersion: " + version
            + " \nPublic License: WTFPL v2"
            + " \nhttps://github.com/AlexIII/spm",
            "About", JOptionPane.INFORMATION_MESSAGE, icon);
    }

    // Private methods

    /**
     * Manages the creation or editing of an entry.
     * @param entry the entry to edit, or null to create a new entry
     * @param dialogTitle the dialog title
     * @return the new or edited entry, or null if cancelled
     */
    private static Entry manageEntry(Entry entry, String dialogTitle) {
        Object[] inputs = InputDialog.show(
            new java.awt.Frame(),
            dialogTitle,
            List.of(
                new InputDialog.Field("Site", entry == null ? "" : entry.getWebsite(), InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Login", entry == null ? "" : entry.getUsername(), InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Password", entry == null ? "" : entry.getPassword(), InputDialog.FIELD_PASSWORD | InputDialog.FIELD_GEN_PASSWORD | InputDialog.FIELD_NOTEMPTY),
                new InputDialog.Field("Comment", entry == null ? "" : entry.getNotes(), InputDialog.FIELD_NORMAL)
            )
        );
        if (inputs == null) return null;
        entry = new Entry((String) inputs[0], (String) inputs[1], new String(((char[]) inputs[2])), (String) inputs[3]);
        clearPassword((char[]) inputs[2]);
        return entry;
    }

    /**
     * Clears a password by overwriting its characters.
     * @param password the password to clear
     */
    private static void clearPassword(char[] password) {
        Arrays.fill(password, '\u0000');
    }
}
