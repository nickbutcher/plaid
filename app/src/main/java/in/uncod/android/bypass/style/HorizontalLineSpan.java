package in.uncod.android.bypass.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

/**
 * Draws a line across the screen.
 */
public class HorizontalLineSpan extends ReplacementSpan {

    private Paint mPaint;
    private int mLineHeight;
    private int mTopBottomPadding;

    public HorizontalLineSpan(int color, int lineHeight, int topBottomPadding) {
        mPaint = new Paint();
        mPaint.setColor(color);
        mLineHeight = lineHeight;
        mTopBottomPadding = topBottomPadding;
    }

    @Override
    public int getSize(
            Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -mLineHeight - mTopBottomPadding;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        // Take up *all* the horizontal space
        return Integer.MAX_VALUE;
    }

    @Override
    public void draw(
            Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int middle = (top + bottom) / 2;
        int halfLineHeight = mLineHeight / 2;
        canvas.drawRect(x, middle - halfLineHeight, Integer.MAX_VALUE, middle + halfLineHeight, mPaint);
    }
}
