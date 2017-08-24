/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.rastermill;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.graphics.Shader.TileMode.CLAMP;

public class FrameSequenceDrawable extends Drawable implements Animatable, Runnable {
    private static final String TAG = "FrameSequence";
    /**
     * These constants are chosen to imitate common browser behavior for WebP/GIF.
     * If other decoders are added, this behavior should be moved into the WebP/GIF decoders.
     *
     * Note that 0 delay is undefined behavior in the GIF standard.
     */
    private static final long MIN_DELAY_MS = 20;
    private static final long DEFAULT_DELAY_MS = 100;

    private static final Object sLock = new Object();
    private static HandlerThread sDecodingThread;
    private static Handler sDecodingThreadHandler;
    private static void initializeDecodingThread() {
        synchronized (sLock) {
            if (sDecodingThread != null) return;

            sDecodingThread = new HandlerThread("FrameSequence decoding thread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            sDecodingThread.start();
            sDecodingThreadHandler = new Handler(sDecodingThread.getLooper());
        }
    }

    public interface OnFinishedListener {
        /**
         * Called when a FrameSequenceDrawable has finished looping.
         *
         * Note that this is will not be called if the drawable is explicitly
         * stopped, or marked invisible.
         */
        void onFinished(FrameSequenceDrawable drawable);
    }

    public interface BitmapProvider {
        /**
         * Called by FrameSequenceDrawable to aquire an 8888 Bitmap with minimum dimensions.
         */
        Bitmap acquireBitmap(int minWidth, int minHeight);

        /**
         * Called by FrameSequenceDrawable to release a Bitmap it no longer needs. The Bitmap
         * will no longer be used at all by the drawable, so it is safe to reuse elsewhere.
         *
         * This method may be called by FrameSequenceDrawable on any thread.
         */
        void releaseBitmap(Bitmap bitmap);
    }

    private static BitmapProvider sAllocatingBitmapProvider = new BitmapProvider() {
        @Override
        public Bitmap acquireBitmap(int minWidth, int minHeight) {
            return Bitmap.createBitmap(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        }

        @Override
        public void releaseBitmap(Bitmap bitmap) {}
    };

    /**
     * Register a callback to be invoked when a FrameSequenceDrawable finishes looping.
     *
     * @see #setLoopBehavior(int)
     */
    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        mOnFinishedListener = onFinishedListener;
    }

    /**
     * Loop continuously. The OnFinishedListener will never be called.
     */
    public static final int LOOP_INF = -1;

    /**
     * Use loop count stored in source data, or LOOP_ONCE if not present.
     */
    public static final int LOOP_DEFAULT = 0;

    /**
     * Loop a finite number of times, which can be set using setLoopCount. Default to loop once.
     */
    public static final int LOOP_FINITE = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            LOOP_INF,
            LOOP_DEFAULT,
            LOOP_FINITE
    })
    @interface LoopBehavior {}

    /**
     * Define looping behavior of frame sequence.
     *
     * Must be one of LOOP_INF, LOOP_DEFAULT, or LOOP_FINITE.
     */
    public void setLoopBehavior(@LoopBehavior int loopBehavior) {
        mLoopBehavior = loopBehavior;
    }

    /**
     * Set the number of loops in LOOP_FINITE mode. The number must be a postive integer.
     */
    public void setLoopCount(int loopCount) {
        mLoopCount = loopCount;
    }

    final GifState gifState;
    private final Paint mPaint;
    private BitmapShader mFrontBitmapShader;
    private BitmapShader mBackBitmapShader;
    private final Rect mSrcRect;
    private boolean mCircleMaskEnabled;

    //Protects the fields below
    final Object mLock = new Object();

    boolean mDestroyed = false;
    Bitmap mFrontBitmap;
    Bitmap mBackBitmap;

    private static final int STATE_SCHEDULED = 1;
    private static final int STATE_DECODING = 2;
    private static final int STATE_WAITING_TO_SWAP = 3;
    private static final int STATE_READY_TO_SWAP = 4;

    int mState;
    private int mCurrentLoop;
    private @LoopBehavior int mLoopBehavior = LOOP_DEFAULT;
    private int mLoopCount = 1;

    long mLastSwap;
    long mNextSwap;
    OnFinishedListener mOnFinishedListener;

    private RectF mTempRectF = new RectF();

    /**
     * Runs on decoding thread, only modifies mBackBitmap's pixels
     */
    private Runnable mDecodeRunnable = new Runnable() {
        @Override
        public void run() {
            int nextFrame;
            Bitmap bitmap;
            synchronized (mLock) {
                if (mDestroyed) return;

                nextFrame = gifState.nextFrameToDecode;
                if (nextFrame < 0) {
                    return;
                }
                bitmap = mBackBitmap;
                mState = STATE_DECODING;
            }
            int lastFrame = nextFrame - 2;
            boolean exceptionDuringDecode = false;
            long invalidateTimeMs = 0;
            try {
                invalidateTimeMs = gifState.frameSequenceState.getFrame(nextFrame, bitmap, lastFrame);
            } catch(Exception e) {
                // Exception during decode: continue, but delay next frame indefinitely.
                Log.e(TAG, "exception during decode: " + e);
                exceptionDuringDecode = true;
            }

            if (invalidateTimeMs < MIN_DELAY_MS) {
                invalidateTimeMs = DEFAULT_DELAY_MS;
            }

            boolean schedule = false;
            Bitmap bitmapToRelease = null;
            synchronized (mLock) {
                if (mDestroyed) {
                    bitmapToRelease = mBackBitmap;
                    mBackBitmap = null;
                } else if (gifState.nextFrameToDecode >= 0 && mState == STATE_DECODING) {
                    schedule = true;
                    mNextSwap = exceptionDuringDecode ? Long.MAX_VALUE : invalidateTimeMs + mLastSwap;
                    mState = STATE_WAITING_TO_SWAP;
                }
            }
            if (schedule) {
                scheduleSelf(FrameSequenceDrawable.this, mNextSwap);
            }
            if (bitmapToRelease != null) {
                // destroy the bitmap here, since there's no safe way to get back to
                // drawable thread - drawable is likely detached, so schedule is noop.
                gifState.bitmapProvider.releaseBitmap(bitmapToRelease);
            }
        }
    };

    private Runnable mFinishedCallbackRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                gifState.nextFrameToDecode = -1;
                mState = 0;
            }
            if (mOnFinishedListener != null) {
                mOnFinishedListener.onFinished(FrameSequenceDrawable.this);
            }
        }
    };

    private static Bitmap acquireAndValidateBitmap(BitmapProvider bitmapProvider,
            int minWidth, int minHeight) {
        Bitmap bitmap = bitmapProvider.acquireBitmap(minWidth, minHeight);

        if (bitmap.getWidth() < minWidth
                || bitmap.getHeight() < minHeight
                || bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Invalid bitmap provided");
        }
        return bitmap;
    }

    public FrameSequenceDrawable(@NonNull FrameSequence frameSequence) {
        this(frameSequence, sAllocatingBitmapProvider);
    }

    public FrameSequenceDrawable(@NonNull FrameSequence frameSequence,
                                 @NonNull BitmapProvider bitmapProvider) {
        this(new GifState(frameSequence, bitmapProvider));
    }

    FrameSequenceDrawable(@NonNull GifState constantState) {
        gifState = constantState;
        final int width = constantState.frameSequence.getWidth();
        final int height = constantState.frameSequence.getHeight();
        mFrontBitmap = acquireAndValidateBitmap(constantState.bitmapProvider, width, height);
        mBackBitmap = acquireAndValidateBitmap(constantState.bitmapProvider, width, height);
        mSrcRect = new Rect(0, 0, width, height);
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);

        mFrontBitmapShader = new BitmapShader(mFrontBitmap, CLAMP, CLAMP);
        mBackBitmapShader = new BitmapShader(mBackBitmap, CLAMP, CLAMP);
        mLastSwap = 0;
        gifState.nextFrameToDecode = -1;
        gifState.frameSequenceState.getFrame(0, mFrontBitmap, -1);
        initializeDecodingThread();
    }

    @Nullable @Override
    public ConstantState getConstantState() {
        return gifState;
    }

    public Bitmap getFirstFrame() {
        renderFrame();
        return mFrontBitmap;
    }

    public int getFrameCount() {
        return gifState.frameSequence.getFrameCount();
    }

    public int getSize() {
        return gifState.frameSequence.getSize();
    }

    public void setAutoPlay(boolean autoPlay) {
        gifState.autoPlay = autoPlay;
        if (!autoPlay) {
            stop();
        }
    }

    /**
     * Pass true to mask the shape of the animated drawing content to a circle.
     *
     * <p> The masking circle will be the largest circle contained in the Drawable's bounds.
     * Masking is done with BitmapShader, incurring minimal additional draw cost.
     */
    public final void setCircleMaskEnabled(boolean circleMaskEnabled) {
        if (mCircleMaskEnabled != circleMaskEnabled) {
            mCircleMaskEnabled = circleMaskEnabled;
            // Anti alias only necessary when using circular mask
            mPaint.setAntiAlias(circleMaskEnabled);
            invalidateSelf();
        }
    }

    public final boolean getCircleMaskEnabled() {
        return mCircleMaskEnabled;
    }

    private void checkDestroyedLocked() {
        if (mDestroyed) {
            throw new IllegalStateException("Cannot perform operation on recycled drawable");
        }
    }

    public boolean isDestroyed() {
        synchronized (mLock) {
            return mDestroyed;
        }
    }

    /**
     * Marks the drawable as permanently recycled (and thus unusable), and releases any owned
     * Bitmaps drawable to its BitmapProvider, if attached.
     *
     * If no BitmapProvider is attached to the drawable, recycle() is called on the Bitmaps.
     */
    public void destroy() {
        if (gifState.bitmapProvider == null) {
            throw new IllegalStateException("BitmapProvider must be non-null");
        }

        Bitmap bitmapToReleaseA;
        Bitmap bitmapToReleaseB = null;
        synchronized (mLock) {
            checkDestroyedLocked();

            bitmapToReleaseA = mFrontBitmap;
            mFrontBitmap = null;

            if (mState != STATE_DECODING) {
                bitmapToReleaseB = mBackBitmap;
                mBackBitmap = null;
            }

            mDestroyed = true;
        }

        // For simplicity and safety, we don't destroy the state object here
        gifState.bitmapProvider.releaseBitmap(bitmapToReleaseA);
        if (bitmapToReleaseB != null) {
            gifState.bitmapProvider.releaseBitmap(bitmapToReleaseB);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        renderFrame();

        if (mCircleMaskEnabled) {
            final Rect bounds = getBounds();
            final int bitmapWidth = getIntrinsicWidth();
            final int bitmapHeight = getIntrinsicHeight();
            final float scaleX = 1.0f * bounds.width() / bitmapWidth;
            final float scaleY = 1.0f * bounds.height() / bitmapHeight;

            canvas.save();
            // scale and translate to account for bounds, so we can operate in intrinsic
            // width/height (so it's valid to use an unscaled bitmap shader)
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(scaleX, scaleY);

            final float unscaledCircleDiameter = Math.min(bounds.width(), bounds.height());
            final float scaledDiameterX = unscaledCircleDiameter / scaleX;
            final float scaledDiameterY = unscaledCircleDiameter / scaleY;

            // Want to draw a circle, but we have to compensate for canvas scale
            mTempRectF.set(
                    (bitmapWidth - scaledDiameterX) / 2.0f,
                    (bitmapHeight - scaledDiameterY) / 2.0f,
                    (bitmapWidth + scaledDiameterX) / 2.0f,
                    (bitmapHeight + scaledDiameterY) / 2.0f);
            mPaint.setShader(mFrontBitmapShader);
            canvas.drawOval(mTempRectF, mPaint);
            canvas.restore();
        } else {
            mPaint.setShader(null);
            canvas.drawBitmap(mFrontBitmap, mSrcRect, getBounds(), mPaint);
        }
    }

    private void renderFrame() {
        synchronized (mLock) {
            checkDestroyedLocked();
            if (mState == STATE_WAITING_TO_SWAP) {
                // may have failed to schedule mark ready runnable,
                // so go ahead and swap if swapping is due
                if (mNextSwap - SystemClock.uptimeMillis() <= 0) {
                    mState = STATE_READY_TO_SWAP;
                }
            }

            if (isRunning() && mState == STATE_READY_TO_SWAP) {
                // Because draw has occurred, the view system is guaranteed to no longer hold a
                // reference to the old mFrontBitmap, so we now use it to produce the next frame
                Bitmap tmp = mBackBitmap;
                mBackBitmap = mFrontBitmap;
                mFrontBitmap = tmp;

                BitmapShader tmpShader = mBackBitmapShader;
                mBackBitmapShader = mFrontBitmapShader;
                mFrontBitmapShader = tmpShader;

                mLastSwap = SystemClock.uptimeMillis();

                boolean continueLooping = true;
                if (gifState.nextFrameToDecode == gifState.frameSequence.getFrameCount() - 1) {
                    mCurrentLoop++;
                    if ((mLoopBehavior == LOOP_FINITE && mCurrentLoop == mLoopCount) ||
                            (mLoopBehavior == LOOP_DEFAULT && mCurrentLoop ==
                                    gifState.frameSequence.getDefaultLoopCount())) {
                        continueLooping = false;
                    }
                }

                if (continueLooping) {
                    scheduleDecodeLocked();
                } else {
                    scheduleSelf(mFinishedCallbackRunnable, 0);
                }
            }
        }
    }

    private void scheduleDecodeLocked() {
        mState = STATE_SCHEDULED;
        gifState.nextFrameToDecode =
                (gifState.nextFrameToDecode + 1) % gifState.frameSequence.getFrameCount();
        sDecodingThreadHandler.post(mDecodeRunnable);
    }

    @Override
    public void run() {
        // set ready to swap as necessary
        boolean invalidate = false;
        synchronized (mLock) {
            if (gifState.nextFrameToDecode >= 0 && mState == STATE_WAITING_TO_SWAP) {
                mState = STATE_READY_TO_SWAP;
                invalidate = true;
            }
        }
        if (invalidate) {
            invalidateSelf();
        }
    }

    @Override
    public void start() {
        if (!isRunning()) {
            synchronized (mLock) {
                checkDestroyedLocked();
                if (mState == STATE_SCHEDULED) return; // already scheduled
                mCurrentLoop = 0;
                scheduleDecodeLocked();
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            unscheduleSelf(this);
        }
    }

    @Override
    public boolean isRunning() {
        synchronized (mLock) {
            return mState != 0 && gifState.nextFrameToDecode > -1 && !mDestroyed;
        }
    }

    @Override
    public void unscheduleSelf(@NonNull Runnable what) {
        synchronized (mLock) {
            gifState.nextFrameToDecode = -1;
            mState = 0;
        }
        super.unscheduleSelf(what);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);

        if (!visible) {
            stop();
        } else if (gifState.autoPlay && (restart || changed)) {
            stop();
            start();
        }

        return changed;
    }

    // drawing properties

    @Override
    public void setFilterBitmap(boolean filter) {
        mPaint.setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicWidth() {
        return gifState.frameSequence.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return gifState.frameSequence.getHeight();
    }

    @Override
    public int getOpacity() {
        return gifState.frameSequence.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSPARENT;
    }

    static class GifState extends ConstantState {

        final FrameSequence frameSequence;
        final BitmapProvider bitmapProvider;
        FrameSequence.State frameSequenceState;
        int nextFrameToDecode = -1;
        boolean autoPlay = true;

        GifState(@NonNull FrameSequence frameSequence, @NonNull BitmapProvider bitmapProvider) {
            this.frameSequence = frameSequence;
            this.bitmapProvider = bitmapProvider;
            frameSequenceState = frameSequence.createState();
        }

        @NonNull @Override
        public Drawable newDrawable() {
            frameSequenceState.destroy();
            frameSequenceState = frameSequence.createState();
            return new FrameSequenceDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
