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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.plaidapp.R;
import io.plaidapp.data.api.materialup.model.Post;
import io.plaidapp.data.prefs.MaterialUpPrefs;
import io.plaidapp.ui.recyclerview.InsetDividerDecoration;
import io.plaidapp.ui.recyclerview.SlideInItemAnimator;
import io.plaidapp.ui.transitions.FabTransform;
import io.plaidapp.ui.widget.AuthorTextView;
import io.plaidapp.ui.widget.CheckableImageButton;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.FABToggle;
import io.plaidapp.ui.widget.FabOverlapTextView;
import io.plaidapp.ui.widget.ForegroundImageView;
import io.plaidapp.ui.widget.ParallaxScrimageView;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.ImeUtils;
import io.plaidapp.util.TransitionUtils;
import io.plaidapp.util.ViewOffsetHelper;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.CircleTransform;
import io.plaidapp.util.glide.GlideUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.plaidapp.util.AnimUtils.getFastOutSlowInInterpolator;
import static io.plaidapp.util.AnimUtils.getLinearOutSlowInInterpolator;

public class MaterialUpPost extends Activity {

    public final static String EXTRA_POST = "EXTRA_POST";
    public final static String RESULT_EXTRA_POST_ID = "RESULT_EXTRA_POST_ID";
    private static final int RC_LOGIN_LIKE = 0;
    private static final int RC_LOGIN_COMMENT = 1;
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @BindView(R.id.draggable_frame)
    ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.back)
    ImageButton back;
    @BindView(R.id.material_up_post)
    ParallaxScrimageView imageView;
    @BindView(R.id.material_up_comments)
    RecyclerView commentsList;
    @BindView(R.id.fab_upvote)
    FABToggle fab;
    private View materialUpPostDescription;
    private View postSpacer;
    private View title;
    private View description;
    private LinearLayout shotActions;
    private Button likeCount;
    private Button viewCount;
    private Button share;
    private TextView playerName;
    private ImageView playerAvatar;
    private TextView shotTimeAgo;
    private View commentFooter;
    private ImageView userAvatar;
    private EditText enterComment;
    private ImageButton postComment;

    private Post post;
    private int fabOffset;
    private MaterialUpPrefs materialUpPrefs;
    private boolean performingLike;
    private boolean allowComment;
    private CircleTransform circleTransform;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private CommentsAdapter adapter;
    private CommentAnimator commentAnimator;
    @BindDimen(R.dimen.large_avatar_size)
    int largeAvatarSize;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_up_post);
        materialUpPrefs = MaterialUpPrefs.get(this);
        getWindow().getSharedElementReturnTransition().addListener(shotReturnHomeListener);
        circleTransform = new CircleTransform(this);
        ButterKnife.bind(this);
        materialUpPostDescription = getLayoutInflater().inflate(R.layout.material_up_post_description,
                commentsList, false);
        postSpacer = materialUpPostDescription.findViewById(R.id.post_spacer);
        title = materialUpPostDescription.findViewById(R.id.post_title);
        description = materialUpPostDescription.findViewById(R.id.post_description);
        shotActions = (LinearLayout) materialUpPostDescription.findViewById(R.id.post_actions);
        likeCount = (Button) materialUpPostDescription.findViewById(R.id.post_like_count);
        viewCount = (Button) materialUpPostDescription.findViewById(R.id.post_view_count);
        share = (Button) materialUpPostDescription.findViewById(R.id.post_share_action);
        playerName = (TextView) materialUpPostDescription.findViewById(R.id.player_name);
        playerAvatar = (ImageView) materialUpPostDescription.findViewById(R.id.player_avatar);
        shotTimeAgo = (TextView) materialUpPostDescription.findViewById(R.id.post_time_ago);

        commentsList.addOnScrollListener(scrollListener);
        commentsList.setOnFlingListener(flingListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultAndFinish();
            }
        });
        fab.setOnClickListener(fabClick);
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            @Override
            public void onDragDismissed() {
                setResultAndFinish();
            }
        };

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_POST)) {
            post = intent.getParcelableExtra(EXTRA_POST);
            bindShot(true);
            setupCommenting();//need to post object, so invoke here, after post initialization
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
        outContent.setWebUri(Uri.parse(post.url));
    }

    private void bindShot(final boolean postponeEnterTransition) {
        final Resources res = getResources();

        // load the main image
        // final int[] imageSize = post.images.bestSize();
        Glide.with(this)
                .load(post.getThumbnails().getPreviewUrl())
                .listener(postLoadListener)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                //  .override(imageSize[0], imageSize[1])
                .into(imageView);
        imageView.setOnClickListener(shotClick);
        postSpacer.setOnClickListener(shotClick);

        if (postponeEnterTransition) postponeEnterTransition();
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                calculateFabPosition();
                enterAnimation();
                if (postponeEnterTransition) startPostponedEnterTransition();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ((FabOverlapTextView) title).setText(post.getName());
        } else {
            ((TextView) title).setText(post.getName());
        }
        if (!TextUtils.isEmpty(post.getLabel())) {
            final Spanned descText = post.getParsedDescription(
                    ContextCompat.getColorStateList(this, R.color.material_up_links),
                    ContextCompat.getColor(this, R.color.material_up_link_highlight));
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
                        post.getUpvotesCount(),
                        nf.format(post.getUpvotesCount())));
        likeCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AnimatedVectorDrawable) likeCount.getCompoundDrawables()[1]).start();
                if (post.getUpvotesCount() > 0) {
                    //   PlayerSheet.start(MaterialUpPost.this, post);
                }
            }
        });
        if (post.getUpvotesCount() == 0) {
            likeCount.setBackground(null); // clear touch ripple if doesn't do anything
        }
        viewCount.setText(
                res.getQuantityString(R.plurals.views,
                        post.getViewCount(),
                        nf.format(post.getViewCount())));
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
                new ShareMaterialUpImageTask(MaterialUpPost.this, post).execute();
            }
        });
        if (post.getMakers() != null && post.getMakers().size() > 0 && post.getMakers().get(0) != null) {
            playerName.setText(post.getMakers().get(0).getFullName().toLowerCase());
            Glide.with(this)
                    .load(post.getMakers().get(0).getAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .into(playerAvatar);
            View.OnClickListener playerClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MaterialUpPost.this, "Profile Detail Not yet supported by MaterialUp API", Toast.LENGTH_SHORT).show();
                }
            };
            playerAvatar.setOnClickListener(playerClick);
            playerName.setOnClickListener(playerClick);
            if (post.getPublishedAt() != null) {
                try {
                    shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(post.getPublishedAt().substring(0, 19)).getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS).toString().toLowerCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            playerName.setVisibility(View.GONE);
            playerAvatar.setVisibility(View.GONE);
            shotTimeAgo.setVisibility(View.GONE);
        }

        adapter = new CommentsAdapter(materialUpPostDescription, commentFooter, post.getCommentsCount(),
                getResources().getInteger(R.integer.comment_expand_collapse_duration));
        commentsList.setAdapter(adapter);
        commentsList.addItemDecoration(new InsetDividerDecoration(
                CommentViewHolder.class,
                res.getDimensionPixelSize(R.dimen.divider_height),
                res.getDimensionPixelSize(R.dimen.keyline_1),
                ContextCompat.getColor(this, R.color.divider)));
        commentAnimator = new CommentAnimator();
        commentsList.setItemAnimator(commentAnimator);
        if (post.getCommentsCount() != 0) {
            loadComments();
        }
        checkLiked();
    }

    private void setupCommenting() {
        allowComment = !materialUpPrefs.isLoggedIn()
                || (materialUpPrefs.isLoggedIn() && materialUpPrefs.userCanPost());
        if (allowComment && commentFooter == null) {
            commentFooter = getLayoutInflater().inflate(R.layout.material_up_enter_comment,
                    commentsList, false);
            userAvatar = (ForegroundImageView) commentFooter.findViewById(R.id.avatar);
            enterComment = (EditText) commentFooter.findViewById(R.id.comment);
            postComment = (ImageButton) commentFooter.findViewById(R.id.post_comment);
            enterComment.setOnFocusChangeListener(enterCommentFocus);
        } else if (!allowComment && commentFooter != null) {
            adapter.removeCommentingFooter();
            commentFooter = null;
            Toast.makeText(getApplicationContext(),
                    R.string.prospects_cant_post, Toast.LENGTH_SHORT).show();
        }

        if (allowComment
                && materialUpPrefs.isLoggedIn() && post.getMakers() != null
                && post.getMakers().size() != 0
                && !TextUtils.isEmpty(post.getMakers().get(0).getAvatarUrl())) {
            Glide.with(this)
                    .load(post.getMakers().get(0).getAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.ic_player)
                    .into(userAvatar);
        }
    }

    private View.OnClickListener shotClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openLink(post.url);
        }
    };

    /**
     * We run a transition to expand/collapse comments. Scrolling the RecyclerView while this is
     * running causes issues, so we consume touch events while the transition runs.
     */
    private View.OnTouchListener touchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    private void openLink(String url) {
        CustomTabActivityHelper.openCustomTab(
                MaterialUpPost.this,
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(MaterialUpPost.this, R.color.material_up))
                        .addDefaultShareMenuItem()
                        .build(),
                Uri.parse(url));
    }

    private RequestListener postLoadListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, MaterialUpPost.this.getResources().getDisplayMetrics());
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
                                        MaterialUpPost.this, R.color.dark_icon));
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
                                        getFastOutSlowInInterpolator(MaterialUpPost.this));
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
                            postSpacer.setBackground(
                                    ViewUtils.createRipple(palette, 0.25f, 0.5f,
                                            ContextCompat.getColor(MaterialUpPost.this, R.color.mid_grey),
                                            true));
                            // slightly more opaque ripple on the pinned image to compensate
                            // for the scrim
                            imageView.setForeground(
                                    ViewUtils.createRipple(palette, 0.3f, 0.6f,
                                            ContextCompat.getColor(MaterialUpPost.this, R.color.mid_grey),
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

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final int scrollY = materialUpPostDescription.getTop();
            imageView.setOffset(scrollY);
            fab.setOffset(fabOffset + scrollY);
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

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(MaterialUpPost.this, "\"Like\" is not yet supported by MaterialUp API", Toast.LENGTH_SHORT).show();
            if (materialUpPrefs.isLoggedIn()) {
                fab.toggle();
                doLike();
            } else {
                final Intent login = new Intent(MaterialUpPost.this, DribbbleLogin.class);
                FabTransform.addExtras(login, ContextCompat.getColor(MaterialUpPost.this, R
                        .color.material_up), R.drawable.ic_heart_empty_56dp);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                        (MaterialUpPost.this, fab, getString(R.string.transition_dribbble_login));
                startActivityForResult(login, RC_LOGIN_LIKE, options.toBundle());
            }
        }
    };

    private Transition.TransitionListener shotReturnHomeListener =
            new TransitionUtils.TransitionListenerAdapter() {
                @Override
                public void onTransitionStart(Transition transition) {
                    super.onTransitionStart(transition);
                    imageView.setElevation(1f);
                    back.setElevation(0f);
                }
            };

    private void loadComments() {
        final Call<List<io.plaidapp.data.api.materialup.model.Comment>> commentsCall =
                materialUpPrefs.getApi().getComments(new BigDecimal(post.id).intValueExact());
        commentsCall.enqueue(new Callback<List<io.plaidapp.data.api.materialup.model.Comment>>() {
            @Override
            public void onResponse(Call<List<io.plaidapp.data.api.materialup.model.Comment>> call, Response<List<io.plaidapp.data.api.materialup.model.Comment>> response) {
                final List<io.plaidapp.data.api.materialup.model.Comment> comments = response.body();
                if (comments != null && !comments.isEmpty()) {
                    adapter.addComments(comments);
                }
            }

            @Override
            public void onFailure(Call<List<io.plaidapp.data.api.materialup.model.Comment>> call, Throwable t) {
                Log.d("onFailure", "onFailure: " + t.getMessage());
            }
        });
    }

    private void setResultAndFinish() {
        back.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);
        final Intent resultData = new Intent();
        resultData.putExtra(RESULT_EXTRA_POST_ID, post.id);
        setResult(RESULT_OK, resultData);
        finishAfterTransition();
    }

    private void calculateFabPosition() {
        // calculate 'natural' position i.e. with full height image. Store it for use when scrolling
        fabOffset = imageView.getHeight() + title.getHeight() - (fab.getHeight() / 2);
        fab.setOffset(fabOffset);

        // calculate min position i.e. pinned to the collapsed image when scrolled
        fab.setMinOffset(imageView.getMinimumHeight() - (fab.getHeight() / 2));
    }

    /**
     * Animate in the title, description and author – can't do this in the window enter transition
     * as they get added to the RecyclerView later so do it manually.  Also animate the FAB
     * translation here so that it plays nicely with #calculateFabPosition
     **/
    private void enterAnimation() {
        Interpolator interp = getFastOutSlowInInterpolator(this);
        int offset = title.getHeight();
        viewEnterAnimation(title, offset, interp);
        if (description.getVisibility() == View.VISIBLE) {
            offset *= 1.5f;
            viewEnterAnimation(description, offset, interp);
        }
        offset *= 1.5f;
        fabEnterAnimation(interp, offset);
        offset *= 1.5f;
        viewEnterAnimation(shotActions, offset, interp);
        offset *= 1.5f;
        viewEnterAnimation(playerName, offset, interp);
        viewEnterAnimation(playerAvatar, offset, interp);
        viewEnterAnimation(shotTimeAgo, offset, interp);
        back.animate()
                .alpha(1f)
                .setDuration(600L)
                .setInterpolator(interp)
                .start();
    }

    private void viewEnterAnimation(View view, float offset, Interpolator interp) {
        view.setTranslationY(offset);
        view.setAlpha(0.6f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600L)
                .setInterpolator(interp)
                .setListener(null)
                .start();
    }

    private void fabEnterAnimation(Interpolator interp, int offset) {
        // FAB should enter upwards with content and also scale/fade. As the FAB uses
        // translationY to position itself on the title seam, we can animating this property.
        // Instead animate the view's layout position (which is a bit more involved).
        final ViewOffsetHelper fabOffset = new ViewOffsetHelper(fab);
        final View.OnLayoutChangeListener fabLayout = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int
                    oldLeft, int oldTop, int oldRight, int oldBottom) {
                fabOffset.onViewLayout();
            }
        };

        fab.addOnLayoutChangeListener(fabLayout);
        fabOffset.setTopAndBottomOffset(offset);
        Animator fabMovement = ObjectAnimator.ofInt(fabOffset, ViewOffsetHelper.OFFSET_Y, 0);
        fabMovement.setDuration(600L);
        fabMovement.setInterpolator(interp);
        fabMovement.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fab.removeOnLayoutChangeListener(fabLayout);
            }
        });
        fabMovement.start();

        Animator showFab = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f));
        showFab.setStartDelay(300L);
        showFab.setDuration(300L);
        showFab.setInterpolator(getLinearOutSlowInInterpolator(this));
        showFab.start();
    }

    private void doLike() {
        //Material up api does not supported like
    }

    private void checkLiked() {
        //Material up api does not supported like

    }

    public void postComment(View view) {
        Toast.makeText(MaterialUpPost.this, "Post Comment Not yet supported by MaterialUp API", Toast.LENGTH_SHORT).show();

    }

    //  private boolean isOP(long playerId) {
    //    return post.getMakers().get(0) != null && post.getMakers().get(0).id == playerId;
    //}

    /* package */ class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int EXPAND = 0x1;
        private static final int COLLAPSE = 0x2;
        private static final int COMMENT_LIKE = 0x3;
        private static final int REPLY = 0x4;

        private final List<io.plaidapp.data.api.materialup.model.Comment> comments = new ArrayList<>();
        private final Transition expandCollapse;
        private final View description;
        private View footer;

        private boolean loading;
        private boolean noComments;
        private int expandedCommentPosition = RecyclerView.NO_POSITION;

        CommentsAdapter(
                @NonNull View description,
                @Nullable View footer,
                long commentCount,
                long expandDuration) {
            this.description = description;
            this.footer = footer;
            noComments = commentCount == 0L;
            loading = !noComments;
            expandCollapse = new AutoTransition();
            expandCollapse.setDuration(expandDuration);
            expandCollapse.setInterpolator(getFastOutSlowInInterpolator(MaterialUpPost.this));
            expandCollapse.addListener(new TransitionUtils.TransitionListenerAdapter() {
                @Override
                public void onTransitionStart(Transition transition) {
                    commentsList.setOnTouchListener(touchEater);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    commentAnimator.setAnimateMoves(true);
                    commentsList.setOnTouchListener(null);
                }
            });
        }

        void addComments(List<io.plaidapp.data.api.materialup.model.Comment> newComments) {
            comments.addAll(newComments);
            loading = false;
            noComments = false;
            notifyDataSetChanged();
        }

        void removeCommentingFooter() {
            if (footer == null) return;
            int footerPos = getItemCount() - 1;
            footer = null;
            notifyItemRemoved(footerPos);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return R.layout.material_up_post_description;
            if (position == 1) {
                if (loading) return R.layout.loading;
                if (noComments) return R.layout.material_up_no_comments;
            }
            if (footer != null) {
                int footerPos = (loading || noComments) ? 2 : comments.size() + 1;
                if (position == footerPos) return R.layout.material_up_enter_comment;
            }
            return R.layout.material_up_comment;
        }

        @Override
        public int getItemCount() {
            int count = 1; // description
            if (!comments.isEmpty()) {
                count += comments.size();
            } else {
                count++; // either loading or no comments
            }
            if (footer != null) count++;
            return count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case R.layout.material_up_post_description:
                    return new SimpleViewHolder(description);
                case R.layout.material_up_comment:
                    return createCommentHolder(parent, viewType);
                case R.layout.loading:
                case R.layout.material_up_no_comments:
                    return new SimpleViewHolder(
                            getLayoutInflater().inflate(viewType, parent, false));
                case R.layout.material_up_enter_comment:
                    return new SimpleViewHolder(footer);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case R.layout.material_up_comment:
                    bindComment((CommentViewHolder) holder, getComment(position));
                    break;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position,
                                     List<Object> partialChangePayloads) {
            if (holder instanceof CommentViewHolder) {
                bindPartialCommentChange(
                        (CommentViewHolder) holder, position, partialChangePayloads);
            } else {
                onBindViewHolder(holder, position);
            }
        }

        private CommentViewHolder createCommentHolder(ViewGroup parent, int viewType) {
            final CommentViewHolder holder = new CommentViewHolder(
                    getLayoutInflater().inflate(viewType, parent, false));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    final io.plaidapp.data.api.materialup.model.Comment comment = getComment(position);
                    TransitionManager.beginDelayedTransition(commentsList, expandCollapse);
                    commentAnimator.setAnimateMoves(false);

                    // collapse any currently expanded items
                    if (expandedCommentPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(expandedCommentPosition, COLLAPSE);
                    }

                    // expand this item (if it wasn't already)
                    if (expandedCommentPosition != position) {
                        expandedCommentPosition = position;
                        notifyItemChanged(position, EXPAND);

                        if (enterComment != null && enterComment.hasFocus()) {
                            enterComment.clearFocus();
                            ImeUtils.hideIme(enterComment);
                        }
                        holder.itemView.requestFocus();
                    } else {
                        expandedCommentPosition = RecyclerView.NO_POSITION;
                    }
                }
            });

            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("holder.avatar", "onClick: ");
                }
            });

            holder.reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;

                    final io.plaidapp.data.api.materialup.model.Comment comment = getComment(position);
                    enterComment.setText("@" + comment.getUser().getFullName() + " ");
                    enterComment.setSelection(enterComment.getText().length());

                    // collapse the comment and scroll the reply box (in the footer) into view
                    expandedCommentPosition = RecyclerView.NO_POSITION;
                    notifyItemChanged(position, REPLY);
                    holder.reply.jumpDrawablesToCurrentState();
                    enterComment.requestFocus();
                    commentsList.smoothScrollToPosition(getItemCount() - 1);
                }
            });

            holder.likeHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("holder.likeHeart", "onClick: ");
                }
            });

            holder.likesCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MaterialUpPost.this, "Not yet supported by MaterialUp API", Toast.LENGTH_SHORT).show();
                }
            });

            return holder;
        }

        private void bindComment(CommentViewHolder holder, io.plaidapp.data.api.materialup.model.Comment comment) {
            final int position = holder.getAdapterPosition();
            final boolean isExpanded = position == expandedCommentPosition;
            Glide.with(MaterialUpPost.this)
                    .load(comment.getUser().getAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .into(holder.avatar);
            holder.author.setText(comment.getUser().getFullName());
            try {
                holder.timeAgo.setText(comment.getCreatedAt() == null ? "" :
                        DateUtils.getRelativeTimeSpanString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(comment.getCreatedAt().substring(0, 19)).getTime(),
                                System.currentTimeMillis(),
                                DateUtils.SECOND_IN_MILLIS)
                                .toString().toLowerCase());
                HtmlUtils.setTextWithNiceLinks(holder.commentBody,
                        comment.getParsedBody(holder.commentBody));
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.likeHeart.setChecked(true);//// TODO: 28/09/16 materialUp api is  devoid of
            holder.likeHeart.setEnabled(!comment.getUser().getNickname().equals(materialUpPrefs.getUserId()));
            holder.likesCount.setText("");
            setExpanded(holder, isExpanded);
        }

        private void setExpanded(CommentViewHolder holder, boolean isExpanded) {
            holder.itemView.setActivated(isExpanded);
            holder.reply.setVisibility((isExpanded && allowComment) ? View.VISIBLE : View.GONE);
            holder.likeHeart.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            holder.likesCount.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

        private void bindPartialCommentChange(
                CommentViewHolder holder, int position, List<Object> partialChangePayloads) {
            // for certain changes we don't need to rebind data, just update some view state
            if ((partialChangePayloads.contains(EXPAND)
                    || partialChangePayloads.contains(COLLAPSE))
                    || partialChangePayloads.contains(REPLY)) {
                setExpanded(holder, position == expandedCommentPosition);
            } else if (partialChangePayloads.contains(COMMENT_LIKE)) {
                return; // nothing to do
            } else {
                onBindViewHolder(holder, position);
            }
        }

        private io.plaidapp.data.api.materialup.model.Comment getComment(int adapterPosition) {
            return comments.get(adapterPosition - 1); // description
        }
    }

    /* package */ static class SimpleViewHolder extends RecyclerView.ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    /* package */ static class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.player_avatar)
        ImageView avatar;
        @BindView(R.id.comment_author)
        AuthorTextView author;
        @BindView(R.id.comment_time_ago)
        TextView timeAgo;
        @BindView(R.id.comment_text)
        TextView commentBody;
        @BindView(R.id.comment_reply)
        ImageButton reply;
        @BindView(R.id.comment_like)
        CheckableImageButton likeHeart;
        @BindView(R.id.comment_likes_count)
        TextView likesCount;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * A {@link RecyclerView.ItemAnimator} which allows disabling move animations. RecyclerView
     * does not like animating item height changes. {@link android.transition.ChangeBounds} allows
     * this but in order to simultaneously collapse one item and expand another, we need to run the
     * Transition on the entire RecyclerView. As such it attempts to move views around. This
     * custom item animator allows us to stop RecyclerView from trying to handle this for us while
     * the transition is running.
     */
    /* package */ static class CommentAnimator extends SlideInItemAnimator {

        private boolean animateMoves = false;

        CommentAnimator() {
            super();
        }

        void setAnimateMoves(boolean animateMoves) {
            this.animateMoves = animateMoves;
        }

        @Override
        public boolean animateMove(
                RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            if (!animateMoves) {
                dispatchMoveFinished(holder);
                return false;
            }
            return super.animateMove(holder, fromX, fromY, toX, toY);
        }
    }

}
