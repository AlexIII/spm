package spm;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class Clipboard {
    private static Timer wipeTimer = null;

    /**
     * Copies the specified string to the system clipboard and schedules a wipe after the specified number of seconds.
     * @param text The text to copy to the clipboard.
     * @param seconds The number of seconds after which the clipboard should be wiped.
     */
    public static void copyToClipboard(String text, int seconds) {
        copyToClipboard(text);
        scheduleClipboardWipe(seconds);
    }

    /**
     * Copies the specified string to the system clipboard.
     * @param text The text to copy to the clipboard.
     */
    public static void copyToClipboard(String text) {
        cancelWipeTimer();
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(text), null);
    }

    /**
     * Wipes the system clipboard by copying an empty string to it.
     */
    public static void wipeClipboard() {
        copyToClipboard("");
    }

    /**
     * Cancels the scheduled clipboard wipe timer.
     */
    private static void cancelWipeTimer() {
        if (wipeTimer != null) {
            wipeTimer.cancel();
        }
        wipeTimer = null;
    }

    /**
     * Schedules a clipboard wipe after the specified number of seconds.
     * @param seconds The number of seconds after which the clipboard should be wiped.
     */
    private static void scheduleClipboardWipe(int seconds) {
        wipeTimer = new Timer();
        wipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                wipeClipboard();
            }
        }, seconds * 1000);
    }
}
