/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spm;

import java.awt.datatransfer.*;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Alex
 */
public class Clipboard {
    static private Timer tmr = null;
    static public void copy(String str, int sec) {
        copy(str);
        tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                copy("");
            }
        }, sec*1000);
    }
    
    static public void copy(String str) {
        cancelTmr();
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(str), null);
    }
    
    static public void wipe() {
        copy("");
    }
    
    static private void cancelTmr() {
       if(tmr != null) tmr.cancel();
       tmr = null; 
    }
}
