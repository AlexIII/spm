package spm;

import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 * Utility class for displaying application dialogs.
 */
public class AppDialogs {

    /**
     * Prompts the user to enter the master password.
     * @param windowName the name of the window
     * @return the MD5 hash of the entered password, or null if cancelled
     */
    public static byte[] passwd(String windowName) {
        Object ret = InputDialog.show(new java.awt.Frame(), windowName,
            "Enter master password", null, InputDialog.FIELD_PASSWORD);
        if (ret == null) return null;
        byte[] key = Crypto.generateMd5Hash((char[]) ret);
        disposePasswd((char[]) ret);
        return key;
    }

    /**
     * Prompts the user to create a new master password.
     * @return the MD5 hash of the new password, or null if cancelled
     */
    public static byte[] newPasswd() {
        Object[] ret = InputDialog.show(new java.awt.Frame(), "Create master password",
            new String[] {"New password", "Confirm new password"}, null,
            new int[] {InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY},
            (fields) -> Arrays.equals((char[]) fields[0], (char[]) fields[1]) ? null : "Entered passwords do not match"
        );
        if (ret == null) return null;
        byte[] key = Crypto.generateMd5Hash((char[]) ret[0]);
        disposePasswd((char[]) ret[0]);
        disposePasswd((char[]) ret[1]);
        return key;
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
     * @param checkPwd a callback to check the old password
     * @return a KeyPair containing the old and new keys, or null if cancelled
     */
    public static KeyPair changePasswd(InputDialog.CheckField checkPwd) {
        Object[] ret = InputDialog.show(new java.awt.Frame(), "Change master password",
            new String[] {"Old password", "New password", "Confirm new password"}, null,
            new int[] {InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD | InputDialog.FIELD_NOTEMPTY},
            (fields) -> {
                if (!Arrays.equals((char[]) fields[1], (char[]) fields[2])) {
                    disposePasswd((char[]) fields[0]);
                    disposePasswd((char[]) fields[1]);
                    disposePasswd((char[]) fields[2]);
                    return "Entered passwords do not match";
                }
                if (checkPwd.check(Crypto.generateMd5Hash((char[]) fields[0])) == null) {
                    disposePasswd((char[]) fields[0]);
                    disposePasswd((char[]) fields[1]);
                    disposePasswd((char[]) fields[2]);
                    return "Incorrect old password";
                }
                return null;
            }
        );
        if (ret == null) return null;
        KeyPair kp = new KeyPair(Crypto.generateMd5Hash((char[]) ret[0]), Crypto.generateMd5Hash((char[]) ret[1]));
        disposePasswd((char[]) ret[0]);
        disposePasswd((char[]) ret[1]);
        disposePasswd((char[]) ret[2]);
        return kp;
    }

    /**
     * Prompts the user to add a new entry.
     * @return the new entry, or null if cancelled
     */
    public static Entry newEntry() {
        return manageEntry(null, "Add entry");
    }

    /**
     * Prompts the user to edit an existing entry.
     * @param e the entry to edit
     * @return the edited entry, or null if cancelled
     */
    public static Entry changeEntry(Entry e) {
        return manageEntry(e, "Edit entry");
    }

    /**
     * Displays an "About" dialog with program information.
     * @param progTitle the program title
     * @param ver the program version
     * @param ic the icon to display
     */
    public static void about(String progTitle, String ver, javax.swing.Icon ic) {
        JOptionPane.showMessageDialog(null,
            progTitle + " \nVersion: " + ver
            + " \nPublic License: WTFPL v2"
            + " \nhttps://github.com/AlexIII/spm",
            "About", JOptionPane.INFORMATION_MESSAGE, ic);
    }

    // Private methods

    /**
     * Manages the creation or editing of an entry.
     * @param e the entry to edit, or null to create a new entry
     * @param title the dialog title
     * @return the new or edited entry, or null if cancelled
     */
    private static Entry manageEntry(Entry e, String title) {
        Object[] ret = InputDialog.show(new java.awt.Frame(), title,
            new String[] {"Site", "Login", "Password", "Comment"},
            e == null ? null : new String[] {e.getSite(), e.getLogin(), e.getPassword(), e.getComment()},
            new int[] {InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD | InputDialog.FIELD_GEN_PASSWORD | InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_NORMAL}
        );
        if (ret == null) return null;
        e = new Entry((String) ret[0], (String) ret[1], new String(((char[]) ret[2])), (String) ret[3]);
        disposePasswd((char[]) ret[2]);
        return e;
    }

    /**
     * Disposes of a password by overwriting its characters.
     * @param pass the password to dispose of
     */
    private static void disposePasswd(char[] pass) {
        Arrays.fill(pass, '\u0000');
    }
}
