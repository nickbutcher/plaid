package io.plaidapp.util.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public final class ImageViewCompat {

    private ImageViewCompat() {
        throw new AssertionError("No instances.");
    }

    public static void setImageAlpha(@NonNull ImageView imageView, @IntRange(from = 0, to = 255) int alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setImageAlpha(alpha);
        } else {
            // Not ideal but better than nothing
            Drawable imageDrawable = imageView.getDrawable();
            if (imageDrawable != null) {
                imageDrawable = imageDrawable.mutate();
                imageDrawable.setAlpha(alpha);
                imageView.setImageDrawable(imageDrawable);
            }
        }
    }

    @IntRange(from = 0, to = 255)
    public static int getImageAlpha(@NonNull ImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return imageView.getImageAlpha();
        } else {
            // Not ideal but better than nothing
            Drawable imageDrawable = imageView.getDrawable();
            if (imageDrawable != null) {
                return imageDrawable.getAlpha();
            } else {
                return 255;
            }
        }
    }
}
