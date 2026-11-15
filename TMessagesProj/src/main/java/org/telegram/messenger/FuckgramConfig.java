package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * fuckgram: tiny boolean settings store plus export / import of a settings pack (JSON).
 */
public class FuckgramConfig {

    public static final String GHOST_NO_READ = "ghost_no_read";
    public static final String GHOST_NO_TYPING = "ghost_no_typing";
    public static final String GHOST_OFFLINE = "ghost_offline";
    public static final String KEEP_DELETED = "keep_deleted";

    public static final String[] ALL_KEYS = {GHOST_NO_READ, GHOST_NO_TYPING, GHOST_OFFLINE, KEEP_DELETED};

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_config", Context.MODE_PRIVATE);
    }

    public static boolean get(String key) {
        try {
            return prefs().getBoolean(key, false);
        } catch (Throwable e) {
            return false;
        }
    }

    public static void set(String key, boolean value) {
        try {
            prefs().edit().putBoolean(key, value).apply();
        } catch (Throwable ignore) {
        }
    }

    public static boolean isGhostNoRead() {
        return get(GHOST_NO_READ);
    }

    public static boolean isGhostNoTyping() {
        return get(GHOST_NO_TYPING);
    }

    public static boolean isGhostOffline() {
        return get(GHOST_OFFLINE);
    }

    public static boolean isKeepDeleted() {
        return get(KEEP_DELETED);
    }

    /** Settings pack as JSON text, easy to send to a friend in any chat. */
    public static String exportPack() {
        try {
            JSONObject root = new JSONObject();
            root.put("fuckgram_pack", 1);
            JSONObject settings = new JSONObject();
            for (String key : ALL_KEYS) {
                settings.put(key, get(key));
            }
            root.put("settings", settings);
            return root.toString(2);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Applies a pack produced by {@link #exportPack()}. Returns false if the text is not a valid pack. */
    public static boolean importPack(String text) {
        try {
            JSONObject root = new JSONObject(text.trim());
            if (root.optInt("fuckgram_pack", 0) != 1) {
                return false;
            }
            JSONObject settings = root.getJSONObject("settings");
            for (String key : ALL_KEYS) {
                if (settings.has(key)) {
                    set(key, settings.getBoolean(key));
                }
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
