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
import android.support.annotation.Keep;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class FrameSequence {
    static {
        System.loadLibrary("framesequence");
    }

    private final long mNativeFrameSequence;
    private final int mWidth;
    private final int mHeight;
    private final boolean mOpaque;
    private final int mFrameCount;
    private final int mDefaultLoopCount;
    private int size = -1;

    public int getWidth() { return mWidth; }
    public int getHeight() { return mHeight; }
    public boolean isOpaque() { return mOpaque; }
    public int getFrameCount() { return mFrameCount; }
    public int getDefaultLoopCount() { return mDefaultLoopCount; }
    public int getSize() {
        // TODO return correct size
        return size > 0 ? size : mWidth * mHeight * mFrameCount;
    }

    private static native FrameSequence nativeDecodeByteArray(byte[] data, int offset, int length);
    private static native FrameSequence nativeDecodeStream(InputStream is, byte[] tempStorage);
    private static native FrameSequence nativeDecodeByteBuffer(ByteBuffer buffer, int offset, int capacity);
    private static native void nativeDestroyFrameSequence(long nativeFrameSequence);
    private static native long nativeCreateState(long nativeFrameSequence);
    static native void nativeDestroyState(long nativeState);
    static native long nativeGetFrame(long nativeState, int frameNr,
                                      Bitmap output, int previousFrameNr);

    @Keep @SuppressWarnings("unused") // called by native
    private FrameSequence(long nativeFrameSequence, int width, int height,
                          boolean opaque, int frameCount, int defaultLoopCount) {
        mNativeFrameSequence = nativeFrameSequence;
        mWidth = width;
        mHeight = height;
        mOpaque = opaque;
        mFrameCount = frameCount;
        mDefaultLoopCount = defaultLoopCount;
    }

    public static FrameSequence decodeByteArray(byte[] data) {
        return decodeByteArray(data, 0, data.length);
    }

    public static FrameSequence decodeByteArray(byte[] data, int offset, int length) {
        if (data == null) throw new IllegalArgumentException();
        if (offset < 0 || length < 0 || (offset + length > data.length)) {
            throw new IllegalArgumentException("invalid offset/length parameters");
        }
        final FrameSequence frameSequence = nativeDecodeByteArray(data, offset, length);
        frameSequence.size = length;
        return frameSequence;
    }

    public static FrameSequence decodeByteBuffer(ByteBuffer buffer) {
        if (buffer == null) throw new IllegalArgumentException();
        if (!buffer.isDirect()) {
            if (buffer.hasArray()) {
                byte[] byteArray = buffer.array();
                return decodeByteArray(byteArray, buffer.position(), buffer.remaining());
            } else {
                throw new IllegalArgumentException("Cannot have non-direct ByteBuffer with no byte array");
            }
        }
        return nativeDecodeByteBuffer(buffer, buffer.position(), buffer.remaining());
    }

    public static FrameSequence decodeStream(InputStream stream) {
        if (stream == null) throw new IllegalArgumentException();
        byte[] tempStorage = new byte[16 * 1024]; // TODO: use buffer pool
        final FrameSequence frameSequence = nativeDecodeStream(stream, tempStorage);
        return frameSequence;
    }

    State createState() {
        if (mNativeFrameSequence == 0) {
            throw new IllegalStateException("attempted to use incorrectly built FrameSequence");
        }

        long nativeState = nativeCreateState(mNativeFrameSequence);
        if (nativeState == 0) {
            return null;
        }
        return new State(nativeState);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeFrameSequence != 0) nativeDestroyFrameSequence(mNativeFrameSequence);
        } finally {
            super.finalize();
        }
    }

    /**
     * Playback state used when moving frames forward in a frame sequence.
     *
     * Note that this doesn't require contiguous frames to be rendered, it just stores
     * information (in the case of gif, a recall buffer) that will be used to construct
     * frames based upon data recorded before previousFrameNr.
     *
     * Note: {@link #destroy()} *must* be called before the object is GC'd to free native resources
     *
     * Note: State holds a native ref to its FrameSequence instance, so its FrameSequence should
     * remain ref'd while it is in use
     */
    static class State {
        private long mNativeState;

        State(long nativeState) {
            mNativeState = nativeState;
        }

        void destroy() {
            if (mNativeState != 0) {
                nativeDestroyState(mNativeState);
                mNativeState = 0;
            }
        }

        // TODO: consider adding alternate API for drawing into a SurfaceTexture
        long getFrame(int frameNr, Bitmap output, int previousFrameNr) {
            if (output == null || output.getConfig() != Bitmap.Config.ARGB_8888) {
                throw new IllegalArgumentException("Bitmap passed must be non-null and ARGB_8888");
            }
            if (mNativeState == 0) {
                throw new IllegalStateException("attempted to draw destroyed FrameSequenceState");
            }
            return nativeGetFrame(mNativeState, frameNr, output, previousFrameNr);
        }
    }
}
