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

package io.plaidapp.ui.dribbble;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.NumberFormat;

import io.plaidapp.base.data.api.dribbble.DribbbleService;
import io.plaidapp.base.data.api.dribbble.model.Shot;
import io.plaidapp.base.data.prefs.DribbblePrefs;
import io.plaidapp.base.dribbble.Injection;
import io.plaidapp.base.util.glide.GlideUtils;
import io.plaidapp.dribbble.R;
import io.plaidapp.ui.recyclerview.InsetDividerDecoration;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.base.ui.widget.ParallaxScrimageView;
import io.plaidapp.base.util.Activities;
import io.plaidapp.base.util.ColorUtils;
import io.plaidapp.base.util.HtmlUtils;
import io.plaidapp.base.util.ViewUtils;
import io.plaidapp.base.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.base.util.glide.GlideApp;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static io.plaidapp.base.util.AnimUtils.getFastOutSlowInInterpolator;

public class DribbbleShot extends Activity {

    private static final float SCRIM_ADJUSTMENT = 0.075f;

    private ElasticDragDismissFrameLayout draggableFrame;
    private ImageButton back;
    private ParallaxScrimageView imageView;
    private RecyclerView commentsList;
    View shotDescription;
    View shotSpacer;
    Button likeCount;
    Button viewCount;
    Button share;
    ImageView playerAvatar;
    private View title;
    private View description;
    private TextView playerName;
    private TextView shotTimeAgo;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

    Shot shot;
    CommentsAdapter adapter;
    private int largeAvatarSize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_shot);
        bindResources();
        shotDescription = getLayoutInflater().inflate(R.layout.dribbble_shot_description,
                commentsList, false);
        shotSpacer = shotDescription.findViewById(R.id.shot_spacer);
        title = shotDescription.findViewById(R.id.shot_title);
        description = shotDescription.findViewById(R.id.shot_description);
        likeCount = shotDescription.findViewById(R.id.shot_like_count);
        viewCount = shotDescription.findViewById(R.id.shot_view_count);
        share = shotDescription.findViewById(R.id.shot_share_action);
        playerName = shotDescription.findViewById(R.id.player_name);
        playerAvatar = shotDescription.findViewById(R.id.player_avatar);
        shotTimeAgo = shotDescription.findViewById(R.id.shot_time_ago);

        commentsList.addOnScrollListener(scrollListener);
        commentsList.setOnFlingListener(flingListener);
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
        } else if (intent.getData() != null) {
            final HttpUrl url = HttpUrl.parse(intent.getDataString());
            if (url.pathSize() == 2 && url.pathSegments().get(0).equals("shots")) {
                try {
                    final String shotPath = url.pathSegments().get(1);
                    final long id = Long.parseLong(shotPath.substring(0, shotPath.indexOf("-")));

                    final DribbbleService api = Injection.provideDribbbleService();
                    final Call<Shot> shotCall = api.getShot(id);
                    shotCall.enqueue(new Callback<Shot>() {
                        @Override
                        public void onResponse(Call<Shot> call, Response<Shot> response) {
                            shot = response.body();
                            bindShot(false);
                        }

                        @Override
                        public void onFailure(Call<Shot> call, Throwable t) {
                            reportUrlError();
                        }
                    });
                } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                    reportUrlError();
                }
            } else {
                reportUrlError();
            }
        }
    }

    private void bindResources() {
        draggableFrame = findViewById(R.id.draggable_frame);
        back = findViewById(R.id.back);
        imageView = findViewById(R.id.shot);
        commentsList = findViewById(R.id.dribbble_comments);
        Resources res = getResources();
        largeAvatarSize = res.getDimensionPixelSize(io.plaidapp.R.dimen.large_avatar_size);
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
        outContent.setWebUri(Uri.parse(shot.url));
    }

    void bindShot(final boolean postponeEnterTransition) {
        final Resources res = getResources();

        // load the main image
        final int[] imageSize = shot.images.bestSize();
        GlideApp.with(this)
                .load(shot.images.best())
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

        ((TextView) title).setText(shot.title);
        if (!TextUtils.isEmpty(shot.description)) {
            final Spanned descText = shot.getParsedDescription(
                    ContextCompat.getColorStateList(this, io.plaidapp.R.color.dribbble_links),
                    ContextCompat.getColor(this, io.plaidapp.R.color.dribbble_link_highlight));
            HtmlUtils.setTextWithNiceLinks((TextView) description, descText);
        } else {
            description.setVisibility(View.GONE);
        }
        NumberFormat nf = NumberFormat.getInstance();
        likeCount.setText(
                res.getQuantityString(io.plaidapp.R.plurals.likes,
                        (int) shot.likes_count,
                        nf.format(shot.likes_count)));
        likeCount.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) likeCount.getCompoundDrawables()[1]).start();
        });
        if (shot.likes_count == 0) {
            likeCount.setBackground(null); // clear touch ripple if doesn't do anything
        }
        viewCount.setText(
                res.getQuantityString(io.plaidapp.R.plurals.views,
                        (int) shot.views_count,
                        nf.format(shot.views_count)));
        viewCount.setOnClickListener(v -> (
                (AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1]).start());
        share.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
            new ShareDribbbleImageTask(DribbbleShot.this, shot).execute();
        });
        if (shot.user != null) {
            playerName.setText(shot.user.name.toLowerCase());
            GlideApp.with(this)
                    .load(shot.user.getHighQualityAvatarUrl())
                    .circleCrop()
                    .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(playerAvatar);
            if (shot.created_at != null) {
                shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(shot.created_at.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS).toString().toLowerCase());
            }
        } else {
            playerName.setVisibility(View.GONE);
            playerAvatar.setVisibility(View.GONE);
            shotTimeAgo.setVisibility(View.GONE);
        }

        adapter = new CommentsAdapter(shotDescription);
        commentsList.setAdapter(adapter);
        commentsList.addItemDecoration(new InsetDividerDecoration(
                res.getDimensionPixelSize(io.plaidapp.R.dimen.divider_height),
                res.getDimensionPixelSize(io.plaidapp.R.dimen.keyline_1),
                ContextCompat.getColor(this, io.plaidapp.R.color.divider)));
    }

    void reportUrlError() {
        Snackbar.make(draggableFrame,
                io.plaidapp.R.string.bad_dribbble_shot_url,
                Snackbar.LENGTH_SHORT)
                .show();
        draggableFrame.postDelayed(this::finishAfterTransition, 3000L);
    }

    private View.OnClickListener shotClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openLink(shot.url);
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
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, DribbbleShot.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                    .generate(palette -> {
                        boolean isDark;
                        @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                        if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                            isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                        } else {
                            isDark = lightness == ColorUtils.IS_DARK;
                        }

                        if (!isDark) { // make back icon dark on light images
                            back.setColorFilter(ContextCompat.getColor(
                                    DribbbleShot.this, io.plaidapp.R.color.dark_icon));
                        }

                        // color the status bar. Set a complementary dark color on L,
                        // light or dark color on M (with matching status bar icons)
                        int statusBarColor = getWindow().getStatusBarColor();
                        final Palette.Swatch topColor =
                                ColorUtils.getMostPopulousSwatch(palette);
                        if (topColor != null &&
                                (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                            statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                    isDark, SCRIM_ADJUSTMENT);
                            // set a light status bar on M+
                            if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ViewUtils.setLightStatusBar(imageView);
                            }
                        }

                        if (statusBarColor != getWindow().getStatusBarColor()) {
                            imageView.setScrimColor(statusBarColor);
                            ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(
                                    getWindow().getStatusBarColor(), statusBarColor);
                            statusBarColorAnim.addUpdateListener(animation ->
                                    getWindow().setStatusBarColor(
                                    (int) animation.getAnimatedValue()));
                            statusBarColorAnim.setDuration(1000L);
                            statusBarColorAnim.setInterpolator(
                                    getFastOutSlowInInterpolator(DribbbleShot.this));
                            statusBarColorAnim.start();
                        }
                    });

            Palette.from(bitmap)
                    .clearFilters()
                    .generate(palette -> {
                        // color the ripple on the image spacer (default is grey)
                        shotSpacer.setBackground(
                                ViewUtils.createRipple(palette, 0.25f, 0.5f, ContextCompat.getColor(
                                        DribbbleShot.this, io.plaidapp.R.color.mid_grey),
                                        true));
                        // slightly more opaque ripple on the pinned image to compensate
                        // for the scrim
                        imageView.setForeground(
                                ViewUtils.createRipple(palette, 0.3f, 0.6f, ContextCompat.getColor(
                                        DribbbleShot.this, io.plaidapp.R.color.mid_grey),
                                        true));
                    });

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

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final int scrollY = shotDescription.getTop();
            imageView.setOffset(scrollY);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
            imageView.setImmediatePin(newState == RecyclerView.SCROLL_STATE_SETTLING);
        }
    };

    private RecyclerView.OnFlingListener flingListener = new RecyclerView.OnFlingListener() {
        @Override
        public boolean onFling(int velocityX, int velocityY) {
            imageView.setImmediatePin(true);
            return false;
        }
    };

    void setResultAndFinish() {
        final Intent resultData = new Intent();
        resultData.putExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID, shot.id);
        setResult(RESULT_OK, resultData);
        finishAfterTransition();
    }

    class CommentsAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

        private final View description;

        CommentsAdapter(@NonNull View description) {
            this.description = description;
        }

        @Override
        public int getItemCount() {
            return 1; // description
        }

        @NonNull
        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SimpleViewHolder(description);
        }

        @Override
        public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
            // no-op
        }

    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {

        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

}
