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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.SharedElementCallback;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TextAppearanceSpan;
import android.transition.ArcMotion;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import in.uncod.android.bypass.style.ImageLoadingSpan;
import io.plaidapp.BuildConfig;
import io.plaidapp.R;
import io.plaidapp.data.api.ClientAuthInterceptor;
import io.plaidapp.data.api.designernews.DesignerNewsService;
import io.plaidapp.data.api.designernews.UpvoteStoryService;
import io.plaidapp.data.api.designernews.model.Comment;
import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.data.api.designernews.model.StoryResponse;
import io.plaidapp.data.prefs.DesignerNewsPrefs;
import io.plaidapp.ui.drawable.ThreadedCommentDrawable;
import io.plaidapp.ui.transitions.FabDialogMorphSetup;
import io.plaidapp.ui.widget.AuthorTextView;
import io.plaidapp.ui.widget.CollapsingTitleLayout;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.FontTextView;
import io.plaidapp.ui.widget.PinnedOffsetView;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.ImageUtils;
import io.plaidapp.util.ImeUtils;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.CircleTransform;
import io.plaidapp.util.glide.ImageSpanTarget;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DesignerNewsStory extends Activity {

    protected static final String EXTRA_STORY = "story";
    private static final int RC_LOGIN_UPVOTE = 7;

    private View header;
    @Bind(R.id.comments_list) RecyclerView commentsList;
    private LinearLayoutManager layoutManager;
    private DesignerNewsCommentsAdapter commentsAdapter;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.fab_expand) View fabExpand;
    @Bind(R.id.comments_container) ElasticDragDismissFrameLayout draggableFrame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    @Nullable @Bind(R.id.backdrop_toolbar) CollapsingTitleLayout collapsingToolbar;
    @Nullable @Bind(R.id.story_title_background) PinnedOffsetView toolbarBackground;
    private Button upvoteStory;
    private EditText enterComment;
    private ImageButton postComment;
    @BindInt(R.integer.fab_expand_duration) int fabExpandDuration;
    @BindDimen(R.dimen.comment_thread_width) int threadWidth;
    @BindDimen(R.dimen.comment_thread_gap) int threadGap;

    private Story story;
    private DesignerNewsPrefs designerNewsPrefs;
    private DesignerNewsService designerNewsApi;
    private Bypass markdown;
    private CustomTabActivityHelper customTab;
    private CircleTransform circleTransform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer_news_story);
        ButterKnife.bind(this);
        getWindow().getSharedElementReturnTransition().addListener(returnHomeListener);

        story = getIntent().getParcelableExtra(EXTRA_STORY);
        fab.setOnClickListener(fabClick);

        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(getWindow()) {
            @Override
            public void onDragDismissed() {
                finishAfterTransition();
            }
        };

        markdown = new Bypass(this, new Bypass.Options()
                .setBlockQuoteLineColor(
                        ContextCompat.getColor(this, R.color.designer_news_quote_line))
                .setBlockQuoteLineWidth(2) // dps
                .setBlockQuoteLineIndent(8) // dps
                .setPreImageLinebreakHeight(4) //dps
                .setBlockQuoteIndentSize(TypedValue.COMPLEX_UNIT_DIP, 2f)
                .setBlockQuoteTextColor(ContextCompat.getColor(this, R.color.designer_news_quote)));
        circleTransform = new CircleTransform(this);

        designerNewsPrefs = DesignerNewsPrefs.get(this);
        createDesignerNewsApi();

        layoutManager = new LinearLayoutManager(this);
        commentsList.setLayoutManager(layoutManager);

        header = getLayoutInflater().inflate(
                R.layout.designer_news_story_description, commentsList, false);
        bindDescription();

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.story_toolbar);
        if (collapsingToolbar != null) { // portrait: collapsing toolbar
            collapsingToolbar.setTitle(story.title);
            collapsingToolbar.addOnLayoutChangeListener(titlebarLayout);
        } else { // landscape: scroll toolbar with content
            toolbar = (Toolbar) header.findViewById(R.id.story_toolbar);
            FontTextView title = (FontTextView) toolbar.findViewById(R.id.story_title);
            title.setText(story.title);
        }
        commentsList.addOnScrollListener(headerScrollListener);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        View enterCommentView = setupCommentField();

        if (story.comment_count > 0) {
            // flatten the comments from a nested structure {@see Comment#comments} to an
            // array for our adapter (saving the depth).
            List<ThreadedComment> wrapped = new ArrayList<>(story.comment_count);
            addComments(story.comments, 0, wrapped);
            commentsAdapter =
                    new DesignerNewsCommentsAdapter(header, wrapped, enterCommentView);
            commentsList.setAdapter(commentsAdapter);

        } else {
            commentsAdapter = new DesignerNewsCommentsAdapter(
                    header, new ArrayList<ThreadedComment>(0), enterCommentView);
            commentsList.setAdapter(commentsAdapter);
        }
        customTab = new CustomTabActivityHelper();
        customTab.setConnectionCallback(customTabConnect);
        setEnterSharedElementCallback(sharedEnterCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTab.bindCustomTabsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // clean up after any fab expansion
        fab.setAlpha(1f);
        fabExpand.setVisibility(View.INVISIBLE);
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_LOGIN_UPVOTE:
                if (resultCode == RESULT_OK) {
                    upvoteStory();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    protected void onStop() {
        customTab.unbindCustomTabsService(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        customTab.setConnectionCallback(null);
        super.onDestroy();
    }

    @Override @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        outContent.setWebUri(Uri.parse(story.url));
    }

    public static CustomTabsIntent.Builder getCustomTabIntent(@NonNull Context context,
                                                              @NonNull Story story,
                                                              @Nullable CustomTabsSession session) {
        Intent upvoteStory = new Intent(context, UpvoteStoryService.class);
        upvoteStory.setAction(UpvoteStoryService.ACTION_UPVOTE);
        upvoteStory.putExtra(UpvoteStoryService.EXTRA_STORY_ID, story.id);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, upvoteStory, 0);
        return new CustomTabsIntent.Builder(session)
                .setToolbarColor(ContextCompat.getColor(context, R.color.designer_news))
                .setActionButton(ImageUtils.vectorToBitmap(context,
                                R.drawable.ic_upvote_filled_24dp_white),
                        context.getString(R.string.upvote_story),
                        pendingIntent,
                        false)
                .setShowTitle(true)
                .enableUrlBarHiding();
    }

    private final CustomTabActivityHelper.ConnectionCallback customTabConnect
            = new CustomTabActivityHelper.ConnectionCallback() {

        @Override
        public void onCustomTabsConnected() {
            customTab.mayLaunchUrl(Uri.parse(story.url), null, null);
        }

        @Override public void onCustomTabsDisconnected() { }
    };

    private RecyclerView.OnScrollListener headerScrollListener
            = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            updateScrollDependentUi();
        }
    };

    private void updateScrollDependentUi() {
        // feed scroll events to the header
        if (collapsingToolbar != null) {
            final int headerScroll = header.getTop() - commentsList.getPaddingTop();
            collapsingToolbar.setScrollPixelOffset(-headerScroll);
            toolbarBackground.setOffset(headerScroll);
        }
        updateFabVisibility();
    }

    private boolean fabIsVisible = true;
    private void updateFabVisibility() {
        // the FAB position can interfere with the enter comment field. Hide the FAB if:
        // - The comment field is scrolled onto screen
        // - The comment field is focused (i.e. stories with no/few comments might not push the
        //   enter comment field off-screen so need to make sure the button is accessible
        final boolean enterCommentFocused = enterComment.isFocused();
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        final int footerPosition = commentsAdapter.getItemCount() - 1;
        final boolean footerVisible = lastVisibleItemPosition == footerPosition;

        final boolean fabShouldBeVisible =
                (firstVisibleItemPosition == 0 && !enterCommentFocused) || !footerVisible;

        if (!fabShouldBeVisible && fabIsVisible) {
            fabIsVisible = false;
            fab.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0.6f)
                    .setDuration(200L)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.fast_out_linear_in))
                    .withLayer()
                    .setListener(postHideFab)
                    .start();
        } else if (fabShouldBeVisible && !fabIsVisible) {
            fabIsVisible = true;
            fab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200L)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.linear_out_slow_in))
                    .withLayer()
                    .setListener(preShowFab)
                    .start();
            ImeUtils.hideIme(enterComment);
        }
    }

    private AnimatorListenerAdapter preShowFab = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            fab.setVisibility(View.VISIBLE);
        }
    };

    private AnimatorListenerAdapter postHideFab = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            fab.setVisibility(View.GONE);
        }
    };

    // title can expand up to a max number of lines.  If it does then adjust the list padding
    // & reset scroll trackers
    private View.OnLayoutChangeListener titlebarLayout = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int
                oldLeft, int oldTop, int oldRight, int oldBottom) {
            if ((bottom - top) != (oldBottom - oldTop)) {
                commentsList.setPaddingRelative(commentsList.getPaddingStart(),
                        collapsingToolbar.getHeight(),
                        commentsList.getPaddingEnd(),
                        commentsList.getPaddingBottom());
                commentsList.scrollToPosition(0);
                collapsingToolbar.setScrollPixelOffset(0);
                toolbarBackground.setOffset(0);
            }
            updateScrollDependentUi();
        }
    };

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doFabExpand();
            CustomTabActivityHelper.openCustomTab(
                    DesignerNewsStory.this,
                    getCustomTabIntent(DesignerNewsStory.this, story,
                            customTab.getSession())
                            .setStartAnimations(getApplicationContext(),
                                    R.anim.chrome_custom_tab_enter,
                                    R.anim.fade_out_rapidly)
                            .build(),
                    Uri.parse(story.url));
        }
    };

    private SharedElementCallback sharedEnterCallback = new SharedElementCallback() {
        @Override
        public void onSharedElementEnd(List<String> sharedElementNames,
                                       List<View> sharedElements,
                                       List<View> sharedElementSnapshots) {
            // force a remeasure to account for shared element shenanigans
            if (collapsingToolbar != null) {
                collapsingToolbar.measure(
                        View.MeasureSpec.makeMeasureSpec(draggableFrame.getWidth(),
                                View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(draggableFrame.getWidth(),
                                View.MeasureSpec.AT_MOST));
                collapsingToolbar.requestLayout();
            }
            if (toolbarBackground != null) {
                toolbarBackground.measure(
                        View.MeasureSpec.makeMeasureSpec(draggableFrame.getWidth(),
                                View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(draggableFrame.getWidth(),
                                View.MeasureSpec.AT_MOST));
                toolbarBackground.requestLayout();
            }
        }
    };

    private Transition.TransitionListener returnHomeListener = new AnimUtils
            .TransitionListenerAdapter() {
        @Override
        public void onTransitionStart(Transition transition) {
            super.onTransitionStart(transition);
            // hide the fab as for some reason it jumps position??  TODO work out why
            fab.setVisibility(View.INVISIBLE);
        }
    };

    private void doFabExpand() {
        // translate the chrome placeholder ui so that it is centered on the FAB
        int fabCenterX = (fab.getLeft() + fab.getRight()) / 2;
        int fabCenterY = ((fab.getTop() + fab.getBottom()) / 2) - fabExpand.getTop();
        int translateX = fabCenterX - (fabExpand.getWidth() / 2);
        int translateY = fabCenterY - (fabExpand.getHeight() / 2);
        fabExpand.setTranslationX(translateX);
        fabExpand.setTranslationY(translateY);

        // then reveal the placeholder ui, starting from the center & same dimens as fab
        fabExpand.setVisibility(View.VISIBLE);
        Animator reveal = ViewAnimationUtils.createCircularReveal(
                fabExpand,
                fabExpand.getWidth() / 2,
                fabExpand.getHeight() / 2,
                fab.getWidth() / 2,
                (int) Math.hypot(fabExpand.getWidth() / 2, fabExpand.getHeight() / 2))
                .setDuration(fabExpandDuration);

        // translate the placeholder ui back into position along an arc
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(translateX, translateY, 0, 0);
        Animator position = ObjectAnimator.ofFloat(fabExpand, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(fabExpandDuration);

        // animate from the FAB colour to the placeholder background color
        Animator background = ObjectAnimator.ofArgb(fabExpand,
                ViewUtils.BACKGROUND_COLOR,
                ContextCompat.getColor(this, R.color.designer_news),
                ContextCompat.getColor(this, R.color.background_light))
                .setDuration(fabExpandDuration);

        // fade out the fab (rapidly)
        Animator fadeOutFab = ObjectAnimator.ofFloat(fab, View.ALPHA, 0f)
                .setDuration(60);

        // play 'em all together with the material interpolator
        AnimatorSet show = new AnimatorSet();
        show.setInterpolator(AnimUtils.getMaterialInterpolator(DesignerNewsStory.this));
        show.playTogether(reveal, background, position, fadeOutFab);
        show.start();
    }

    private void bindDescription() {
        final TextView storyComment = (TextView) header.findViewById(R.id.story_comment);
        if (!TextUtils.isEmpty(story.comment)) {
            HtmlUtils.setTextWithNiceLinks(storyComment, markdown.markdownToSpannable(story
                    .comment, storyComment, new Bypass.LoadImageCallback() {
                @Override
                public void loadImage(String src, ImageLoadingSpan loadingSpan) {
                    Glide.with(DesignerNewsStory.this)
                            .load(src)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new ImageSpanTarget(storyComment, loadingSpan));
                }
            }));
        } else {
            storyComment.setVisibility(View.GONE);
        }

        upvoteStory = (Button) header.findViewById(R.id.story_vote_action);
        upvoteStory.setText(getResources().getQuantityString(R.plurals.upvotes, story.vote_count,
                NumberFormat.getInstance().format(story.vote_count)));
        upvoteStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                upvoteStory();
            }
        });

        Button share = (Button) header.findViewById(R.id.story_share_action);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ShareCompat.IntentBuilder.from(DesignerNewsStory.this)
                        .setText(story.url)
                        .setType("text/plain")
                        .getIntent());
            }
        });

        TextView storyPosterTime = (TextView) header.findViewById(R.id.story_poster_time);
        SpannableString poster = new SpannableString("–" + story.user_display_name);
        poster.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_CommentAuthor),
                0, poster.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence job = !TextUtils.isEmpty(story.user_job) ? "\n" + story.user_job : "";
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(story.created_at.getTime(),
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS);
        storyPosterTime.setText(TextUtils.concat(poster, job, "\n", timeAgo));
        ImageView avatar = (ImageView) header.findViewById(R.id.story_poster_avatar);
        if (!TextUtils.isEmpty(story.user_portrait_url)) {
            Glide.with(this)
                    .load(story.user_portrait_url)
                    .placeholder(R.drawable.avatar_placeholder)
                    .transform(circleTransform)
                    .into(avatar);
        } else {
            avatar.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View setupCommentField() {
        View enterCommentView = getLayoutInflater()
                .inflate(R.layout.designer_news_enter_comment, commentsList, false);
        enterComment = (EditText) enterCommentView.findViewById(R.id.comment);
        postComment = (ImageButton) enterCommentView.findViewById(R.id.post_comment);
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (designerNewsPrefs.isLoggedIn()) {
                    if (TextUtils.isEmpty(enterComment.getText())) return;
                    enterComment.setEnabled(false);
                    postComment.setEnabled(false);
                    designerNewsApi.comment(story.id, enterComment.getText().toString(),
                            new Callback<Comment>() {
                                @Override
                                public void success(Comment comment, Response response) {
                                    enterComment.getText().clear();
                                    enterComment.setEnabled(true);
                                    postComment.setEnabled(true);
                                    ((DesignerNewsCommentsAdapter) commentsList.getAdapter())
                                            .addComment(new ThreadedComment(0, comment));
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Toast.makeText(getApplicationContext(),
                                            "Failed to post comment :(", Toast.LENGTH_SHORT).show();
                                    enterComment.setEnabled(true);
                                    postComment.setEnabled(true);
                                }
                            });
                } else {
                    needsLogin(postComment, 0);
                }
                enterComment.clearFocus();
            }
        });
        enterComment.setOnFocusChangeListener(enterCommentFocus);
        return enterCommentView;
    }

    private void upvoteStory() {
        if (designerNewsPrefs.isLoggedIn()) {
            if (!upvoteStory.isActivated()) {
                upvoteStory.setActivated(true);
                designerNewsApi.upvoteStory(story.id, "",
                        new Callback<StoryResponse>() {
                            @Override
                            public void success(StoryResponse storyResponse, Response
                                    response) {
                                final int newUpvoteCount = storyResponse.story.vote_count;
                                upvoteStory.setText(getResources().getQuantityString(
                                        R.plurals.upvotes, newUpvoteCount,
                                        NumberFormat.getInstance().format(newUpvoteCount)));
                            }

                            @Override public void failure(RetrofitError error) { }
                        });
            } else {
                upvoteStory.setActivated(false);
                // TODO delete upvote. Not available in v1 API.
            }

        } else {
            needsLogin(upvoteStory, RC_LOGIN_UPVOTE);
        }
    }

    private void needsLogin(View triggeringView, int requestCode) {
        Intent login = new Intent(DesignerNewsStory.this,
                DesignerNewsLogin.class);
        login.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR,
                ContextCompat.getColor(DesignerNewsStory.this, R.color.background_light));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                DesignerNewsStory.this,
                triggeringView, getString(R.string.transition_designer_news_login));
        startActivityForResult(login, requestCode, options.toBundle());
    }

    private void createDesignerNewsApi() {
        designerNewsApi = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .setRequestInterceptor(new ClientAuthInterceptor(designerNewsPrefs.getAccessToken(),
                        BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .build()
                .create(DesignerNewsService.class);
    }

    private void addComments(List<Comment> comments, int depth, List<ThreadedComment> wrapped) {
        for (Comment comment : comments) {
            wrapped.add(new ThreadedComment(depth, comment));
            // todo move this to after downloading so only done once
            if (comment.comments != null && comment.comments.size() > 0) {
                addComments(comment.comments, depth + 1, wrapped);
            }
        }
    }

    private View.OnFocusChangeListener enterCommentFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            // kick off an anim (via animated state list) on the post button. see
            // @drawable/ic_add_comment_state
            postComment.setActivated(hasFocus);
            updateFabVisibility();
        }
    };

    private boolean isOP(Long userId) {
        return userId.equals(story.user_id);
    }

    // convenience class used to convert nested comment structure returned from the API to a flat
    // structure with a depth attribute, suitable for showing in a list.
    protected class ThreadedComment {
        final int depth;
        final Comment comment;

        ThreadedComment(int depth,
                        Comment comment) {
            this.depth = depth;
            this.comment = comment;
        }
    }

    /* package */ class DesignerNewsCommentsAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_NO_COMMENTS = 1;
        private static final int TYPE_COMMENT = 2;
        private static final int TYPE_FOOTER = 3;

        private View header;
        private List<ThreadedComment> comments;
        private View footer;

        DesignerNewsCommentsAdapter(@NonNull View header,
                                    @NonNull List<ThreadedComment> comments,
                                    @NonNull View footer) {
            this.header = header;
            this.comments = comments;
            this.footer = footer;
        }

        private boolean hasComments() {
            return !comments.isEmpty();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else if ((hasComments() && position == comments.size() + 1)
                    || (!hasComments() && position == 2)) {
                return TYPE_FOOTER;
            } else {
                return hasComments() ? TYPE_COMMENT : TYPE_NO_COMMENTS;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEADER:
                    return new HeaderHolder(header);
                case TYPE_COMMENT:
                    return new CommentHolder(
                        getLayoutInflater().inflate(R.layout.designer_news_comment, parent, false));
                case TYPE_NO_COMMENTS:
                    return new NoCommentsHolder(
                        getLayoutInflater().inflate(
                                R.layout.designer_news_no_comments, parent, false));
                case TYPE_FOOTER:
                    return new FooterHolder(footer);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_COMMENT) {
                bindComment((CommentHolder) holder, comments.get(position - 1)); // minus header
            } // nothing to bind for header / no comment / footer views
        }

        private void bindComment(final CommentHolder holder, final ThreadedComment comment) {
            HtmlUtils.setTextWithNiceLinks(holder.comment, markdown.markdownToSpannable(comment
                    .comment.body, holder.comment, new Bypass.LoadImageCallback() {
                @Override
                public void loadImage(String src, ImageLoadingSpan loadingSpan) {
                    Glide.with(DesignerNewsStory.this)
                            .load(src)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new ImageSpanTarget(holder.comment, loadingSpan));
                }
            }));
            holder.author.setText(comment.comment.user_display_name);
            holder.author.setOriginalPoster(isOP(comment
                    .comment.user_id));
            holder.timeAgo.setText(
                    DateUtils.getRelativeTimeSpanString(comment.comment.created_at.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS));
            ThreadedCommentDrawable depthDrawable = new ThreadedCommentDrawable(threadWidth,
                    threadGap);
            depthDrawable.setDepth(comment.depth);
            holder.threadDepth.setImageDrawable(depthDrawable);
        }

        @Override
        public int getItemCount() {
            // include header & footer (+ no comments view)
            return hasComments() ? comments.size() + 2 : 3;
        }

        public void addComment(ThreadedComment newComment) {
            if (!hasComments()) {
                notifyItemRemoved(1); // remove the no comments view
            }
            comments.add(newComment);
            notifyItemInserted(comments.size());
        }
    }

    /* package */ static class CommentHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.depth) ImageView threadDepth;
        @Bind(R.id.comment_author) AuthorTextView author;
        @Bind(R.id.comment_time_ago) TextView timeAgo;
        @Bind(R.id.comment_text) TextView comment;

        public CommentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /* package */ static class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    /* package */ static class NoCommentsHolder extends RecyclerView.ViewHolder {

        public NoCommentsHolder(View itemView) {
            super(itemView);
        }
    }

    /* package */ static class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}
