package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** fuckgram: remembers which messages were deleted by the other side but kept locally. */
public class FuckgramDeleted {

    private static final int LIMIT = 4000;
    private static final Set<String>[] cache = new Set[UserConfig.MAX_ACCOUNT_COUNT];

    private static synchronized Set<String> set(int account) {
        if (cache[account] == null) {
            Set<String> s = new HashSet<>();
            try {
                String joined = prefs(account).getString("ids", "");
                for (String part : joined.split(",")) {
                    if (!part.isEmpty()) {
                        s.add(part);
                    }
                }
            } catch (Throwable ignore) {
            }
            cache[account] = s;
        }
        return cache[account];
    }

    private static SharedPreferences prefs(int account) {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_deleted_" + account, Context.MODE_PRIVATE);
    }

    /** key 0 = private chats and basic groups, otherwise the channel dialog id. */
    public static synchronized void mark(int account, LongSparseArray<ArrayList<Integer>> deleted) {
        if (deleted == null) {
            return;
        }
        Set<String> s = set(account);
        for (int a = 0; a < deleted.size(); a++) {
            long key = deleted.keyAt(a);
            ArrayList<Integer> ids = deleted.valueAt(a);
            for (int i = 0; i < ids.size(); i++) {
                s.add(key + "_" + ids.get(i));
            }
        }
        if (s.size() > LIMIT) {
            ArrayList<String> list = new ArrayList<>(s);
            s.clear();
            s.addAll(list.subList(list.size() - LIMIT / 2, list.size()));
        }
        try {
            prefs(account).edit().putString("ids", android.text.TextUtils.join(",", s)).apply();
        } catch (Throwable ignore) {
        }
    }

    public static boolean isDeleted(int account, long dialogId, int id) {
        if (account < 0 || account >= cache.length) {
            return false;
        }
        Set<String> s = set(account);
        synchronized (FuckgramDeleted.class) {
            return s.contains("0_" + id) || s.contains(dialogId + "_" + id);
        }
    }
}
