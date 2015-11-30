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
import android.app.SharedElementCallback;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.plaidapp.R;
import io.plaidapp.data.api.AuthInterceptor;
import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.dribbble.model.Comment;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.ui.transitions.FabDialogMorphSetup;
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
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class DribbbleShot extends Activity {

    protected final static String EXTRA_SHOT = "shot";
    private static final int RC_LOGIN_LIKE = 0;
    private static final int RC_LOGIN_COMMENT = 1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @Bind(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @Bind(R.id.back) ImageButton back;
    @Bind(R.id.shot) ParallaxScrimageView imageView;
    @Bind(R.id.fab_heart) FABToggle fab;
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
    private DribbbleService dribbbleApi;
    private boolean performingLike;
    private boolean allowComment;
    private CircleTransform circleTransform;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_shot);
        shot = getIntent().getParcelableExtra(EXTRA_SHOT);
        setupDribbble();
        setExitSharedElementCallback(fabLoginSharedElementCallback);
        getWindow().getSharedElementReturnTransition().addListener(shotReturnHomeListener);
        circleTransform = new CircleTransform(this);
        Resources res = getResources();

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
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(getWindow()) {
            @Override
            public void onDragDismissed() {
                expandImageAndFinish();
            }
        };

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

        postponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                calculateFabPosition();
                enterAnimation(savedInstanceState != null);
                startPostponedEnterTransition();
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
        // TODO onClick show likes
        viewCount.setText(
                res.getQuantityString(R.plurals.views,
                        (int) shot.views_count,
                        nf.format(shot.views_count)));
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareDribbbleImageTask(DribbbleShot.this, shot).execute();
            }
        });
        if (shot.user != null) {
            playerName.setText("–" + shot.user.name);
            Glide.with(this)
                    .load(shot.user.avatar_url)
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(playerAvatar);
            playerAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DribbbleShot.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(shot
                            .user.html_url)));
                }
            });
            if (shot.created_at != null) {
                shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(shot.created_at.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS));
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
                    setupDribbble(); // recreate to capture the new access token
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
                    .build(),
                Uri.parse(url));
    }

    private RequestListener shotLoadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            float imageScale = (float) imageView.getHeight() / (float) bitmap.getHeight();
            float twentyFourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                    DribbbleShot.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(0, 0, bitmap.getWidth() - 1, (int) (twentyFourDip / imageScale))
                    // - 1 to work around https://code.google.com/p/android/issues/detail?id=191013
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
                            Palette.Swatch topColor = ColorUtils.getMostPopulousSwatch(palette);
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
                                ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(getWindow
                                        ().getStatusBarColor(), statusBarColor);
                                statusBarColorAnim.addUpdateListener(new ValueAnimator
                                        .AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        getWindow().setStatusBarColor((int) animation
                                                .getAnimatedValue());
                                    }
                                });
                                statusBarColorAnim.setDuration(1000);
                                statusBarColorAnim.setInterpolator(AnimationUtils
                                        .loadInterpolator(DribbbleShot.this, android.R
                                                .interpolator.fast_out_slow_in));
                                statusBarColorAnim.start();
                            }
                        }
                    });

            Palette.from(bitmap)
                    .clearFilters() // by default palette ignore certain hues (e.g. pure
                            // black/white) but we don't want this.
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
            // @drawable/ic_add_comment_state
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
                int listScroll = commentsList.getChildAt(0).getTop();
                imageView.setOffset(listScroll);
                fab.setOffset(fabOffset + listScroll);
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // as we animate the main image's elevation change when it 'pins' at it's min height
            // a fling can cause the title to go over the image before the animation has a chance to
            // run. In this case we short circuit the animation and just jump to state.
            imageView.setImmediatePin(scrollState == AbsListView.OnScrollListener
                    .SCROLL_STATE_FLING);
        }
    };

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (dribbblePrefs.isLoggedIn()) {
                fab.toggle();
                doLike();
            } else {
                Intent login = new Intent(DribbbleShot.this, DribbbleLogin.class);
                login.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR,
                        ContextCompat.getColor(DribbbleShot.this, R.color.dribbble));
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                        (DribbbleShot.this, fab, getString(R.string.transition_dribbble_login));
                startActivityForResult(login, RC_LOGIN_LIKE, options.toBundle());
            }
        }
    };

    private SharedElementCallback fabLoginSharedElementCallback = new SharedElementCallback() {
        @Override
        public Parcelable onCaptureSharedElementSnapshot(View sharedElement,
                                                         Matrix viewToGlobalMatrix,
                                                         RectF screenBounds) {
            // store a snapshot of the fab to fade out when morphing to the login dialog
            int bitmapWidth = Math.round(screenBounds.width());
            int bitmapHeight = Math.round(screenBounds.height());
            Bitmap bitmap = null;
            if (bitmapWidth > 0 && bitmapHeight > 0) {
                bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                sharedElement.draw(new Canvas(bitmap));
            }
            return bitmap;
        }
    };

    private Transition.TransitionListener shotReturnHomeListener = new AnimUtils
            .TransitionListenerAdapter() {
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
                    .setInterpolator(AnimationUtils.loadInterpolator(DribbbleShot.this, android.R
                            .interpolator.linear_out_slow_in));
            imageView.setElevation(1f);
            back.setElevation(0f);
            commentsList.animate()
                    .alpha(0f)
                    .setDuration(50)
                    .setInterpolator(AnimationUtils.loadInterpolator(DribbbleShot.this, android.R
                            .interpolator.linear_out_slow_in));
        }
    };

    private void loadComments() {
        commentsList.setAdapter(getLoadingCommentsAdapter());

        // then load comments
        dribbbleApi.getComments(shot.id, null, DribbbleService.PER_PAGE_MAX, new retrofit
                .Callback<List<Comment>>() {
            @Override
            public void success(List<Comment> comments, Response response) {
                if (comments != null && !comments.isEmpty()) {
                    commentsAdapter = new DribbbleCommentsAdapter(DribbbleShot.this, R.layout
                            .dribbble_comment, comments);
                    commentsList.setAdapter(commentsAdapter);
                    commentsList.setDivider(getDrawable(R.drawable.list_divider));
                    commentsList.setDividerHeight(getResources().getDimensionPixelSize(R.dimen
                            .divider_height));
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void expandImageAndFinish() {
        if (imageView.getOffset() != 0f) {
            Animator expandImage = ObjectAnimator.ofFloat(imageView, ParallaxScrimageView.OFFSET,
                    0f);
            expandImage.setDuration(80);
            expandImage.setInterpolator(AnimationUtils.loadInterpolator(this, android.R
                    .interpolator.fast_out_slow_in));
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

    private void setupDribbble() {
        // setup the api object which captures the current access token
        dribbblePrefs = DribbblePrefs.get(this);
        Gson gson = new GsonBuilder()
                .setDateFormat(DribbbleService.DATE_FORMAT)
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(DribbbleService.ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new AuthInterceptor(dribbblePrefs.getAccessToken()))
                .build();
        dribbbleApi = restAdapter.create(DribbbleService.class);
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
    private void enterAnimation(boolean isOrientationChange) {
        Interpolator interp = AnimationUtils.loadInterpolator(this, android.R.interpolator
                .fast_out_slow_in);
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

        if (isOrientationChange) {
            // we rely on the window enter content transition to show the fab. This isn't run on
            // orientation changes so manually show it.
            Animator showFab = ObjectAnimator.ofPropertyValuesHolder(fab,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f));
            showFab.setStartDelay(300L);
            showFab.setDuration(300L);
            showFab.setInterpolator(AnimationUtils.loadInterpolator(this,
                    android.R.interpolator.linear_out_slow_in));
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
            dribbbleApi.like(shot.id, "", new retrofit.Callback<Like>() {
                @Override
                public void success(Like like, Response response) {
                    performingLike = false;
                }

                @Override
                public void failure(RetrofitError error) {
                    performingLike = false;
                }
            });
        } else {
            dribbbleApi.unlike(shot.id, new retrofit.Callback<Void>() {
                @Override
                public void success(Void aVoid, Response response) {
                    performingLike = false;
                }

                @Override
                public void failure(RetrofitError error) {
                    performingLike = false;
                }
            });
        }
    }

    private void checkLiked() {
        if (dribbblePrefs.isLoggedIn()) {
            dribbbleApi.liked(shot.id, new retrofit.Callback<Like>() {
                @Override
                public void success(Like like, Response response) {
                    // note that like.user will be null here
                    fab.setChecked(like != null);
                    fab.jumpDrawablesToCurrentState();
                }

                @Override
                public void failure(RetrofitError error) {
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
            dribbbleApi.postComment(shot.id, enterComment.getText().toString().trim(), new retrofit
                    .Callback<Comment>() {
                @Override
                public void success(Comment comment, Response response) {
                    loadComments();
                    enterComment.getText().clear();
                    enterComment.setEnabled(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    enterComment.setEnabled(true);
                }
            });
        } else {
            Intent login = new Intent(DribbbleShot.this, DribbbleLogin.class);
            login.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR, ContextCompat.getColor
                    (this, R.color.background_light));
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
            change.setInterpolator(AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in));
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
                    .load(comment.user.avatar_url)
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .into(avatar);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DribbbleShot.this.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(comment.user.html_url)));
                }
            });
            author.setText(comment.user.name);
            author.setOriginalPoster(isOP(comment.user.id));
            timeAgo.setText(comment.created_at == null ? "" :
                    DateUtils.getRelativeTimeSpanString(comment.created_at.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS));
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
                        reply.setVisibility(View.VISIBLE);
                        likeHeart.setVisibility(View.VISIBLE);
                        likesCount.setVisibility(View.VISIBLE);
                        if (comment.liked == null) {
                            dribbbleApi.likedComment(shot.id, comment.id,
                                    new retrofit.Callback<Like>() {
                                @Override
                                public void success(Like like, Response response) {
                                    comment.liked = true;
                                    likeHeart.setChecked(true);
                                    likeHeart.jumpDrawablesToCurrentState();
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    comment.liked = false;
                                    likeHeart.setChecked(false);
                                    likeHeart.jumpDrawablesToCurrentState();
                                }
                            });
                        }
                        if (enterComment != null && enterComment.hasFocus()) {
                            enterComment.clearFocus();
                            ImeUtils.hideIme(enterComment);
                        }
                        view.requestFocus();
                    } else { // do collapse
                        expandedCommentPosition = ListView.INVALID_POSITION;
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
                                dribbbleApi.likeComment(shot.id, comment.id, "",
                                        new retrofit.Callback<Like>() {
                                    @Override
                                    public void success(Like like, Response response) { }

                                    @Override
                                    public void failure(RetrofitError error) { }
                                });
                            } else {
                                comment.liked = false;
                                comment.likes_count--;
                                likesCount.setText(String.valueOf(comment.likes_count));
                                notifyDataSetChanged();
                                dribbbleApi.unlikeComment(shot.id, comment.id,
                                        new retrofit.Callback<Void>() {
                                    @Override
                                    public void success(Void voyd, Response response) { }

                                    @Override
                                    public void failure(RetrofitError error) { }
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
                    dribbbleApi.getCommentLikes(shot.id, comment.id,
                            new retrofit.Callback<List<Like>>() {
                        @Override
                        public void success(List<Like> likes, Response response) {
                            // TODO something better than this.
                            StringBuilder sb = new StringBuilder("Liked by:\n\n");
                            for (Like like : likes) {
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
                        public void failure(RetrofitError error) {
                            Log.e("GET COMMENT LIKES", error.getMessage(), error);
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
