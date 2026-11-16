package org.telegram.ui;

import android.content.Context;
import android.os.SystemClock;

import androidx.core.app.NotificationManagerCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FuckgramDecoy;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

/** fuckgram: two quick taps on the logo switch between the main and the second space. */
public class FuckgramPanic {

    private static long lastTap;
    private static int taps;

    /** Returns true when this tap was the third one and the panic has been triggered. */
    public static boolean tap(BaseFragment fragment) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastTap > 700) {
            taps = 0;
        }
        lastTap = now;
        taps++;
        if (taps < 2) {
            return false;
        }
        taps = 0;
        trigger(fragment);
        return true;
    }

    /** Switches between the main and the second space without asking for anything. */
    public static void trigger(BaseFragment fragment) {
        Context context = ApplicationLoader.applicationContext;
        boolean quiet = !FuckgramDecoy.isActive();
        FuckgramDecoy.setActive(quiet);
        if (quiet) {
            try {
                NotificationManagerCompat.from(context).cancelAll();
            } catch (Throwable ignore) {
            }
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeChats);
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
        try {
            if (fragment != null && fragment.getFragmentView() != null) {
                fragment.getFragmentView().performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
            }
        } catch (Throwable ignore) {
        }
    }
}
