package io.plaidapp.util.compat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * API compatibility functions for {@link Canvas} supporting API 15+
 */
public final class CanvasCompat {

    private CanvasCompat() {
        throw new AssertionError("No instances.");
    }

    public static void drawRoundRect(
            @NonNull Canvas canvas, float left, float top, float right, float bottom, float rx,
            float ry, @NonNull Paint paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(left, top, right, bottom, rx, ry, paint);
        } else {
            canvas.drawRoundRect(new RectF(left, top, right, bottom), rx, ry, paint);
        }
    }
}
