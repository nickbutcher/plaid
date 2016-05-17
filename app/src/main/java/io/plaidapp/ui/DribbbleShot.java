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

package io.plaidapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.NumberFormat;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.plaidapp.R;
import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.dribbble.model.Comment;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.ui.transitions.FabTransform;
import io.plaidapp.ui.widget.AuthorTextView;
import io.plaidapp.ui.widget.CheckableImageButton;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.FABToggle;
import io.plaidapp.ui.widget.FabOverlapTextView;
import io.plaidapp.ui.widget.ForegroundImageView;
import io.plaidapp.ui.widget.ParallaxScrimageView;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.ImeUtils;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.CircleTransform;
import io.plaidapp.util.glide.GlideUtils;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.plaidapp.util.AnimUtils.getFastOutSlowInInterpolator;
import static io.plaidapp.util.AnimUtils.getLinearOutSlowInInterpolator;

public class DribbbleShot extends Activity {

    public final static String EXTRA_SHOT = "EXTRA_SHOT";
    private static final int RC_LOGIN_LIKE = 0;
    private static final int RC_LOGIN_COMMENT = 1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.back) ImageButton back;
    @BindView(R.id.shot) ParallaxScrimageView imageView;
    @BindView(R.id.fab_heart) FABToggle fab;
    private View shotSpacer;
    private View title;
    private View description;
    private LinearLayout shotActions;
    private Button likeCount;
    private Button viewCount;
    private Button share;
    private TextView playerName;
    private ImageView playerAvatar;
    private TextView shotTimeAgo;
    private ListView commentsList;
    private DribbbleCommentsAdapter commentsAdapter;
    private View commentFooter;
    private ImageView userAvatar;
    private EditText enterComment;
    private ImageButton postComment;

    private Shot shot;
    private int fabOffset;
    private DribbblePrefs dribbblePrefs;
    private boolean performingLike;
    private boolean allowComment;
    private CircleTransform circleTransform;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    @BindDimen(R.dimen.large_avatar_size) int largeAvatarSize;
    @BindDimen(R.dimen.z_card) int cardElevation;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_shot);
        dribbblePrefs = DribbblePrefs.get(this);
        getWindow().getSharedElementReturnTransition().addListener(shotReturnHomeListener);
        circleTransform = new CircleTransform(this);

        ButterKnife.bind(this);
        View shotDescription = getLayoutInflater().inflate(R.layout.dribbble_shot_description,
                commentsList, false);
        shotSpacer = shotDescription.findViewById(R.id.shot_spacer);
        title = shotDescription.findViewById(R.id.shot_title);
        description = shotDescription.findViewById(R.id.shot_description);
        shotActions = (LinearLayout) shotDescription.findViewById(R.id.shot_actions);
        likeCount = (Button) shotDescription.findViewById(R.id.shot_like_count);
        viewCount = (Button) shotDescription.findViewById(R.id.shot_view_count);
        share = (Button) shotDescription.findViewById(R.id.shot_share_action);
        playerName = (TextView) shotDescription.findViewById(R.id.player_name);
        playerAvatar = (ImageView) shotDescription.findViewById(R.id.player_avatar);
        shotTimeAgo = (TextView) shotDescription.findViewById(R.id.shot_time_ago);
        commentsList = (ListView) findViewById(R.id.dribbble_comments);
        commentsList.addHeaderView(shotDescription);
        setupCommenting();
        commentsList.setOnScrollListener(scrollListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandImageAndFinish();
            }
        });
        fab.setOnClickListener(fabClick);
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            @Override
            public void onDragDismissed() {
                expandImageAndFinish();
            }
        };

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SHOT)) {
            shot = intent.getParcelableExtra(EXTRA_SHOT);
            bindShot(true, savedInstanceState != null);
        } else if (intent.getData() != null) {
            final HttpUrl url = HttpUrl.parse(intent.getDataString());
            if (url.pathSize() == 2 && url.pathSegments().get(0).equals("shots")) {
                try {
                    final String shotPath = url.pathSegments().get(1);
                    final long id = Long.parseLong(shotPath.substring(0, shotPath.indexOf("-")));

                    final Call<Shot> shotCall = dribbblePrefs.getApi().getShot(id);
                    shotCall.enqueue(new Callback<Shot>() {
                        @Override
                        public void onResponse(Call<Shot> call, Response<Shot> response) {
                            shot = response.body();
                            bindShot(false, true);
                        }

                        @Override
                        public void onFailure(Call<Shot> call, Throwable t) {
                            reportUrlError();
                        }
                    });
                } catch (NumberFormatException|StringIndexOutOfBoundsException ex) {
                    reportUrlError();
                }
            } else {
                reportUrlError();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!performingLike) {
            checkLiked();
        }
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_LOGIN_LIKE:
                if (resultCode == RESULT_OK) {
                    // TODO when we add more authenticated actions will need to keep track of what
                    // the user was trying to do when forced to login
                    fab.setChecked(true);
                    doLike();
                    setupCommenting();
                }
                break;
            case RC_LOGIN_COMMENT:
                if (resultCode == RESULT_OK) {
                    setupCommenting();
                }
        }
    }

    @Override
    public void onBackPressed() {
        expandImageAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        expandImageAndFinish();
        return true;
    }

    @Override @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        outContent.setWebUri(Uri.parse(shot.url));
    }

    private void bindShot(final boolean postponeEnterTransition, final boolean animateFabManually) {
        final Resources res = getResources();

        // load the main image
        final int[] imageSize = shot.images.bestSize();
        Glide.with(this)
                .load(shot.images.best())
                .listener(shotLoadListener)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .override(imageSize[0], imageSize[1])
                .into(imageView);
        imageView.setOnClickListener(shotClick);
        shotSpacer.setOnClickListener(shotClick);

        if (postponeEnterTransition) postponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                calculateFabPosition();
                enterAnimation(animateFabManually);
                if (postponeEnterTransition) startPostponedEnterTransition();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((FabOverlapTextView) title).setText(shot.title);
        } else {
            ((TextView) title).setText(shot.title);
        }
        if (!TextUtils.isEmpty(shot.description)) {
            final Spanned descText = shot.getParsedDescription(
                    ContextCompat.getColorStateList(this, R.color.dribbble_links),
                    ContextCompat.getColor(this, R.color.dribbble_link_highlight));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ((FabOverlapTextView) description).setText(descText);
            } else {
                HtmlUtils.setTextWithNiceLinks((TextView) description, descText);
            }
        } else {
            description.setVisibility(View.GONE);
        }
        NumberFormat nf = NumberFormat.getInstance();
        likeCount.setText(
                res.getQuantityString(R.plurals.likes,
                        (int) shot.likes_count,
                        nf.format(shot.likes_count)));
        likeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AnimatedVectorDrawable) likeCount.getCompoundDrawables()[1]).start();
                if (shot.likes_count > 0) {
                    PlayerSheet.start(DribbbleShot.this, shot);
                }
            }
        });
        if (shot.likes_count == 0) {
            likeCount.setBackground(null); // clear touch ripple if doesn't do anything
        }
        viewCount.setText(
                res.getQuantityString(R.plurals.views,
                        (int) shot.views_count,
                        nf.format(shot.views_count)));
        viewCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1]).start();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
                new ShareDribbbleImageTask(DribbbleShot.this, shot).execute();
            }
        });
        if (shot.user != null) {
            playerName.setText(shot.user.name.toLowerCase());
            Glide.with(this)
                    .load(shot.user.getHighQualityAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .into(playerAvatar);
            View.OnClickListener playerClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent player = new Intent(DribbbleShot.this, PlayerActivity.class);
                    if (shot.user.shots_count > 0) { // legit user object
                        player.putExtra(PlayerActivity.EXTRA_PLAYER, shot.user);
                    } else {
                        // search doesn't fully populate the user object,
                        // in this case send the ID not the full user
                        player.putExtra(PlayerActivity.EXTRA_PLAYER_NAME, shot.user.username);
                        player.putExtra(PlayerActivity.EXTRA_PLAYER_ID, shot.user.id);
                    }
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(DribbbleShot.this,
                                    playerAvatar, getString(R.string.transition_player_avatar));
                    startActivity(player, options.toBundle());
                }
            };
            playerAvatar.setOnClickListener(playerClick);
            playerName.setOnClickListener(playerClick);
            if (shot.created_at != null) {
                shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(shot.created_at.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS)
                        .toString().toLowerCase());
            }
        } else {
            playerName.setVisibility(View.GONE);
            playerAvatar.setVisibility(View.GONE);
            shotTimeAgo.setVisibility(View.GONE);
        }

        if (shot.comments_count > 0) {
            loadComments();
        } else {
            commentsList.setAdapter(getNoCommentsAdapter());
        }
        checkLiked();
    }

    private void reportUrlError() {
        Snackbar.make(draggableFrame, R.string.bad_dribbble_shot_url, Snackbar.LENGTH_SHORT).show();
        draggableFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAfterTransition();
            }
        }, 3000L);
    }

    private void setupCommenting() {
        allowComment = !dribbblePrefs.isLoggedIn()
                || (dribbblePrefs.isLoggedIn() && dribbblePrefs.userCanPost());
        if (allowComment && commentFooter == null) {
            commentFooter = getLayoutInflater().inflate(R.layout.dribbble_enter_comment,
                    commentsList, false);
            userAvatar = (ForegroundImageView) commentFooter.findViewById(R.id.avatar);
            enterComment = (EditText) commentFooter.findViewById(R.id.comment);
            postComment = (ImageButton) commentFooter.findViewById(R.id.post_comment);
            enterComment.setOnFocusChangeListener(enterCommentFocus);
            commentsList.addFooterView(commentFooter);
        } else if (!allowComment && commentFooter != null) {
            commentsList.removeFooterView(commentFooter);
            commentFooter = null;
            Toast.makeText(getApplicationContext(),
                    R.string.prospects_cant_post, Toast.LENGTH_SHORT).show();
        }

        if (allowComment
                && dribbblePrefs.isLoggedIn()
                && !TextUtils.isEmpty(dribbblePrefs.getUserAvatar())) {
            Glide.with(this)
                    .load(dribbblePrefs.getUserAvatar())
                    .transform(circleTransform)
                    .placeholder(R.drawable.ic_player)
                    .into(userAvatar);
        }
    }

    private View.OnClickListener shotClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openLink(shot.url);
        }
    };

    private void openLink(String url) {
        CustomTabActivityHelper.openCustomTab(
                DribbbleShot.this,
                new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(DribbbleShot.this, R.color.dribbble))
                    .addDefaultShareMenuItem()
                    .build(),
                Uri.parse(url));
    }

    private RequestListener shotLoadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, DribbbleShot.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            boolean isDark;
                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                            } else {
                                isDark = lightness == ColorUtils.IS_DARK;
                            }

                            if (!isDark) { // make back icon dark on light images
                                back.setColorFilter(ContextCompat.getColor(
                                        DribbbleShot.this, R.color.dark_icon));
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
                                statusBarColorAnim.addUpdateListener(new ValueAnimator
                                        .AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        getWindow().setStatusBarColor(
                                                (int) animation.getAnimatedValue());
                                    }
                                });
                                statusBarColorAnim.setDuration(1000L);
                                statusBarColorAnim.setInterpolator(
                                        getFastOutSlowInInterpolator(DribbbleShot.this));
                                statusBarColorAnim.start();
                            }
                        }
                    });

            Palette.from(bitmap)
                    .clearFilters()
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            // color the ripple on the image spacer (default is grey)
                            shotSpacer.setBackground(ViewUtils.createRipple(palette, 0.25f, 0.5f,
                                    ContextCompat.getColor(DribbbleShot.this, R.color.mid_grey),
                                    true));
                            // slightly more opaque ripple on the pinned image to compensate
                            // for the scrim
                            imageView.setForeground(ViewUtils.createRipple(palette, 0.3f, 0.6f,
                                    ContextCompat.getColor(DribbbleShot.this, R.color.mid_grey),
                                    true));
                        }
                    });

            // TODO should keep the background if the image contains transparency?!
            imageView.setBackground(null);
            return false;
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            return false;
        }
    };

    private View.OnFocusChangeListener enterCommentFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            // kick off an anim (via animated state list) on the post button. see
            // @drawable/ic_add_comment
            postComment.setActivated(hasFocus);
        }
    };

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItemPosition, int
                visibleItemCount, int totalItemCount) {
            if (commentsList.getMaxScrollAmount() > 0
                    && firstVisibleItemPosition == 0
                    && commentsList.getChildAt(0) != null) {
                final int listScroll = commentsList.getChildAt(0).getTop();
                imageView.setOffset(listScroll);
                fab.setOffset(fabOffset + listScroll);
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
            imageView.setImmediatePin(
                    scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING);
        }
    };

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (dribbblePrefs.isLoggedIn()) {
                fab.toggle();
                doLike();
            } else {
                final Intent login = new Intent(DribbbleShot.this, DribbbleLogin.class);
                FabTransform.addExtras(login, ContextCompat.getColor(DribbbleShot.this, R
                        .color.dribbble), R.drawable.ic_heart_empty_56dp);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                        (DribbbleShot.this, fab, getString(R.string.transition_dribbble_login));
                startActivityForResult(login, RC_LOGIN_LIKE, options.toBundle());
            }
        }
    };

    private Transition.TransitionListener shotReturnHomeListener =
            new AnimUtils.TransitionListenerAdapter() {
        @Override
        public void onTransitionStart(Transition transition) {
            super.onTransitionStart(transition);
            // hide the fab as for some reason it jumps position??  TODO work out why
            fab.setVisibility(View.INVISIBLE);
            // fade out the "toolbar" & list as we don't want them to be visible during return
            // animation
            back.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .setInterpolator(getLinearOutSlowInInterpolator(DribbbleShot.this));
            imageView.setElevation(1f);
            back.setElevation(0f);
            commentsList.animate()
                    .alpha(0f)
                    .setDuration(50)
                    .setInterpolator(getLinearOutSlowInInterpolator(DribbbleShot.this));
        }
    };

    private void loadComments() {
        commentsList.setAdapter(getLoadingCommentsAdapter());

        // then load comments
        final Call<List<Comment>> commentsCall =
                dribbblePrefs.getApi().getComments(shot.id, 0, DribbbleService.PER_PAGE_MAX);
        commentsCall.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                final List<Comment> comments = response.body();
                if (comments != null && !comments.isEmpty()) {
                    commentsAdapter = new DribbbleCommentsAdapter(
                            DribbbleShot.this, R.layout.dribbble_comment, comments);
                    commentsList.setAdapter(commentsAdapter);
                    commentsList.setDivider(getDrawable(R.drawable.list_divider));
                    commentsList.setDividerHeight(getResources().getDimensionPixelSize(R.dimen
                            .divider_height));
                }
            }

            @Override public void onFailure(Call<List<Comment>> call, Throwable t) { }
        });
    }

    private void expandImageAndFinish() {
        if (imageView.getOffset() != 0f) {
            Animator expandImage = ObjectAnimator.ofFloat(imageView, ParallaxScrimageView.OFFSET,
                    0f);
            expandImage.setDuration(80);
            expandImage.setInterpolator(getFastOutSlowInInterpolator(this));
            expandImage.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishAfterTransition();
                }
            });
            expandImage.start();
        } else {
            finishAfterTransition();
        }
    }

    private void calculateFabPosition() {
        // calculate 'natural' position i.e. with full height image. Store it for use when scrolling
        fabOffset = imageView.getHeight() + title.getHeight() - (fab.getHeight() / 2);
        fab.setOffset(fabOffset);

        // calculate min position i.e. pinned to the collapsed image when scrolled
        fab.setMinOffset(imageView.getMinimumHeight() - (fab.getHeight() / 2));
    }

    /**
     * Animate in the title, description and author – can't do this in a content transition as they
     * are within the ListView so do it manually.  Also handle the FAB tanslation here so that it
     * plays nicely with #calculateFabPosition
     */
    private void enterAnimation(boolean animateFabManually) {
        Interpolator interp = getFastOutSlowInInterpolator(this);
        int offset = title.getHeight();
        viewEnterAnimation(title, offset, interp);
        if (description.getVisibility() == View.VISIBLE) {
            offset *= 1.5f;
            viewEnterAnimation(description, offset, interp);
        }
        // animate the fab without touching the alpha as this is handled in the content transition
        offset *= 1.5f;
        float fabTransY = fab.getTranslationY();
        fab.setTranslationY(fabTransY + offset);
        fab.animate()
                .translationY(fabTransY)
                .setDuration(600)
                .setInterpolator(interp)
                .start();
        offset *= 1.5f;
        viewEnterAnimation(shotActions, offset, interp);
        offset *= 1.5f;
        viewEnterAnimation(playerName, offset, interp);
        viewEnterAnimation(playerAvatar, offset, interp);
        viewEnterAnimation(shotTimeAgo, offset, interp);
        back.animate()
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(interp)
                .start();

        if (animateFabManually) {
            // we rely on the window enter content transition to show the fab. This isn't run on
            // orientation changes so manually show it.
            Animator showFab = ObjectAnimator.ofPropertyValuesHolder(fab,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f));
            showFab.setStartDelay(300L);
            showFab.setDuration(300L);
            showFab.setInterpolator(getLinearOutSlowInInterpolator(this));
            showFab.start();
        }
    }

    private void viewEnterAnimation(View view, float offset, Interpolator interp) {
        view.setTranslationY(offset);
        view.setAlpha(0.8f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(interp)
                .setListener(null)
                .start();
    }

    private void doLike() {
        performingLike = true;
        if (fab.isChecked()) {
            final Call<Like> likeCall = dribbblePrefs.getApi().like(shot.id);
            likeCall.enqueue(new Callback<Like>() {
                @Override
                public void onResponse(Call<Like> call, Response<Like> response) {
                    performingLike = false;
                }

                @Override
                public void onFailure(Call<Like> call, Throwable t) {
                    performingLike = false;
                }
            });
        } else {
            final Call<Void> unlikeCall = dribbblePrefs.getApi().unlike(shot.id);
            unlikeCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    performingLike = false;
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    performingLike = false;
                }
            });
        }
    }

    private void checkLiked() {
        if (shot != null && dribbblePrefs.isLoggedIn()) {
            final Call<Like> likedCall = dribbblePrefs.getApi().liked(shot.id);
            likedCall.enqueue(new Callback<Like>() {
                @Override
                public void onResponse(Call<Like> call, Response<Like> response) {
                    // note that like.user will be null here
                    fab.setChecked(response.body() != null);
                    fab.jumpDrawablesToCurrentState();
                }

                @Override
                public void onFailure(Call<Like> call, Throwable t) {
                    // 404 is expected if shot is not liked
                    fab.setChecked(false);
                    fab.jumpDrawablesToCurrentState();
                }
            });
        }
    }

    public void postComment(View view) {
        if (dribbblePrefs.isLoggedIn()) {
            if (TextUtils.isEmpty(enterComment.getText())) return;
            enterComment.setEnabled(false);
            final Call<Comment> postCommentCall =
                    dribbblePrefs.getApi().postComment(shot.id, enterComment.getText().toString().trim());
            postCommentCall.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(Call<Comment> call, Response<Comment> response) {
                    loadComments();
                    enterComment.getText().clear();
                    enterComment.setEnabled(true);
                }

                @Override
                public void onFailure(Call<Comment> call, Throwable t) {
                    enterComment.setEnabled(true);
                }
            });
        } else {
            Intent login = new Intent(DribbbleShot.this, DribbbleLogin.class);
            FabTransform.addExtras(login, ContextCompat.getColor(DribbbleShot.this, R
                    .color.background_light), R.drawable.ic_comment_add);
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(DribbbleShot.this, postComment,
                            getString(R.string.transition_dribbble_login));
            startActivityForResult(login, RC_LOGIN_COMMENT, options.toBundle());
        }
    }

    private boolean isOP(long playerId) {
        return shot.user != null && shot.user.id == playerId;
    }

    private ListAdapter getNoCommentsAdapter() {
        String[] noComments = { getString(R.string.no_comments) };
        return new ArrayAdapter<>(this, R.layout.dribbble_no_comments, noComments);
    }

    private ListAdapter getLoadingCommentsAdapter() {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return DribbbleShot.this.getLayoutInflater().inflate(R.layout.loading, parent,
                        false);
            }
        };
    }

    protected class DribbbleCommentsAdapter extends ArrayAdapter<Comment> {

        private final LayoutInflater inflater;
        private final Transition change;
        private int expandedCommentPosition = ListView.INVALID_POSITION;

        public DribbbleCommentsAdapter(Context context, int resource, List<Comment> comments) {
            super(context, resource, comments);
            inflater = LayoutInflater.from(context);
            change = new AutoTransition();
            change.setDuration(200L);
            change.setInterpolator(getFastOutSlowInInterpolator(context));
        }

        @Override
        public View getView(int position, View view, ViewGroup container) {
            if (view == null) {
                view = newNewCommentView(position, container);
            }
            bindComment(getItem(position), position, view);
            return view;
        }

        private View newNewCommentView(int position, ViewGroup parent) {
            View view = inflater.inflate(R.layout.dribbble_comment, parent, false);
            view.setTag(R.id.player_avatar, view.findViewById(R.id.player_avatar));
            view.setTag(R.id.comment_author, view.findViewById(R.id.comment_author));
            view.setTag(R.id.comment_time_ago, view.findViewById(R.id.comment_time_ago));
            view.setTag(R.id.comment_text, view.findViewById(R.id.comment_text));
            view.setTag(R.id.comment_reply, view.findViewById(R.id.comment_reply));
            view.setTag(R.id.comment_like, view.findViewById(R.id.comment_like));
            view.setTag(R.id.comment_likes_count, view.findViewById(R.id.comment_likes_count));
            return view;
        }

        private void bindComment(final Comment comment, final int position, final View view) {
            final ImageView avatar = (ImageView) view.getTag(R.id.player_avatar);
            final AuthorTextView author = (AuthorTextView) view.getTag(R.id.comment_author);
            final TextView timeAgo = (TextView) view.getTag(R.id.comment_time_ago);
            final TextView commentBody = (TextView) view.getTag(R.id.comment_text);
            final ImageButton reply = (ImageButton) view.getTag(R.id.comment_reply);
            final CheckableImageButton likeHeart =
                    (CheckableImageButton) view.getTag(R.id.comment_like);
            final TextView likesCount = (TextView) view.getTag(R.id.comment_likes_count);

            Glide.with(DribbbleShot.this)
                    .load(comment.user.getHighQualityAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .into(avatar);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent player = new Intent(DribbbleShot.this, PlayerActivity.class);
                    player.putExtra(PlayerActivity.EXTRA_PLAYER, comment.user);
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(DribbbleShot.this,
                                    Pair.create(view,
                                            getString(R.string.transition_player_background)),
                                    Pair.create((View) avatar,
                                            getString(R.string.transition_player_avatar)));
                    startActivity(player, options.toBundle());
                }
            });
            author.setText(comment.user.name.toLowerCase());
            author.setOriginalPoster(isOP(comment.user.id));
            timeAgo.setText(comment.created_at == null ? "" :
                    DateUtils.getRelativeTimeSpanString(comment.created_at.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS)
                            .toString().toLowerCase());
            HtmlUtils.setTextWithNiceLinks(commentBody, comment.getParsedBody(commentBody));

            view.setActivated(position == expandedCommentPosition);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isExpanded = reply.getVisibility() == View.VISIBLE;
                    TransitionManager.beginDelayedTransition(commentsList, change);
                    view.setActivated(!isExpanded);
                    if (!isExpanded) { // do expand
                        expandedCommentPosition = position;

                        // work around issue where avatar of selected comment not shown during
                        // shared element transition (returning from player screen)
                        avatar.setOutlineProvider(null);
                        avatar.setElevation(cardElevation);

                        reply.setVisibility(View.VISIBLE);
                        likeHeart.setVisibility(View.VISIBLE);
                        likesCount.setVisibility(View.VISIBLE);
                        if (comment.liked == null) {
                            final Call<Like> liked = dribbblePrefs.getApi()
                                    .likedComment(shot.id, comment.id);
                            liked.enqueue(new Callback<Like>() {
                                @Override
                                public void onResponse(Call<Like> call, Response<Like> response) {
                                    comment.liked = response.isSuccessful();
                                    likeHeart.setChecked(comment.liked);
                                    likeHeart.jumpDrawablesToCurrentState();
                                }

                                @Override public void onFailure(Call<Like> call, Throwable t) { }
                            });
                        }
                        if (enterComment != null && enterComment.hasFocus()) {
                            enterComment.clearFocus();
                            ImeUtils.hideIme(enterComment);
                        }
                        view.requestFocus();
                    } else { // do collapse
                        expandedCommentPosition = ListView.INVALID_POSITION;
                        avatar.setOutlineProvider(ViewUtils.CIRCULAR_OUTLINE);
                        avatar.setElevation(0f);
                        reply.setVisibility(View.GONE);
                        likeHeart.setVisibility(View.GONE);
                        likesCount.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                }
            });

            reply.setVisibility((position == expandedCommentPosition && allowComment) ?
                    View.VISIBLE : View.GONE);
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterComment.setText("@" + comment.user.username + " ");
                    enterComment.setSelection(enterComment.getText().length());

                    // collapse the comment and scroll the reply box (in the footer) into view
                    expandedCommentPosition = ListView.INVALID_POSITION;
                    notifyDataSetChanged();
                    enterComment.requestFocus();
                    commentsList.smoothScrollToPositionFromTop(commentsList.getCount(), 0, 300);
                }
            });

            likeHeart.setChecked(comment.liked != null && comment.liked.booleanValue());
            likeHeart.setVisibility(position == expandedCommentPosition ? View.VISIBLE : View.GONE);
            if (comment.user.id != dribbblePrefs.getUserId()) {
                likeHeart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dribbblePrefs.isLoggedIn()) {
                            if (comment.liked == null || !comment.liked) {
                                comment.liked = true;
                                comment.likes_count++;
                                likesCount.setText(String.valueOf(comment.likes_count));
                                notifyDataSetChanged();
                                final Call<Like> likeCommentCall =
                                        dribbblePrefs.getApi().likeComment(shot.id, comment.id);
                                likeCommentCall.enqueue(new Callback<Like>() {
                                    @Override
                                    public void onResponse(Call<Like> call,
                                                           Response<Like> response) { }

                                    @Override
                                    public void onFailure(Call<Like> call, Throwable t) { }
                                });
                            } else {
                                comment.liked = false;
                                comment.likes_count--;
                                likesCount.setText(String.valueOf(comment.likes_count));
                                notifyDataSetChanged();
                                final Call<Void> unlikeCommentCall =
                                        dribbblePrefs.getApi().unlikeComment(shot.id, comment.id);
                                unlikeCommentCall.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call,
                                                           Response<Void> response) { }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) { }
                                });
                            }
                        } else {
                            likeHeart.setChecked(false);
                            startActivityForResult(new Intent(DribbbleShot.this,
                                    DribbbleLogin.class), RC_LOGIN_LIKE);
                        }
                    }
                });
            }
            likesCount.setVisibility(
                    position == expandedCommentPosition ? View.VISIBLE : View.GONE);
            likesCount.setText(String.valueOf(comment.likes_count));
            likesCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Call<List<Like>> commentLikesCall =
                            dribbblePrefs.getApi().getCommentLikes(shot.id, comment.id);
                    commentLikesCall.enqueue(new Callback<List<Like>>() {
                        @Override
                        public void onResponse(Call<List<Like>> call,
                                               Response<List<Like>> response) {
                            // TODO something better than this.
                            StringBuilder sb = new StringBuilder("Liked by:\n\n");
                            for (Like like : response.body()) {
                                if (like.user != null) {
                                    sb.append("@");
                                    sb.append(like.user.username);
                                    sb.append("\n");
                                }
                            }
                            Toast.makeText(getApplicationContext(), sb.toString(), Toast
                                    .LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<List<Like>> call, Throwable t) {
                            Log.e("GET COMMENT LIKES", t.getMessage(), t);
                        }
                    });
                }
            });
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

    }

}
