package org.telegram.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

/**
 * fuckgram: small round badge drawn next to the user's own name (local only, nobody else sees it).
 */
public class FuckgramBadge {

    private static Bitmap cached;

    public static Drawable create(Context context) {
        if (cached == null) {
            try {
                Bitmap source = BitmapFactory.decodeResource(context.getResources(), R.drawable.fg_logo);
                if (source != null) {
                    cached = Bitmap.createScaledBitmap(source, 72, 72, true);
                }
            } catch (Throwable ignore) {
            }
        }
        if (cached == null) {
            return null;
        }
        final int size = AndroidUtilities.dp(18);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), cached) {
            @Override
            public int getIntrinsicWidth() {
                return size;
            }

            @Override
            public int getIntrinsicHeight() {
                return size;
            }
        };
        drawable.setFilterBitmap(true);
        return drawable;
    }
}
