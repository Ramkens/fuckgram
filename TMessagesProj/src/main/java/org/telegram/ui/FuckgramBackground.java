package org.telegram.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.FuckgramConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * fuckgram: own photo / video as the background of chats. The picked file is copied into the app folder;
 * a video is muted, looped, and paused whenever the chat is not on screen.
 */
public class FuckgramBackground {

    public static final int NONE = 0, PHOTO = 1, VIDEO = 2;

    public static final int AREA_CHAT = 0, AREA_LIST = 1, AREA_SETTINGS = 2;

    public static String kindKey(int area) {
        return area == AREA_LIST ? FuckgramConfig.BG_KIND_LIST : area == AREA_SETTINGS ? FuckgramConfig.BG_KIND_SETTINGS : FuckgramConfig.BG_KIND;
    }

    public static File file(int area) {
        return new File(ApplicationLoader.applicationContext.getFilesDir(), area == AREA_CHAT ? "fg_background" : "fg_background_" + area);
    }

    public static File file() {
        return file(AREA_CHAT);
    }

    public static int kind(int area) {
        return FuckgramConfig.getInt(kindKey(area));
    }

    public static boolean isActive() {
        return isActive(AREA_CHAT);
    }

    public static boolean isActive(int area) {
        return kind(area) != NONE && file(area).exists();
    }

    /** Copies the picked file into the app folder. Runs on a worker thread, returns true on success. */
    public static boolean importFrom(Context context, Uri uri, boolean video, int area) {
        try (InputStream in = context.getContentResolver().openInputStream(uri); FileOutputStream out = new FileOutputStream(file(area))) {
            if (in == null) {
                return false;
            }
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            forget(area);
            FuckgramConfig.setInt(kindKey(area), video ? VIDEO : PHOTO);
            return true;
        } catch (Throwable e) {
            FileLog.e(e);
            return false;
        }
    }

    public static void remove(int area) {
        forget(area);
        FuckgramConfig.setInt(kindKey(area), NONE);
        try {
            file(area).delete();
        } catch (Throwable ignore) {
        }
    }

    /** Puts the background under everything in the given chat container. Does nothing if no background is set. */
    public static void attach(FrameLayout container) {
        attach(container, AREA_CHAT);
    }

    public static void attach(FrameLayout container, int area) {
        try {
            if (container == null || !isActive(area)) {
                return;
            }
            Context context = container.getContext();
            FrameLayout root = new FrameLayout(context);
            root.setClickable(false);
            root.setFocusable(false);
            root.setWillNotDraw(true);
            // a still picture (or the first frame of a video) is shown at once, the video starts on top of it later
            ImageView image = new ImageView(context);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageBitmap(still(area));
            root.addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (kind(area) == VIDEO && !org.telegram.messenger.FuckgramLite.isOn() && org.telegram.messenger.SharedConfig.getDevicePerformanceClass() != org.telegram.messenger.SharedConfig.PERFORMANCE_CLASS_LOW) {
                root.addView(new VideoView(context, area), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            View dim = new View(context);
            int level = FuckgramConfig.getInt(FuckgramConfig.BG_DIM);
            int alpha = level <= 1 ? 50 : level == 2 ? 100 : 150;
            dim.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
            root.addView(dim, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            container.addView(root, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    private static final Bitmap[] stills = new Bitmap[3];

    /** Decodes the pictures ahead of time (worker thread) so that a chat opens with its background already in place. */
    public static void preload() {
        org.telegram.messenger.Utilities.globalQueue.postRunnable(() -> {
            for (int a = 0; a < 3; a++) {
                if (isActive(a)) {
                    still(a);
                }
            }
        });
    }

    private static synchronized Bitmap still(int area) {
        if (stills[area] == null) {
            try {
                if (kind(area) == VIDEO) {
                    android.media.MediaMetadataRetriever r = new android.media.MediaMetadataRetriever();
                    try {
                        r.setDataSource(file(area).getAbsolutePath());
                        stills[area] = r.getFrameAtTime(0);
                    } finally {
                        r.release();
                    }
                } else {
                    stills[area] = loadScaled(area);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        return stills[area];
    }

    private static synchronized void forget(int area) {
        stills[area] = null;
    }

    private static Bitmap loadScaled(int area) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file(area).getAbsolutePath(), bounds);
        int target = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file(area).getAbsolutePath(), options);
    }

    private static class VideoView extends TextureView implements TextureView.SurfaceTextureListener {
        private MediaPlayer player;
        private Surface surface;
        private boolean prepared;
        private final int area;

        VideoView(Context context, int area) {
            super(context);
            this.area = area;
            setSurfaceTextureListener(this);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            release();
            try {
                surface = new Surface(texture);
                player = new MediaPlayer();
                player.setDataSource(file(area).getAbsolutePath());
                player.setSurface(surface);
                player.setLooping(true);
                player.setVolume(0, 0);
                player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                player.setOnPreparedListener(mp -> {
                    prepared = true;
                    if (isShown()) {
                        mp.start();
                    }
                });
                player.prepareAsync();
            } catch (Throwable e) {
                FileLog.e(e);
                release();
            }
        }

        @Override
        protected void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            updatePlayback();
        }

        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            updatePlayback();
        }

        private void updatePlayback() {
            try {
                if (player != null && prepared) {
                    if (isShown() && getWindowVisibility() == View.VISIBLE) {
                        if (!player.isPlaying()) {
                            player.start();
                        }
                    } else if (player.isPlaying()) {
                        player.pause();
                    }
                }
            } catch (Throwable ignore) {
            }
        }

        private void release() {
            prepared = false;
            try {
                if (player != null) {
                    player.release();
                }
            } catch (Throwable ignore) {
            }
            player = null;
            if (surface != null) {
                surface.release();
                surface = null;
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            release();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    }
}
