package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Locale;

/**
 * fuckgram: a self-imposed ban on chosen bots for a set time. While it runs the bots' chats are hidden, cannot be opened,
 * their mini apps do not start and their notifications are dropped. It cannot be shortened or cleared before it ends,
 * only extended or made stricter.
 */
public class FuckgramBotBlock {

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_botblock", Context.MODE_PRIVATE);
    }

    public static long until() {
        return prefs().getLong("until", 0);
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < until();
    }

    public static ArrayList<String> names() {
        ArrayList<String> list = new ArrayList<>();
        String text = prefs().getString("names", "");
        if (text.length() > 0) {
            for (String part : text.split(",")) {
                if (part.length() > 0) {
                    list.add(part);
                }
            }
        }
        return list;
    }

    private static void saveNames(ArrayList<String> list) {
        prefs().edit().putString("names", android.text.TextUtils.join(",", list)).apply();
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        name = name.trim().toLowerCase(Locale.ROOT);
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int q = name.indexOf('?');
        if (q >= 0) {
            name = name.substring(0, q);
        }
        if (name.startsWith("@")) {
            name = name.substring(1);
        }
        return name.replaceAll("[^a-z0-9_]", "");
    }

    /** Adding is always allowed, even while the ban runs (it only makes it stricter). */
    public static boolean add(String name) {
        name = normalize(name);
        if (name.length() < 3) {
            return false;
        }
        ArrayList<String> list = names();
        if (!list.contains(name)) {
            list.add(name);
            saveNames(list);
        }
        return true;
    }

    /** Removing works only when no ban is running. */
    public static boolean remove(String name) {
        if (isActive()) {
            return false;
        }
        ArrayList<String> list = names();
        list.remove(name);
        saveNames(list);
        return true;
    }

    /** Starts or extends the ban. A shorter time than what is left changes nothing. */
    public static void start(long durationMs) {
        long end = System.currentTimeMillis() + durationMs;
        if (end > until()) {
            prefs().edit().putLong("until", end).apply();
        }
    }

    private static boolean matches(TLRPC.User user, ArrayList<String> names) {
        if (user == null) {
            return false;
        }
        if (user.username != null && names.contains(user.username.toLowerCase(Locale.ROOT))) {
            return true;
        }
        if (user.usernames != null) {
            for (int i = 0; i < user.usernames.size(); i++) {
                TLRPC.TL_username u = user.usernames.get(i);
                if (u != null && u.username != null && names.contains(u.username.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean blocked(int account, long dialogId) {
        if (dialogId <= 0 || !isActive()) {
            return false;
        }
        ArrayList<String> names = names();
        if (names.isEmpty()) {
            return false;
        }
        return matches(MessagesController.getInstance(account).getUser(dialogId), names);
    }

    public static <T extends TLRPC.Dialog> ArrayList<T> filter(int account, ArrayList<T> source) {
        ArrayList<T> result = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            T dialog = source.get(i);
            if (dialog == null || !blocked(account, dialog.id)) {
                result.add(dialog);
            }
        }
        return result;
    }

    /** For notifications: the same list without the messages of banned bots. */
    public static ArrayList<MessageObject> filterMessages(int account, ArrayList<MessageObject> source) {
        if (source == null || !isActive()) {
            return source;
        }
        ArrayList<MessageObject> result = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            MessageObject object = source.get(i);
            if (object == null || !blocked(account, object.getDialogId())) {
                result.add(object);
            }
        }
        return result;
    }
}
