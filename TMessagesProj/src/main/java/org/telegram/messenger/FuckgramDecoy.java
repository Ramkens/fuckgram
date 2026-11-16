package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * fuckgram: the "ghost password". A second code on the lock screen opens the app in a quiet mode: only Saved Messages and the
 * chats you allowed are in the list, notifications from the others are silent. The real code brings everything back.
 */
public class FuckgramDecoy {

    private static volatile Boolean active;

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_decoy", Context.MODE_PRIVATE);
    }

    public static boolean isActive() {
        Boolean value = active;
        if (value == null) {
            value = prefs().getBoolean("active", false);
            active = value;
        }
        return value;
    }

    public static void setActive(boolean value) {
        active = value;
        prefs().edit().putBoolean("active", value).apply();
    }

    public static boolean hasCode() {
        return prefs().getString("hash", "").length() > 0;
    }

    private static String hash(String code, byte[] salt) {
        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[salt.length + codeBytes.length];
        System.arraycopy(salt, 0, bytes, 0, salt.length);
        System.arraycopy(codeBytes, 0, bytes, salt.length, codeBytes.length);
        return Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
    }

    public static void setCode(String code) {
        byte[] salt = new byte[16];
        Utilities.random.nextBytes(salt);
        prefs().edit().putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP)).putString("hash", hash("fg.decoy:" + code, salt)).apply();
    }

    public static void clearCode() {
        prefs().edit().remove("hash").remove("salt").apply();
    }

    public static boolean matches(String code) {
        try {
            SharedPreferences p = prefs();
            String stored = p.getString("hash", "");
            if (stored.length() == 0 || code == null) {
                return false;
            }
            byte[] salt = Base64.decode(p.getString("salt", ""), Base64.NO_WRAP);
            return stored.equals(hash("fg.decoy:" + code, salt));
        } catch (Throwable e) {
            return false;
        }
    }

    public static Set<Long> allowedSet() {
        Set<Long> set = new HashSet<>();
        String text = prefs().getString("allowed", "");
        if (text.length() > 0) {
            for (String part : text.split(",")) {
                try {
                    set.add(Long.parseLong(part));
                } catch (Throwable ignore) {
                }
            }
        }
        return set;
    }

    public static boolean isAllowedChat(long dialogId) {
        return allowedSet().contains(dialogId);
    }

    public static void setAllowedChat(long dialogId, boolean allowed) {
        Set<Long> set = allowedSet();
        if (allowed) {
            set.add(dialogId);
        } else {
            set.remove(dialogId);
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : set) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        prefs().edit().putString("allowed", sb.toString()).apply();
    }

    /** In the quiet mode: may this chat be shown? Saved Messages are always shown. */
    public static boolean allowed(int account, long dialogId) {
        if (!isActive()) {
            return true;
        }
        return dialogId == UserConfig.getInstance(account).getClientUserId() || allowedSet().contains(dialogId);
    }

    public static <T extends org.telegram.tgnet.TLRPC.Dialog> ArrayList<T> filter(int account, ArrayList<T> source) {
        ArrayList<T> result = new ArrayList<>();
        Set<Long> set = allowedSet();
        long self = UserConfig.getInstance(account).getClientUserId();
        for (int i = 0; i < source.size(); i++) {
            T dialog = source.get(i);
            if (dialog == null || dialog.id == 0 || dialog.id == self || set.contains(dialog.id)) {
                result.add(dialog);
            }
        }
        return result;
    }
}
