package org.telegram.messenger;

import android.graphics.Paint;
import android.text.TextPaint;

public class FuckgramFonts {
    /** TextPaint that already uses the bundled regular font. */
    public static TextPaint textPaint() {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(AndroidUtilities.regular());
        return paint;
    }
}
