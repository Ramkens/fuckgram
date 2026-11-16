package org.telegram.messenger;

import android.util.SparseIntArray;

import org.telegram.ui.ActionBar.Theme;

/**
 * fuckgram: own colours. Five roles (accent, outgoing bubble, incoming bubble, background, text) override a group of theme keys.
 * A role with value 0 keeps the colour of the theme.
 */
public class FuckgramColors {

    public static final int ACCENT = 0, OUT = 1, IN = 2, BG = 3, TEXT = 4;
    public static final String[] KEYS = {"col_accent", "col_out", "col_in", "col_bg", "col_text"};

    private static SparseIntArray table;

    public static int role(int role) {
        return FuckgramConfig.getInt(KEYS[role]);
    }

    public static void setRole(int role, int color) {
        FuckgramConfig.setInt(KEYS[role], color);
        reload();
    }

    public static boolean any() {
        for (int a = 0; a < KEYS.length; a++) {
            if (role(a) != 0) {
                return true;
            }
        }
        return false;
    }

    public static void reset() {
        for (String key : KEYS) {
            FuckgramConfig.setInt(key, 0);
        }
        reload();
    }

    /** Colour for a theme key, or 0 if the key is not overridden. */
    public static int get(int key) {
        SparseIntArray t = table;
        if (t == null) {
            t = reload();
        }
        return t.size() == 0 ? 0 : t.get(key, 0);
    }

    public static synchronized SparseIntArray reload() {
        SparseIntArray t = new SparseIntArray();
        int accent = role(ACCENT), out = role(OUT), in = role(IN), bg = role(BG), text = role(TEXT);
        if (accent != 0) {
            put(t, accent, Theme.key_featuredStickers_addButton, Theme.key_chats_actionBackground, Theme.key_windowBackgroundWhiteBlueHeader,
                    Theme.key_windowBackgroundWhiteBlueText, Theme.key_windowBackgroundWhiteValueText, Theme.key_chat_messagePanelSend,
                    Theme.key_chats_unreadCounter, Theme.key_switchTrackChecked, Theme.key_chat_messageLinkIn);
        }
        if (out != 0) {
            put(t, out, Theme.key_chat_outBubble, Theme.key_chat_outBubbleSelected, Theme.key_chat_outBubbleGradient1,
                    Theme.key_chat_outBubbleGradient2, Theme.key_chat_outBubbleGradient3);
        }
        if (in != 0) {
            put(t, in, Theme.key_chat_inBubble, Theme.key_chat_inBubbleSelected);
        }
        if (bg != 0) {
            put(t, bg, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundGray, Theme.key_dialogBackground,
                    Theme.key_chat_messagePanelBackground, Theme.key_actionBarDefault);
        }
        if (text != 0) {
            put(t, text, Theme.key_windowBackgroundWhiteBlackText, Theme.key_chat_messageTextIn, Theme.key_chat_messageTextOut,
                    Theme.key_actionBarDefaultTitle, Theme.key_chat_messagePanelText);
        }
        table = t;
        return t;
    }

    private static void put(SparseIntArray t, int color, int... keys) {
        for (int key : keys) {
            t.put(key, 0xFF000000 | color);
        }
    }

    /** Ready palettes: accent, outgoing, incoming, background, text. */
    public static final String[] PRESET_NAMES = {"Graphite", "Ocean", "Rose", "Emerald", "Violet", "Amber"};
    public static final int[][] PRESETS = {
            {0xFF8E8E93, 0xFF2E2E33, 0xFF1C1C1E, 0xFF000000, 0xFFFFFFFF},
            {0xFF3A9BDC, 0xFF1F4E79, 0xFF16202B, 0xFF0B141C, 0xFFEAF3FA},
            {0xFFFF5C93, 0xFF7A2848, 0xFF2A1A21, 0xFF140A0F, 0xFFFBEAF0},
            {0xFF34C77B, 0xFF1D5C3F, 0xFF16241D, 0xFF09140E, 0xFFE9F7EF},
            {0xFFA67CFF, 0xFF4B3290, 0xFF221A33, 0xFF100B1B, 0xFFF1EBFF},
            {0xFFFFB02E, 0xFF7A4E10, 0xFF2B2216, 0xFF150F07, 0xFFFFF3DD},
    };

    public static void applyPreset(int index) {
        int[] p = PRESETS[index];
        for (int a = 0; a < KEYS.length; a++) {
            FuckgramConfig.setInt(KEYS[a], p[a]);
        }
        reload();
    }
}
