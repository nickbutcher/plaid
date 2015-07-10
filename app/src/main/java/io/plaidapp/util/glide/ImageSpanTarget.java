/*
 * Copyright 2015 Google Inc.
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

package io.plaidapp.util.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.transition.TransitionManager;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.ref.WeakReference;

import in.uncod.android.bypass.style.ImageLoadingSpan;

/**
 * A target that puts a downloaded image into an ImageSpan in the provided TextView.  It uses a
 * {@link ImageLoadingSpan} to mark the area to be replaced by the image.
 */
public class ImageSpanTarget extends SimpleTarget<Bitmap> {

    private WeakReference<TextView> textView;
    private ImageLoadingSpan loadingSpan;

    public ImageSpanTarget(TextView textView, ImageLoadingSpan loadingSpan) {
        this.textView = new WeakReference<>(textView);
        this.loadingSpan = loadingSpan;
    }

    @Override
    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
        TextView tv = textView.get();
        if (tv != null) {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(tv.getResources(), bitmap);
            // image span doesn't handle scaling so we manually set bounds
            if (bitmap.getWidth() > tv.getWidth()) {
                float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
                bitmapDrawable.setBounds(0, 0, tv.getWidth(), (int) (aspectRatio * tv.getWidth()));
            } else {
                bitmapDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            }
            ImageSpan span = new ImageSpan(bitmapDrawable);
            // add the image span and remove our marker
            SpannableStringBuilder ssb = new SpannableStringBuilder(tv.getText());
            int start = ssb.getSpanStart(loadingSpan);
            int end = ssb.getSpanEnd(loadingSpan);
            if (start >= 0 && end >= 0) {
                ssb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.removeSpan(loadingSpan);
            // animate the change
            TransitionManager.beginDelayedTransition((ViewGroup) tv.getParent());
            tv.setText(ssb);
        }
    }

}
