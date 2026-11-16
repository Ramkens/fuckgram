package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Map;

/**
 * fuckgram: settings store (booleans and ints) plus export / import of a settings pack (JSON).
 */
public class FuckgramConfig {

    public static final String GHOST_NO_READ = "ghost_no_read";
    public static final String GHOST_NO_TYPING = "ghost_no_typing";
    public static final String GHOST_OFFLINE = "ghost_offline";
    public static final String KEEP_DELETED = "keep_deleted";
    public static final String SHOW_BADGE = "show_badge";          // show the fuckgram badge of other users / mine to me
    public static final String MARK_ACCOUNT = "mark_account";      // other fuckgram users can recognise my account
    public static final String ENC_ENABLED = "enc_enabled";
    public static final String LIVE_BG = "live_bg";                // 0 off, 1 gradient, 2 gradient + particles
    public static final String LIVE_BG_SPEED = "live_bg_speed";    // 1 slow .. 3 fast
    public static final String LIVE_BG_DENSITY = "live_bg_density";// 1 few .. 3 many particles
    public static final String LIVE_BG_BATTERY = "live_bg_battery";// stop animation in power saving mode
    public static final String TRANSITION = "transition";          // 0 default, 1 fade, 2 zoom, 3 slide up
    public static final String TRANSITION_SPEED = "transition_speed"; // 1 fast .. 3 slow
    public static final String BG_KIND = "bg_kind";                // 0 none, 1 photo, 2 video (the file itself is not part of a pack)
    public static final String BG_KIND_LIST = "bg_kind_list";      // same for the chat list
    public static final String BG_KIND_SETTINGS = "bg_kind_settings"; // same for the settings screens
    public static final String BG_DIM = "bg_dim";                  // 1 light .. 3 dark
    public static final String THEME_MODE = "fg_theme";            // 0 black, 1 white

    public static final String[] ALL_KEYS = {GHOST_NO_READ, GHOST_NO_TYPING, GHOST_OFFLINE, KEEP_DELETED, SHOW_BADGE, MARK_ACCOUNT,
            ENC_ENABLED, LIVE_BG, LIVE_BG_SPEED, LIVE_BG_DENSITY, LIVE_BG_BATTERY, TRANSITION, TRANSITION_SPEED, THEME_MODE, BG_DIM, BG_KIND_LIST, BG_KIND_SETTINGS};

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_config", Context.MODE_PRIVATE);
    }

    private static boolean defaultBool(String key) {
        return SHOW_BADGE.equals(key) || MARK_ACCOUNT.equals(key) || ENC_ENABLED.equals(key) || LIVE_BG_BATTERY.equals(key) || KEEP_DELETED.equals(key);
    }

    private static int defaultInt(String key) {
        switch (key) {
            case LIVE_BG_SPEED:
            case LIVE_BG_DENSITY:
            case TRANSITION_SPEED:
            case BG_DIM:
                return 2;
            default:
                return 0;
        }
    }

    public static boolean get(String key) {
        try {
            return prefs().getBoolean(key, defaultBool(key));
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

    public static int getInt(String key) {
        try {
            return prefs().getInt(key, defaultInt(key));
        } catch (Throwable e) {
            return defaultInt(key);
        }
    }

    public static void setInt(String key, int value) {
        try {
            prefs().edit().putInt(key, value).apply();
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

    public static boolean isShowBadge() {
        return get(SHOW_BADGE);
    }

    public static boolean isKeepDeleted() {
        return get(KEEP_DELETED);
    }

    public static boolean isEncryptionEnabled() {
        return get(ENC_ENABLED);
    }

    /** Settings pack as JSON text, easy to send to a friend in any chat. Passphrases are never part of it. */
    public static String exportPack() {
        try {
            JSONObject root = new JSONObject();
            root.put("fuckgram_pack", 2);
            JSONObject settings = new JSONObject();
            Map<String, ?> all = prefs().getAll();
            for (String key : ALL_KEYS) {
                Object v = all.get(key);
                if (v instanceof Boolean || v instanceof Integer) {
                    settings.put(key, v);
                } else if (defaultBool(key) || key.startsWith("ghost") || KEEP_DELETED.equals(key) || LIVE_BG_BATTERY.equals(key)) {
                    settings.put(key, get(key));
                } else {
                    settings.put(key, getInt(key));
                }
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
            if (root.optInt("fuckgram_pack", 0) < 1) {
                return false;
            }
            JSONObject settings = root.getJSONObject("settings");
            for (String key : ALL_KEYS) {
                if (!settings.has(key)) {
                    continue;
                }
                Object v = settings.get(key);
                if (v instanceof Boolean) {
                    set(key, (Boolean) v);
                } else if (v instanceof Number) {
                    setInt(key, ((Number) v).intValue());
                }
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
