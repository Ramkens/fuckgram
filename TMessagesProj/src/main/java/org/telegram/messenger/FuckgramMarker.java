package org.telegram.messenger;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;

/**
 * fuckgram: lets fuckgram clients recognise each other. Invisible characters are appended to the last name; other
 * fuckgram clients that see them draw the fuckgram badge. Regular clients just don't see anything.
 */
public class FuckgramMarker {

    public static final String MARK = "⁣⁤⁣";

    public static boolean isMarked(TLRPC.User user) {
        return user != null && user.last_name != null && user.last_name.endsWith(MARK);
    }

    /** Hides the marker when a name is shown as text. */
    public static String strip(String s) {
        if (s != null && s.endsWith(MARK)) {
            return s.substring(0, s.length() - MARK.length());
        }
        return s;
    }

    private static final boolean[] tried = new boolean[UserConfig.MAX_ACCOUNT_COUNT];

    /** Adds the marker to the own account name once per app start, if it is missing. */
    public static void ensure(int account) {
        try {
            if (tried[account] || !UserConfig.getInstance(account).isClientActivated()) {
                return;
            }
            TLRPC.User self = UserConfig.getInstance(account).getCurrentUser();
            if (self == null || isMarked(self)) {
                tried[account] = true;
                return;
            }
            String last = self.last_name == null ? "" : self.last_name;
            if (last.length() + MARK.length() > 64) {
                tried[account] = true;
                return;
            }
            tried[account] = true;
            TL_account.updateProfile req = new TL_account.updateProfile();
            req.flags = 2;
            req.last_name = last + MARK;
            ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> {
                if (response instanceof TLRPC.User) {
                    AndroidUtilities.runOnUIThread(() -> {
                        TLRPC.User user = (TLRPC.User) response;
                        MessagesController.getInstance(account).putUser(user, false);
                        UserConfig.getInstance(account).setCurrentUser(user);
                        UserConfig.getInstance(account).saveConfig(true);
                        NotificationCenter.getInstance(account).postNotificationName(NotificationCenter.mainUserInfoChanged);
                    });
                }
            });
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
