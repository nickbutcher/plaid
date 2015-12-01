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
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import io.plaidapp.R;
import io.plaidapp.ui.widget.BadgedFourThreeImageView;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.ViewUtils;

/**
 * A Glide {@see ViewTarget} for {@link BadgedFourThreeImageView}s. It applies a badge for animated
 * images, can prevent GIFs from auto-playing & applies a palette generated ripple.
 */
public class DribbbleTarget extends GlideDrawableImageViewTarget implements
        Palette.PaletteAsyncListener {

    private final boolean autoplayGifs;

    public DribbbleTarget(BadgedFourThreeImageView view, boolean autoplayGifs) {
        super(view);
        this.autoplayGifs = autoplayGifs;
    }

    @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable>
            animation) {
        super.onResourceReady(resource, animation);
        if (!autoplayGifs) {
            resource.stop();
        }

        BadgedFourThreeImageView badgedImageView = (BadgedFourThreeImageView) getView();
        if (resource instanceof GlideBitmapDrawable) {
            Palette.from(((GlideBitmapDrawable) resource).getBitmap())
                    .clearFilters()
                    .generate(this);
            badgedImageView.showBadge(false);
        } else if (resource instanceof GifDrawable) {
            Bitmap image = ((GifDrawable) resource).getFirstFrame();
            Palette.from(image).clearFilters().generate(this);
            badgedImageView.showBadge(true);

            // look at the corner to determine the gif badge color
            int cornerSize = (int) (56 * getView().getContext().getResources().getDisplayMetrics
                    ().scaledDensity);
            Bitmap corner = Bitmap.createBitmap(image,
                    image.getWidth() - cornerSize,
                    image.getHeight() - cornerSize,
                    cornerSize, cornerSize);
            boolean isDark = ColorUtils.isDark(corner);
            corner.recycle();
            badgedImageView.setBadgeColor(ContextCompat.getColor(getView().getContext(),
                    isDark ? R.color.gif_badge_dark_image : R.color.gif_badge_light_image));
        }
    }

    @Override
    public void onStart() {
        if (autoplayGifs) {
            super.onStart();
        }
    }

    @Override
    public void onStop() {
        if (autoplayGifs) {
            super.onStop();
        }
    }

    @Override
    public void onGenerated(Palette palette) {
        ((BadgedFourThreeImageView) getView()).setForeground(
                ViewUtils.createRipple(palette, 0.25f, 0.5f,
                        ContextCompat.getColor(getView().getContext(), R.color.mid_grey), true));
    }

}
