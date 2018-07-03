package io.plaidapp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import io.plaidapp.core.ui.widget.ForegroundImageView;
import io.plaidapp.core.util.ViewUtils;

/**
 * An extension to image view that has a circular outline.
 */
public class CircularImageView extends ForegroundImageView {

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOutlineProvider(ViewUtils.CIRCULAR_OUTLINE);
        setClipToOutline(true);
    }
}
