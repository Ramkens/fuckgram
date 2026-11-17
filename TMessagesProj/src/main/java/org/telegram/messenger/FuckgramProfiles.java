package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;

import java.io.File;
import java.io.FileOutputStream;

/** fuckgram: profile presets (name, description, avatar) that can be applied in one tap. */
public class FuckgramProfiles {

    public static class Preset {
        public String id, title, first, last, about;
        public boolean hasAvatar;
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("fuckgram_profiles", Context.MODE_PRIVATE);
    }

    public static File avatarFile(String id) {
        File dir = new File(ApplicationLoader.applicationContext.getFilesDir(), "fg_profiles");
        dir.mkdirs();
        return new File(dir, id + ".jpg");
    }

    public static java.util.ArrayList<Preset> list() {
        java.util.ArrayList<Preset> result = new java.util.ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs().getString("list", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                Preset p = new Preset();
                p.id = o.getString("id");
                p.title = o.optString("title");
                p.first = o.optString("first");
                p.last = o.optString("last");
                p.about = o.optString("about");
                p.hasAvatar = avatarFile(p.id).exists();
                result.add(p);
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return result;
    }

    private static void save(java.util.ArrayList<Preset> list) {
        try {
            JSONArray array = new JSONArray();
            for (Preset p : list) {
                JSONObject o = new JSONObject();
                o.put("id", p.id);
                o.put("title", p.title);
                o.put("first", p.first);
                o.put("last", p.last);
                o.put("about", p.about);
                array.put(o);
            }
            prefs().edit().putString("list", array.toString()).apply();
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    public static Preset put(Preset preset) {
        java.util.ArrayList<Preset> all = list();
        if (preset.id == null) {
            preset.id = "p" + System.currentTimeMillis();
            all.add(preset);
        } else {
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).id.equals(preset.id)) {
                    all.set(i, preset);
                }
            }
        }
        save(all);
        return preset;
    }

    public static void delete(String id) {
        java.util.ArrayList<Preset> all = list();
        for (int i = all.size() - 1; i >= 0; i--) {
            if (all.get(i).id.equals(id)) {
                all.remove(i);
            }
        }
        save(all);
        avatarFile(id).delete();
    }

    /** Stores a picture (scaled down, as JPEG) as the avatar of the preset. */
    public static boolean setAvatar(String id, File source) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(source.getAbsolutePath(), bounds);
            int sample = 1;
            while (bounds.outWidth / (sample * 2) >= 1000 && bounds.outHeight / (sample * 2) >= 1000) {
                sample *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap bitmap = BitmapFactory.decodeFile(source.getAbsolutePath(), options);
            if (bitmap == null) {
                return false;
            }
            try (FileOutputStream out = new FileOutputStream(avatarFile(id))) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            bitmap.recycle();
            return true;
        } catch (Throwable e) {
            FileLog.e(e);
            return false;
        }
    }

    /** Applies the preset to the account: name, description and (if there is one) the avatar. */
    public static void apply(int account, Preset preset) {
        TL_account.updateProfile req = new TL_account.updateProfile();
        req.flags = 1 | 2 | 4;
        req.first_name = preset.first == null ? "" : preset.first;
        String last = preset.last == null ? "" : preset.last;
        if (FuckgramConfig.get(FuckgramConfig.MARK_ACCOUNT) && !last.endsWith(FuckgramMarker.MARK) && last.length() + FuckgramMarker.MARK.length() <= 64) {
            last += FuckgramMarker.MARK; // keep the fuckgram badge
        }
        req.last_name = last;
        req.about = preset.about == null ? "" : preset.about;
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
        File avatar = avatarFile(preset.id);
        if (avatar.exists()) {
            try {
                TLRPC.TL_fileLocationToBeDeprecated location = new TLRPC.TL_fileLocationToBeDeprecated();
                location.volume_id = Integer.MIN_VALUE;
                location.local_id = SharedConfig.getLastLocalId();
                File target = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), location.volume_id + "_" + location.local_id + ".jpg");
                AndroidUtilities.copyFile(avatar, target);
                MessagesController.getInstance(account).uploadAndApplyUserAvatar(location);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }
}
