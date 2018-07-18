/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.dribbble.ui;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.assist.AssistContent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.core.ui.widget.ParallaxScrimageView;
import io.plaidapp.core.util.Activities;
import io.plaidapp.core.util.ColorUtils;
import io.plaidapp.core.util.HtmlUtils;
import io.plaidapp.core.util.ViewUtils;
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.core.util.glide.GlideApp;
import io.plaidapp.core.util.glide.GlideUtils;
import io.plaidapp.dribbble.R;

import java.text.NumberFormat;

import static io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator;

public class DribbbleShot extends Activity {

    private static final float SCRIM_ADJUSTMENT = 0.075f;

    private ElasticDragDismissFrameLayout draggableFrame;
    private NestedScrollView bodyScroll;
    private Button likeCount;
    private Button viewCount;
    private Button share;
    private ImageView playerAvatar;
    private TextView title;
    private TextView description;
    private TextView playerName;
    private TextView shotTimeAgo;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    ImageButton back;
    ParallaxScrimageView imageView;
    View shotSpacer;

    Shot shot;
    private int largeAvatarSize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_shot);
        bindResources();

        bodyScroll.setOnScrollChangeListener(scrollListener);
        back.setOnClickListener(v -> setResultAndFinish());
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            @Override
            public void onDragDismissed() {
                setResultAndFinish();
            }
        };

        final Intent intent = getIntent();
        if (intent.hasExtra(Activities.Dribbble.Shot.EXTRA_SHOT)) {
            shot = intent.getParcelableExtra(Activities.Dribbble.Shot.EXTRA_SHOT);
            bindShot(true);
        } else {
            finishAfterTransition();
        }
    }

    private void bindResources() {
        draggableFrame = findViewById(R.id.draggable_frame);
        back = findViewById(R.id.back);
        imageView = findViewById(R.id.shot);
        bodyScroll = findViewById(R.id.body_scroll);
        shotSpacer = findViewById(R.id.shot_spacer);
        title = findViewById(R.id.shot_title);
        description = findViewById(R.id.shot_description);
        likeCount = findViewById(R.id.shot_like_count);
        viewCount = findViewById(R.id.shot_view_count);
        share = findViewById(R.id.shot_share_action);
        playerName = findViewById(R.id.player_name);
        playerAvatar = findViewById(R.id.player_avatar);
        shotTimeAgo = findViewById(R.id.shot_time_ago);
        largeAvatarSize = getResources().getDimensionPixelSize(io.plaidapp.R.dimen.large_avatar_size);
    }

    @Override
    protected void onResume() {
        super.onResume();
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        setResultAndFinish();
        return true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        outContent.setWebUri(Uri.parse(shot.getUrl()));
    }

    void bindShot(final boolean postponeEnterTransition) {
        final Resources res = getResources();

        // load the main image
        final int[] imageSize = shot.getImages().bestSize();
        GlideApp.with(this)
                .load(shot.getImages().best())
                .listener(shotLoadListener)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .priority(Priority.IMMEDIATE)
                .override(imageSize[0], imageSize[1])
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
        imageView.setOnClickListener(shotClick);
        shotSpacer.setOnClickListener(shotClick);

        if (postponeEnterTransition) postponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (postponeEnterTransition) startPostponedEnterTransition();
                        return true;
                    }
                });

        title.setText(shot.getTitle());
        if (!TextUtils.isEmpty(shot.getDescription())) {
            final Spanned descText = HtmlUtils.parseHtml(
                    shot.getDescription(),
                    ContextCompat.getColorStateList(this, R.color.dribbble_links),
                    ContextCompat.getColor(this, io.plaidapp.R.color.dribbble_link_highlight));
            HtmlUtils.setTextWithNiceLinks(description, descText);
        } else {
            description.setVisibility(View.GONE);
        }
        NumberFormat nf = NumberFormat.getInstance();
        likeCount.setText(
                res.getQuantityString(io.plaidapp.R.plurals.likes,
                        (int) shot.getLikesCount(),
                        nf.format(shot.getLikesCount())));
        likeCount.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) likeCount.getCompoundDrawables()[1]).start();
        });
        viewCount.setText(
                res.getQuantityString(io.plaidapp.R.plurals.views,
                        (int) shot.getViewsCount(),
                        nf.format(shot.getViewsCount())));
        viewCount.setOnClickListener(v -> (
                (AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1]).start());
        share.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
            new ShareDribbbleImageTask(DribbbleShot.this, shot).execute();
        });
        if (shot.getUser() != null) {
            playerName.setText(shot.getUser().getName().toLowerCase());
            GlideApp.with(this)
                    .load(shot.getUser().getHighQualityAvatarUrl())
                    .circleCrop()
                    .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(playerAvatar);
            if (shot.getCreatedAt() != null) {
                shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(
                        shot.getCreatedAt().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS).toString().toLowerCase());
            }
        } else {
            playerName.setVisibility(View.GONE);
            playerAvatar.setVisibility(View.GONE);
            shotTimeAgo.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener shotClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openLink(shot.getUrl());
        }
    };

    void openLink(String url) {
        CustomTabActivityHelper.openCustomTab(
                DribbbleShot.this,
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(
                                DribbbleShot.this, io.plaidapp.R.color.dribbble))
                        .addDefaultShareMenuItem()
                        .build(),
                Uri.parse(url));
    }

    private RequestListener<Drawable> shotLoadListener = new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                       DataSource dataSource, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            if (bitmap == null) return false;

            Palette.from(bitmap)
                    .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .generate(palette -> applyFullImagePalette(palette));

            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, DribbbleShot.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                    .generate(palette -> applyTopPalette(bitmap, palette));

            // TODO should keep the background if the image contains transparency?!
            imageView.setBackground(null);
            return false;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Drawable> target, boolean isFirstResource) {
            return false;
        }
    };

    void applyFullImagePalette(Palette palette) {
        // color the ripple on the image spacer (default is grey)
        shotSpacer.setBackground(ViewUtils.createRipple(palette, 0.25f, 0.5f,
                ContextCompat.getColor(DribbbleShot.this, io.plaidapp.R.color.mid_grey), true));
        // slightly more opaque ripple on the pinned image to compensate for the scrim
        imageView.setForeground(ViewUtils.createRipple(palette, 0.3f, 0.6f,
                ContextCompat.getColor(DribbbleShot.this, io.plaidapp.R.color.mid_grey), true));
    }

    void applyTopPalette(Bitmap bitmap, Palette palette) {
        boolean isDark;
        @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
        if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
            isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
        } else {
            isDark = lightness == ColorUtils.IS_DARK;
        }

        if (!isDark) { // make back icon dark on light images
            back.setColorFilter(
                    ContextCompat.getColor(DribbbleShot.this, io.plaidapp.R.color.dark_icon));
        }

        // color the status bar.
        int statusBarColor = getWindow().getStatusBarColor();
        final Palette.Swatch topColor = ColorUtils.getMostPopulousSwatch(palette);
        if (topColor != null) {
            statusBarColor = ColorUtils.scrimify(topColor.getRgb(), isDark, SCRIM_ADJUSTMENT);
            // set a light status bar
            if (!isDark) {
                ViewUtils.setLightStatusBar(imageView);
            }
        }

        if (statusBarColor != getWindow().getStatusBarColor()) {
            imageView.setScrimColor(statusBarColor);
            ValueAnimator statusBarColorAnim =
                    ValueAnimator.ofArgb(getWindow().getStatusBarColor(), statusBarColor);
            statusBarColorAnim.addUpdateListener(animation ->
                    getWindow().setStatusBarColor((int) animation.getAnimatedValue()));
            statusBarColorAnim.setDuration(1000L);
            statusBarColorAnim.setInterpolator(getFastOutSlowInInterpolator(DribbbleShot.this));
            statusBarColorAnim.start();
        }
    }

    private NestedScrollView.OnScrollChangeListener scrollListener =
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> imageView.setOffset(-scrollY);

    void setResultAndFinish() {
        final Intent resultData = new Intent();
        resultData.putExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID, shot.getId());
        setResult(RESULT_OK, resultData);
        finishAfterTransition();
    }

}
