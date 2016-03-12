package io.plaidapp.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;

import io.plaidapp.R;

/**
 * An extension to image view that has a circular outline.
 *
 * Adapted from https://github.com/Pkmmte/CircularImageView
 */
public class CircularImageView extends ForegroundImageView {

    // For logging purposes
    private static final String TAG = CircularImageView.class.getSimpleName();

    // Selector configuration variables
    private boolean hasSelector;
    private boolean isSelected;
    private int canvasSize;

    // Objects used for the actual drawing
    private BitmapShader shader;
    private Bitmap image;
    private Paint paint;
    private ColorFilter selectorFilter;
    private static final boolean IS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public CircularImageView(Context context) {
        this(context, null, R.styleable.CircularImageViewStyle_circularImageViewDefault);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.styleable.CircularImageViewStyle_circularImageViewDefault);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Initializes paint objects and sets desired attributes.
     * @param context Context
     * @param attrs Attributes
     * @param defStyle Default Style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        if (IS_LOLLIPOP) {
            initLollipop();
            return;
        }

        // Initialize paint objects
        paint = new Paint();
        paint.setAntiAlias(true);

        // Load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle, 0);

        // Check for extra features being enabled
        hasSelector = attributes.getBoolean(R.styleable.CircularImageView_civ_selector, false);

        // Set selector properties, if enabled
        if (hasSelector && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setSelectorColor(attributes.getColor(R.styleable.CircularImageView_civ_selectorColor, getResources()
                    .getColor(R.color.ripple_light)));
        }

        // We no longer need our attributes TypedArray, give it back to cache
        attributes.recycle();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initLollipop() {
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(view.getPaddingLeft(),
                        view.getPaddingTop(),
                        view.getWidth() - view.getPaddingRight(),
                        view.getHeight() - view.getPaddingBottom());
            }
        });
        setClipToOutline(true);
    }

    /**
     * Sets the color of the selector to be draw over the
     * CircularImageView. Be sure to provide some opacity.
     * @param selectorColor The color (including alpha) to set for the selector overlay.
     */
    public void setSelectorColor(int selectorColor) {
        this.selectorFilter = new PorterDuffColorFilter(selectorColor, PorterDuff.Mode.SRC_ATOP);
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (IS_LOLLIPOP) {
            super.onDraw(canvas);
            return;
        }

        // Don't draw anything without an image
        if(image == null)
            return;

        // Nothing to draw (Empty bounds)
        if(image.getHeight() == 0 || image.getWidth() == 0)
            return;

        // Update shader if canvas size has changed
        int oldCanvasSize = canvasSize;
        canvasSize = getWidth() < getHeight() ? getWidth() : getHeight();
        if(oldCanvasSize != canvasSize)
            updateBitmapShader();

        // Apply shader to paint
        paint.setShader(shader);

        // Get the exact X/Y axis of the view
        int center = canvasSize / 2;


        if(hasSelector && isSelected) { // Draw the selector stroke & apply the selector filter, if applicable
            center = (canvasSize) / 2;

            paint.setColorFilter(selectorFilter);
        }
        else // Clear the color filter if no selector nor border were drawn
            paint.setColorFilter(null);

        // Draw the circular image itself
        canvas.drawCircle(center, center, ((canvasSize) / 2), paint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (IS_LOLLIPOP) {
            return super.dispatchTouchEvent(event);
        }

        // Check for clickable state and do nothing if disabled
        if(!this.isClickable()) {
            this.isSelected = false;
            return super.onTouchEvent(event);
        }

        // Set selected state based on Motion Event
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.isSelected = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                this.isSelected = false;
                break;
        }

        // Redraw image and return super type
        this.invalidate();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);

        if (IS_LOLLIPOP) {
            return;
        }

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if(canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);

        if (IS_LOLLIPOP) {
            return;
        }

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if(canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);

        if (IS_LOLLIPOP) {
            return;
        }

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable());
        if(canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        if (IS_LOLLIPOP) {
            return;
        }

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = bm;
        if(canvasSize > 0)
            updateBitmapShader();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (IS_LOLLIPOP) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        }
        else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        }
        else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return (result + 2);
    }

    /**
     * Convert a drawable object into a Bitmap.
     * @param drawable Drawable to extract a Bitmap from.
     * @return A Bitmap created from the drawable parameter.
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)   // Don't do anything without a proper drawable
            return null;
        else if (drawable instanceof BitmapDrawable) {  // Use the getBitmap() method instead if BitmapDrawable
            Log.i(TAG, "Bitmap drawable!");
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null;

        try {
            // Create Bitmap object out of the drawable
            Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Simply return null of failed bitmap creations
            Log.e(TAG, "Encountered OutOfMemoryError while generating bitmap!");
            return null;
        }
    }

    /**
     * Re-initializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    public void updateBitmapShader() {
        if (image == null)
            return;

        shader = new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        if(canvasSize != image.getWidth() || canvasSize != image.getHeight()) {
            Matrix matrix = new Matrix();
            float scale = (float) canvasSize / (float) image.getWidth();
            matrix.setScale(scale, scale);
            shader.setLocalMatrix(matrix);
        }
    }

    /**
     * @return Whether or not this view is currently
     * in its selected state.
     */
    public boolean isSelected() {
        return this.isSelected;
    }
}
