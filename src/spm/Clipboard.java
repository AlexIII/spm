package spm;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class Clipboard {
    private static Timer timer = null;

    public static void copy(String str, int seconds) {
        copy(str);
        scheduleWipe(seconds);
    }

    public static void copy(String str) {
        cancelTimer();
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(str), null);
    }

    public static void wipe() {
        copy("");
    }

    private static void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    private static void scheduleWipe(int seconds) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                wipe();
            }
        }, seconds * 1000);
    }
}
