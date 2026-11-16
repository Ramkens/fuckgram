package org.telegram.ui;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FuckgramDecoy;
import org.telegram.messenger.FuckgramText;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

/** fuckgram: the panic button. Three quick taps on the logo close the chats and lock the app in the quiet mode. */
public class FuckgramPanic {

    private static long lastTap;
    private static int taps;

    /** Returns true when this tap was the third one and the panic has been triggered. */
    public static boolean tap(BaseFragment fragment) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastTap > 900) {
            taps = 0;
        }
        lastTap = now;
        taps++;
        if (taps < 3) {
            return false;
        }
        taps = 0;
        trigger(fragment);
        return true;
    }

    public static void trigger(BaseFragment fragment) {
        Context context = ApplicationLoader.applicationContext;
        if (SharedConfig.passcodeHash == null || SharedConfig.passcodeHash.length() == 0) {
            // without a passcode there would be no way back from the quiet mode
            Toast.makeText(context, FuckgramText.t("Panic needs a passcode: Settings → Privacy → Passcode", "Для тревожной кнопки нужен код-пароль: Настройки → Конфиденциальность → Код-пароль"), Toast.LENGTH_LONG).show();
            return;
        }
        FuckgramDecoy.setActive(true);
        try {
            NotificationManagerCompat.from(context).cancelAll();
        } catch (Throwable ignore) {
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeChats);
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
        AndroidUtilities.runOnUIThread(() -> {
            if (LaunchActivity.instance != null) {
                LaunchActivity.instance.showPasscodeActivity(false, true, -1, -1, null, null);
            }
        }, 150);
    }
}
