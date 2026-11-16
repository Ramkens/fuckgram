package org.telegram.messenger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * fuckgram: hides an (already encrypted) text in the lowest bits of the picture and gets it back.
 * The result is a PNG: it has to be sent as a file, because Telegram recompresses photos and that destroys the hidden bits.
 */
public class FuckgramStego {

    private static final int MAX_SIDE = 2048;
    private static final byte[] MAGIC = {'F', 'G', 'S', 'T'};

    /** Writes a PNG with the text hidden in it to the output file. Returns false if the picture is too small for the text. */
    public static boolean hide(String sourcePath, String text, File out) {
        Bitmap source = null;
        Bitmap work = null;
        try {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(sourcePath, bounds);
            int sample = 1;
            while (bounds.outWidth / (sample * 2) >= MAX_SIDE || bounds.outHeight / (sample * 2) >= MAX_SIDE) {
                sample *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            source = BitmapFactory.decodeFile(sourcePath, options);
            if (source == null) {
                return false;
            }
            int w = source.getWidth(), h = source.getHeight();
            if ((long) w * h * 3 < (long) (data.length + 8) * 8) {
                return false;
            }
            work = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(work);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(source, 0, 0, null);
            int[] pixels = new int[w * h];
            work.getPixels(pixels, 0, w, 0, 0, w, h);
            byte[] payload = new byte[data.length + 8];
            System.arraycopy(MAGIC, 0, payload, 0, 4);
            payload[4] = (byte) (data.length >>> 24);
            payload[5] = (byte) (data.length >>> 16);
            payload[6] = (byte) (data.length >>> 8);
            payload[7] = (byte) data.length;
            System.arraycopy(data, 0, payload, 8, data.length);
            int totalBits = payload.length * 8;
            int bit = 0;
            for (int i = 0; i < pixels.length && bit < totalBits; i++) {
                int p = pixels[i];
                int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
                if (bit < totalBits) {
                    r = (r & 0xFE) | bitAt(payload, bit++);
                }
                if (bit < totalBits) {
                    g = (g & 0xFE) | bitAt(payload, bit++);
                }
                if (bit < totalBits) {
                    b = (b & 0xFE) | bitAt(payload, bit++);
                }
                pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            work.setPixels(pixels, 0, w, 0, 0, w, h);
            try (FileOutputStream stream = new FileOutputStream(out)) {
                return work.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
        } catch (Throwable e) {
            FileLog.e(e);
            return false;
        } finally {
            if (source != null) {
                source.recycle();
            }
            if (work != null) {
                work.recycle();
            }
        }
    }

    private static int bitAt(byte[] data, int bit) {
        return (data[bit >> 3] >> (7 - (bit & 7))) & 1;
    }

    /** The hidden text or null if the picture holds nothing. */
    public static String reveal(String path) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(path, options);
            if (bitmap == null) {
                return null;
            }
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            int[] cursor = {0};
            byte[] header = readBytes(pixels, cursor, 8);
            if (header == null) {
                return null;
            }
            for (int i = 0; i < 4; i++) {
                if (header[i] != MAGIC[i]) {
                    return null;
                }
            }
            int length = ((header[4] & 0xFF) << 24) | ((header[5] & 0xFF) << 16) | ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
            if (length <= 0 || (long) length * 8 > (long) pixels.length * 3) {
                return null;
            }
            byte[] data = readBytes(pixels, cursor, length);
            return data == null ? null : new String(data, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            FileLog.e(e);
            return null;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private static byte[] readBytes(int[] pixels, int[] cursor, int count) {
        byte[] out = new byte[count];
        int totalBits = count * 8;
        for (int bit = 0; bit < totalBits; bit++) {
            int index = cursor[0] / 3;
            if (index >= pixels.length) {
                return null;
            }
            int channel = cursor[0] % 3;
            int p = pixels[index];
            int value = channel == 0 ? (p >> 16) & 1 : channel == 1 ? (p >> 8) & 1 : p & 1;
            cursor[0]++;
            out[bit >> 3] |= (byte) (value << (7 - (bit & 7)));
        }
        return out;
    }
}
