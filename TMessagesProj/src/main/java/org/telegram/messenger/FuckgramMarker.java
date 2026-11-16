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

    /** The marker of a message: invisible characters at its end. Only fuckgram clients look for it. */
    public static final String MSG_MARK = "\u2063\u2064";

    private static final java.util.Set<Long> knownUsers = new java.util.HashSet<>();
    private static boolean knownLoaded;

    private static android.content.SharedPreferences usersPrefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_users", android.content.Context.MODE_PRIVATE);
    }

    private static synchronized void loadKnown() {
        if (knownLoaded) {
            return;
        }
        knownLoaded = true;
        try {
            for (String id : usersPrefs().getStringSet("ids", new java.util.HashSet<>())) {
                knownUsers.add(Long.parseLong(id));
            }
        } catch (Throwable ignore) {
        }
    }

    public static synchronized void markUser(long userId) {
        loadKnown();
        if (userId > 0 && knownUsers.add(userId)) {
            try {
                java.util.Set<String> set = new java.util.HashSet<>();
                for (Long id : knownUsers) {
                    set.add(String.valueOf(id));
                }
                usersPrefs().edit().putStringSet("ids", set).apply();
            } catch (Throwable ignore) {
            }
        }
    }

    /** True if the user is known to run fuckgram: marker in the name, or fuckgram messages seen from him. */
    public static synchronized boolean isFuckgramUser(TLRPC.User user) {
        if (user == null) {
            return false;
        }
        if (isMarked(user)) {
            return true;
        }
        loadKnown();
        return knownUsers.contains(user.id);
    }

    public static boolean isMarked(TLRPC.User user) {
        return user != null && user.last_name != null && user.last_name.endsWith(MARK);
    }

    /** Adds the invisible message marker to plain outgoing text so that fuckgram clients can recognise the sender. */
    public static String decorateOutgoing(String text, long peer) {
        try {
            if (text == null || text.isEmpty() || text.length() > 3800 || text.endsWith(MSG_MARK)) {
                return text;
            }
            if (text.charAt(0) == '/' || text.indexOf('@') >= 0 || text.contains("://") || text.contains("t.me") || text.contains("www.")) {
                return text;
            }
            if (FuckgramCrypto.looksEncrypted(text)) {
                return text;
            }
            if (peer > 0) {
                TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(peer);
                if (user == null || user.bot) {
                    return text;
                }
            }
            return text + MSG_MARK;
        } catch (Throwable e) {
            return text;
        }
    }

    /** Called for every incoming message object: remembers the sender if he is a fuckgram user and hides the marker. */
    public static void onIncoming(TLRPC.Message message, long dialogId, boolean encryptedOk) {
        try {
            if (message == null || message.message == null) {
                return;
            }
            boolean marked = message.message.endsWith(MSG_MARK);
            if (marked) {
                message.message = message.message.substring(0, message.message.length() - MSG_MARK.length());
            }
            if (!marked && !encryptedOk) {
                return;
            }
            long from = 0;
            if (message.from_id instanceof TLRPC.TL_peerUser) {
                from = message.from_id.user_id;
            } else if (!message.out && dialogId > 0) {
                from = dialogId;
            }
            if (from > 0 && from != UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()) {
                markUser(from);
            }
        } catch (Throwable ignore) {
        }
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
