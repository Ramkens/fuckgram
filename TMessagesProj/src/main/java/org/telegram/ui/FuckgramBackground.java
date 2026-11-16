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

    public static File file() {
        return new File(ApplicationLoader.applicationContext.getFilesDir(), "fg_background");
    }

    public static boolean isActive() {
        int kind = FuckgramConfig.getInt(FuckgramConfig.BG_KIND);
        return kind != NONE && file().exists();
    }

    /** Copies the picked file into the app folder. Runs on a worker thread, returns true on success. */
    public static boolean importFrom(Context context, Uri uri, boolean video) {
        try (InputStream in = context.getContentResolver().openInputStream(uri); FileOutputStream out = new FileOutputStream(file())) {
            if (in == null) {
                return false;
            }
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            FuckgramConfig.setInt(FuckgramConfig.BG_KIND, video ? VIDEO : PHOTO);
            return true;
        } catch (Throwable e) {
            FileLog.e(e);
            return false;
        }
    }

    public static void remove() {
        FuckgramConfig.setInt(FuckgramConfig.BG_KIND, NONE);
        try {
            file().delete();
        } catch (Throwable ignore) {
        }
    }

    /** Puts the background under everything in the given chat container. Does nothing if no background is set. */
    public static void attach(FrameLayout container) {
        try {
            if (container == null || !isActive()) {
                return;
            }
            Context context = container.getContext();
            FrameLayout root = new FrameLayout(context);
            if (FuckgramConfig.getInt(FuckgramConfig.BG_KIND) == VIDEO) {
                root.addView(new VideoView(context), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                ImageView image = new ImageView(context);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setImageBitmap(loadScaled());
                root.addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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

    private static Bitmap loadScaled() {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file().getAbsolutePath(), bounds);
        int target = Math.max(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file().getAbsolutePath(), options);
    }

    private static class VideoView extends TextureView implements TextureView.SurfaceTextureListener {
        private MediaPlayer player;
        private Surface surface;
        private boolean prepared;

        VideoView(Context context) {
            super(context);
            setSurfaceTextureListener(this);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            release();
            try {
                surface = new Surface(texture);
                player = new MediaPlayer();
                player.setDataSource(file().getAbsolutePath());
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
