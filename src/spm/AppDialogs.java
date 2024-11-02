/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author Alex
 */
public class AppDialogs {
    public static byte[] passwd(String windowName) {
        Object ret = InputDialog.show(new java.awt.Frame(), windowName,
            "Enter master password", null, InputDialog.FIELD_PASSWORD);
        if(ret == null) return null;
        byte[] key = Crypto.md5((char[])ret);
        disposePasswd((char[])ret);
        return key;
    }
    public static byte[] newPasswd() {
        byte[] key = null;
        Object[] ret = InputDialog.show(new java.awt.Frame(), "Create master password",
            new String[] {"New password", "Confirm new password"}, null,
            new int[] {InputDialog.FIELD_PASSWORD|InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD|InputDialog.FIELD_NOTEMPTY},
            (fields) -> {return Arrays.equals((char[])fields[0], (char[])fields[1])? null : "Entered passwords do not match";}
        );
        if(ret != null) {
            key = Crypto.md5((char[])ret[0]);
            disposePasswd((char[])ret[0]);
            disposePasswd((char[])ret[1]);
        }
        return key;
    }
    public static class KeyPair {
        public byte[] oldKey;
        public byte[] newKey;
        KeyPair(byte[] oldKey, byte[] newKey) {
            this.oldKey = oldKey;
            this.newKey = newKey;
        }
    }    
    public static KeyPair changePasswd(InputDialog.CheckField checkPwd) {
        KeyPair kp = null;
        Object[] ret = InputDialog.show(new java.awt.Frame(), "Change master password",
            new String[] {"Old password", "New password", "Confirm new password"}, null,
            new int[] {InputDialog.FIELD_PASSWORD|InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD|InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD|InputDialog.FIELD_NOTEMPTY},
            (fields) -> {
                String e = null;
                if(!Arrays.equals((char[])fields[1], (char[])fields[2])) 
                    e = "Entered passwords do not match";
                if(checkPwd.check(Crypto.md5((char[])fields[0])) == null)
                    e = "Incorrect old password";
                if(e != null) {
                    disposePasswd((char[])fields[0]);
                    disposePasswd((char[])fields[1]);
                    disposePasswd((char[])fields[2]);
                }
                return e;
            }
        );
        if(ret != null) {
            kp = new KeyPair(Crypto.md5((char[])ret[0]), Crypto.md5((char[])ret[1]));
            disposePasswd((char[])ret[0]);
            disposePasswd((char[])ret[1]);
            disposePasswd((char[])ret[2]);
        }
        return kp;
    }      
    public static Entry newEntry() {
        return manageEntry(null, "Add entry");
    }
    public static Entry changeEntry(Entry e) {
        return manageEntry(e, "Edit entry");
    }
    public static void about(String progTitle, String ver, javax.swing.Icon ic) {
        JOptionPane.showMessageDialog(null, 
                progTitle 
                + " \nVersion: " + ver
                + " \nPublic License: WTFPL v2"
                + " \nhttps://github.com/AlexIII/spm",
                "About", JOptionPane.INFORMATION_MESSAGE, ic);
    }
    
//private methods
    private static Entry manageEntry(Entry e, String title) {
        Object[] ret = InputDialog.show(new java.awt.Frame(), title,
            new String[] {"Site", "Login", "Password", "Comment"},
            e == null? null : new String[] {e.getSite(), e.getLogin(), e.getPassword(), e.getComment()},
            new int[] {InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_PASSWORD|InputDialog.FIELD_GEN_PASSWORD|InputDialog.FIELD_NOTEMPTY, InputDialog.FIELD_NORMAL}
        );
        if(ret == null) return null;
        e = new Entry((String)ret[0], (String)ret[1], new String(((char[])ret[2])), (String)ret[3]);
        disposePasswd((char[])ret[2]);
        return e;
    }  
    
    private static void disposePasswd(char[] pass) {
        Arrays.fill(pass, '\u0000');
    }
}
