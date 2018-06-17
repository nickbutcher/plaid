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

package io.plaidapp.ui.designernews.story;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import static io.plaidapp.util.AnimUtils.getFastOutLinearInInterpolator;
import static io.plaidapp.util.AnimUtils.getFastOutSlowInInterpolator;
import static io.plaidapp.util.AnimUtils.getLinearOutSlowInInterpolator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.app.assist.AssistContent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TextAppearanceSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.uncod.android.bypass.Bypass;
import io.plaidapp.activities.R;
import io.plaidapp.designernews.DesignerNewsPrefs;
import io.plaidapp.designernews.data.api.model.Comment;
import io.plaidapp.designernews.data.api.model.Story;
import io.plaidapp.designernews.data.api.model.User;
import io.plaidapp.ui.designernews.DesignerNewsLogin;
import io.plaidapp.ui.drawable.ThreadedCommentDrawable;
import io.plaidapp.ui.transitions.GravityArcMotion;
import io.plaidapp.ui.transitions.MorphTransform;
import io.plaidapp.ui.transitions.ReflowText;
import io.plaidapp.ui.widget.CollapsingTitleLayout;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.PinnedOffsetView;
import io.plaidapp.util.Activities;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.ImeUtils;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.GlideApp;
import io.plaidapp.util.glide.ImageSpanTarget;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DesignerNewsStory extends Activity {

    private static final int RC_LOGIN_UPVOTE = 7;

    private View header;
    private RecyclerView commentsList;
    private LinearLayoutManager layoutManager;
    private DesignerNewsCommentsAdapter commentsAdapter;
    private ImageButton fab;
    private View fabExpand;
    private ElasticDragDismissFrameLayout draggableFrame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    @Nullable
    private CollapsingTitleLayout collapsingToolbar;
    @Nullable
    private PinnedOffsetView toolbarBackground;
    @Nullable
    private View background;
    private TextView upvoteStory;
    private EditText enterComment;
    private ImageButton postComment;
    private int fabExpandDuration;
    private int threadWidth;
    private int threadGap;
    private View enterCommentView;

    private Story story;

    private DesignerNewsPrefs designerNewsPrefs;
    private Bypass markdown;
    private CustomTabActivityHelper customTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer_news_story);

        bindResources();

        story = getIntent().getParcelableExtra(Activities.DesignerNews.Story.EXTRA_STORY);

        fab.setOnClickListener(fabClick);
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);
        markdown = new Bypass(this, new Bypass.Options()
                .setBlockQuoteLineColor(
                        ContextCompat.getColor(this, io.plaidapp.R.color.designer_news_quote_line))
                .setBlockQuoteLineWidth(2) // dps
                .setBlockQuoteLineIndent(8) // dps
                .setPreImageLinebreakHeight(4) //dps
                .setBlockQuoteIndentSize(TypedValue.COMPLEX_UNIT_DIP, 2f)
                .setBlockQuoteTextColor(
                        ContextCompat.getColor(this, io.plaidapp.R.color.designer_news_quote)));
        designerNewsPrefs = DesignerNewsPrefs.get(this);
        layoutManager = new LinearLayoutManager(this);
        commentsList.setLayoutManager(layoutManager);
        commentsList.setItemAnimator(new CommentAnimator(
                getResources().getInteger(io.plaidapp.R.integer.comment_expand_collapse_duration)));
        header = getLayoutInflater().inflate(
                R.layout.designer_news_story_description, commentsList, false);
        bindDescription();

        // setup title/toolbar
        if (collapsingToolbar != null) { // narrow device: collapsing toolbar
            collapsingToolbar.addOnLayoutChangeListener(titlebarLayout);
            collapsingToolbar.setTitle(story.title);
            final Toolbar toolbar = findViewById(R.id.story_toolbar);
            toolbar.setNavigationOnClickListener(backClick);
            commentsList.addOnScrollListener(headerScrollListener);

            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View>
                        sharedElements, List<View> sharedElementSnapshots) {
                    ReflowText.setupReflow(getIntent(), collapsingToolbar);
                }

                @Override
                public void onSharedElementEnd(List<String> sharedElementNames, List<View>
                        sharedElements, List<View> sharedElementSnapshots) {
                    ReflowText.setupReflow(collapsingToolbar);
                }
            });

        } else { // w600dp configuration: content card scrolls over title bar
            final TextView title = findViewById(R.id.story_title);
            title.setText(story.title);
            findViewById(R.id.back).setOnClickListener(backClick);
        }

        enterCommentView = setupCommentField();
        commentsAdapter = new DesignerNewsCommentsAdapter(
                header, new ArrayList<>(0), enterCommentView);
        commentsList.setAdapter(commentsAdapter);

        customTab = new CustomTabActivityHelper();
        customTab.setConnectionCallback(customTabConnect);
    }

    private void setupComments(View enterCommentView, List<Comment> comments) {
        if (comments.size() > 0) {
            // flatten the comments from a nested structure {@see Comment#comments} to a
            // list appropriate for our adapter (using the depth attribute).
            List<Comment> flattened = new ArrayList<>(story.comment_count);
            unnestComments(comments, flattened);
            commentsAdapter =
                    new DesignerNewsCommentsAdapter(header, comments, enterCommentView);
            commentsList.setAdapter(commentsAdapter);

        }
    }

    private void bindResources() {
        commentsList = findViewById(R.id.comments_list);
        fab = findViewById(R.id.fab);
        fabExpand = findViewById(R.id.fab_expand);
        draggableFrame = findViewById(R.id.comments_container);
        collapsingToolbar = findViewById(R.id.backdrop_toolbar);
        toolbarBackground = findViewById(R.id.story_title_background);
        background = findViewById(R.id.background);
        Resources res = getResources();
        fabExpandDuration = res.getInteger(io.plaidapp.R.integer.fab_expand_duration);
        threadWidth = res.getDimensionPixelSize(io.plaidapp.R.dimen.comment_thread_width);
        threadGap = res.getDimensionPixelSize(io.plaidapp.R.dimen.comment_thread_gap);
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

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        if (story.url != null) {
            outContent.setWebUri(Uri.parse(story.url));
        }
    }

    private final CustomTabActivityHelper.ConnectionCallback customTabConnect
            = new CustomTabActivityHelper.ConnectionCallback() {

        @Override
        public void onCustomTabsConnected() {
            if (story.url != null) {
                customTab.mayLaunchUrl(Uri.parse(story.url), null, null);
            }
        }

        @Override
        public void onCustomTabsDisconnected() {
        }
    };

    private final RecyclerView.OnScrollListener headerScrollListener
            = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            updateScrollDependentUi();
        }
    };

    private final View.OnClickListener backClick = view -> finishAfterTransition();

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
        // - The comment field is focused (i.e. stories with no/few commentLinks might not push the
        //   enter comment field off-screen so need to make sure the button is accessible
        // - A comment reply field is focused
        final boolean enterCommentFocused = enterComment.isFocused();
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        final int footerPosition = commentsAdapter.getItemCount() - 1;
        final boolean footerVisible = lastVisibleItemPosition == footerPosition;
        final boolean replyCommentFocused = commentsAdapter.isReplyToCommentFocused();

        final boolean fabShouldBeVisible =
                ((firstVisibleItemPosition == 0 && !enterCommentFocused) || !footerVisible)
                        && !replyCommentFocused;

        if (!fabShouldBeVisible && fabIsVisible) {
            fabIsVisible = false;
            fab.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0.6f)
                    .setDuration(200L)
                    .setInterpolator(getFastOutLinearInInterpolator(this))
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
                    .setInterpolator(getLinearOutSlowInInterpolator(this))
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

    // title can expand up to a max number of lines. If it does then adjust UI to reflect
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
            }
            collapsingToolbar.removeOnLayoutChangeListener(this);
        }
    };

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doFabExpand();
            CustomTabActivityHelper.openCustomTab(
                    DesignerNewsStory.this,
                    Activities.DesignerNews.Story.INSTANCE
                            .customTabIntent(DesignerNewsStory.this, story,
                                    customTab.getSession())
                            .setStartAnimations(getApplicationContext(),
                                    io.plaidapp.R.anim.chrome_custom_tab_enter,
                                    io.plaidapp.R.anim.fade_out_rapidly)
                            .build(),
                    Uri.parse(story.url));
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
        GravityArcMotion arcMotion = new GravityArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(translateX, translateY, 0, 0);
        Animator position = ObjectAnimator.ofFloat(fabExpand, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(fabExpandDuration);

        // animate from the FAB colour to the placeholder background color
        Animator background = ObjectAnimator.ofArgb(fabExpand,
                ViewUtils.BACKGROUND_COLOR,
                ContextCompat.getColor(this, io.plaidapp.R.color.designer_news),
                ContextCompat.getColor(this, io.plaidapp.R.color.background_light))
                .setDuration(fabExpandDuration);

        // fade out the fab (rapidly)
        Animator fadeOutFab = ObjectAnimator.ofFloat(fab, View.ALPHA, 0f)
                .setDuration(60);

        // play 'em all together with the material interpolator
        AnimatorSet show = new AnimatorSet();
        show.setInterpolator(getFastOutSlowInInterpolator(DesignerNewsStory.this));
        show.playTogether(reveal, background, position, fadeOutFab);
        show.start();
    }

    private void bindDescription() {
        final TextView storyComment = header.findViewById(R.id.story_comment);
        if (!TextUtils.isEmpty(story.comment)) {
            HtmlUtils.parseMarkdownAndSetText(storyComment, story.comment, markdown,
                    (src, loadingSpan) -> GlideApp.with(DesignerNewsStory.this)
                            .asBitmap()
                            .load(src)
                            .transition(BitmapTransitionOptions.withCrossFade())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new ImageSpanTarget(storyComment, loadingSpan)));
        } else {
            storyComment.setVisibility(View.GONE);
        }

        upvoteStory = header.findViewById(R.id.story_vote_action);
        upvoteStory.setText(
                getResources().getQuantityString(io.plaidapp.R.plurals.upvotes, story.vote_count,
                        NumberFormat.getInstance().format(story.vote_count)));
        upvoteStory.setOnClickListener(v -> upvoteStory());

        final TextView share = header.findViewById(R.id.story_share_action);
        share.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
            startActivity(ShareCompat.IntentBuilder.from(DesignerNewsStory.this)
                    .setText(story.url)
                    .setType("text/plain")
                    .setSubject(story.title)
                    .getIntent());
        });

        TextView storyPosterTime = header.findViewById(R.id.story_poster_time);
        if (story.user_display_name != null && story.user_job != null) {
            SpannableString poster = new SpannableString(story.user_display_name.toLowerCase());
            poster.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_CommentAuthor),
                    0, poster.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            CharSequence job =
                    !TextUtils.isEmpty(story.user_job) ? "\n" + story.user_job.toLowerCase() : "";
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(story.created_at.getTime(),
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS)
                    .toString().toLowerCase();
            storyPosterTime.setText(TextUtils.concat(poster, job, "\n", timeAgo));

        }
        ImageView avatar = header.findViewById(R.id.story_poster_avatar);
        if (!TextUtils.isEmpty(story.user_portrait_url)) {
            GlideApp.with(this)
                    .load(story.user_portrait_url)
                    .transition(withCrossFade())
                    .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(avatar);
        } else {
            avatar.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View setupCommentField() {
        View enterCommentView = getLayoutInflater()
                .inflate(R.layout.designer_news_enter_comment, commentsList, false);
        enterComment = enterCommentView.findViewById(R.id.comment);
        postComment = enterCommentView.findViewById(R.id.post_comment);
        postComment.setOnClickListener(v -> {
            if (designerNewsPrefs.isLoggedIn()) {
                if (TextUtils.isEmpty(enterComment.getText())) return;
                enterComment.setEnabled(false);
                postComment.setEnabled(false);
                final Call<Comment> comment = designerNewsPrefs.getApi()
                        .comment(story.id, enterComment.getText().toString());
                comment.enqueue(new Callback<Comment>() {
                    @Override
                    public void onResponse(Call<Comment> call, Response<Comment> response) {
                        enterComment.getText().clear();
                        enterComment.setEnabled(true);
                        postComment.setEnabled(true);
                        commentsAdapter.addComment(response.body());
                    }

                    @Override
                    public void onFailure(Call<Comment> call, Throwable t) {
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
        });
        enterComment.setOnFocusChangeListener(enterCommentFocus);
        return enterCommentView;
    }

    private void upvoteStory() {
        if (designerNewsPrefs.isLoggedIn()) {
            if (!upvoteStory.isActivated()) {
                upvoteStory.setActivated(true);
                final Call<Story> upvoteStory
                        = designerNewsPrefs.getApi().upvoteStory(story.id);
                upvoteStory.enqueue(new Callback<Story>() {
                    @Override
                    public void onResponse(Call<Story> call, Response<Story> response) {
                        final int newUpvoteCount = response.body().vote_count;
                        DesignerNewsStory.this.upvoteStory.setText(getResources().getQuantityString(
                                io.plaidapp.R.plurals.upvotes, newUpvoteCount,
                                NumberFormat.getInstance().format(newUpvoteCount)));
                    }

                    @Override
                    public void onFailure(Call<Story> call, Throwable t) {
                    }
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
        MorphTransform.addExtras(login, ContextCompat.getColor(this,
                io.plaidapp.R.color.background_light),
                triggeringView.getHeight() / 2);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                DesignerNewsStory.this,
                triggeringView, getString(io.plaidapp.R.string.transition_designer_news_login));
        startActivityForResult(login, requestCode, options.toBundle());
    }

    private void unnestComments(List<Comment> nested, List<Comment> flat) {
        for (Comment comment : nested) {
            flat.add(comment);
            if (comment.comments != null && comment.comments.size() > 0) {
                unnestComments(comment.comments, flat);
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

    /* package */ class DesignerNewsCommentsAdapter
            extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_NO_COMMENTS = 1;
        private static final int TYPE_COMMENT = 2;
        private static final int TYPE_COMMENT_REPLY = 3;
        private static final int TYPE_FOOTER = 4;

        private View header;
        private List<Comment> comments;
        private View footer;
        private int expandedCommentPosition = RecyclerView.NO_POSITION;
        private boolean replyToCommentFocused = false;

        DesignerNewsCommentsAdapter(@NonNull View header,
                @NonNull List<Comment> comments,
                @NonNull View footer) {
            this.header = header;
            this.comments = comments;
            this.footer = footer;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            if (isCommentReplyExpanded() && position == expandedCommentPosition + 1) {
                return TYPE_COMMENT_REPLY;
            }
            int footerPosition = hasComments() ? 1 + comments.size() // header + comments
                    : 2; // header + no comments view
            if (isCommentReplyExpanded()) footerPosition++;
            if (position == footerPosition) return TYPE_FOOTER;
            return hasComments() ? TYPE_COMMENT : TYPE_NO_COMMENTS;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEADER:
                    return new HeaderHolder(header);
                case TYPE_COMMENT:
                    return createCommentHolder(parent);
                case TYPE_COMMENT_REPLY:
                    return createCommentReplyHolder(parent);
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
            switch (getItemViewType(position)) {
                case TYPE_COMMENT:
                    bindComment((CommentViewHolder) holder, null);
                    break;
                case TYPE_COMMENT_REPLY:
                    ((CommentReplyHolder) holder).bindCommentReply(
                            getComment(holder.getAdapterPosition() - 1));
                    break;
            } // nothing to bind for header / no comment / footer views
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder,
                int position,
                List<Object> partialChangePayloads) {
            switch (getItemViewType(position)) {
                case TYPE_COMMENT:
                    bindComment((CommentViewHolder) holder, partialChangePayloads);
                    break;
                default:
                    onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            int itemCount = 2; // header + footer
            if (hasComments()) {
                itemCount += comments.size();
            } else {
                itemCount++; // no comments view
            }
            if (isCommentReplyExpanded()) itemCount++;
            return itemCount;
        }

        public void addComment(Comment newComment) {
            if (!hasComments()) {
                notifyItemRemoved(1); // remove the no comments view
            }
            comments.add(newComment);
            notifyItemInserted(commentIndexToAdapterPosition(comments.size() - 1));
        }

        /**
         * Add a new comment and return the adapter position that it was inserted at.
         */
        public int addCommentReply(Comment newComment, int inReplyToAdapterPosition) {
            // when replying to a comment, we want to insert it after any existing replies
            // i.e. after any following comments with the same or greater depth
            int commentIndex = adapterPositionToCommentIndex(inReplyToAdapterPosition);
            do {
                commentIndex++;
            } while (commentIndex < comments.size() &&
                    comments.get(commentIndex).depth >= newComment.depth);
            comments.add(commentIndex, newComment);
            int adapterPosition = commentIndexToAdapterPosition(commentIndex);
            notifyItemInserted(adapterPosition);
            return adapterPosition;
        }

        public boolean isReplyToCommentFocused() {
            return replyToCommentFocused;
        }

        private boolean hasComments() {
            return !comments.isEmpty();
        }

        private boolean isCommentReplyExpanded() {
            return expandedCommentPosition != RecyclerView.NO_POSITION;
        }

        private Comment getComment(int adapterPosition) {
            return comments.get(adapterPositionToCommentIndex(adapterPosition));
        }

        private int adapterPositionToCommentIndex(int adapterPosition) {
            int index = adapterPosition - 1; // less header
            if (isCommentReplyExpanded()
                    && adapterPosition > expandedCommentPosition) {
                index--;
            }
            return index;
        }

        private int commentIndexToAdapterPosition(int index) {
            int adapterPosition = index + 1; // header
            if (isCommentReplyExpanded()) {
                int expandedCommentIndex = adapterPositionToCommentIndex(expandedCommentPosition);
                if (index > expandedCommentIndex) adapterPosition++;
            }
            return adapterPosition;
        }

        @NonNull
        private CommentViewHolder createCommentHolder(ViewGroup parent) {
            final CommentViewHolder holder = new CommentViewHolder(
                    getLayoutInflater().inflate(R.layout.designer_news_comment, parent, false));
            holder.itemView.setOnClickListener(v -> {
                final boolean collapsingSelf =
                        expandedCommentPosition == holder.getAdapterPosition();
                collapseExpandedComment();
                if (collapsingSelf) return;

                // show reply below this
                expandedCommentPosition = holder.getAdapterPosition();
                notifyItemInserted(expandedCommentPosition + 1);
                notifyItemChanged(expandedCommentPosition, CommentAnimator.EXPAND_COMMENT);
            });
            holder.getThreadDepth().setImageDrawable(
                    new ThreadedCommentDrawable(threadWidth, threadGap));

            return holder;
        }

        private void collapseExpandedComment() {
            if (!isCommentReplyExpanded()) return;
            notifyItemChanged(expandedCommentPosition, CommentAnimator.COLLAPSE_COMMENT);
            notifyItemRemoved(expandedCommentPosition + 1);
            replyToCommentFocused = false;
            expandedCommentPosition = RecyclerView.NO_POSITION;
            updateFabVisibility();
        }

        private void bindComment(final CommentViewHolder holder, List<Object> partialChanges) {
            // Check if this is a partial update for expanding/collapsing a comment. If it is we
            // can do a partial bind as the bound data has not changed.
            if (partialChanges == null || partialChanges.isEmpty() ||
                    !(partialChanges.contains(CommentAnimator.COLLAPSE_COMMENT)
                            || partialChanges.contains(CommentAnimator.EXPAND_COMMENT))) {

                final Comment comment = getComment(holder.getAdapterPosition());

                HtmlUtils.parseMarkdownAndSetText(holder.getComment(), comment.body, markdown,
                        (src, loadingSpan) -> GlideApp.with(DesignerNewsStory.this)
                                .asBitmap()
                                .load(src)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(new ImageSpanTarget(holder.getComment(), loadingSpan)));

                if (comment.user_display_name != null) {
                    holder.getAuthor().setText(comment.user_display_name.toLowerCase());
                }
                holder.getAuthor().setOriginalPoster(isOP(comment.user_id));
                if (comment.created_at != null) {
                    holder.getTimeAgo().setText(
                            DateUtils.getRelativeTimeSpanString(comment.created_at.getTime(),
                                    System.currentTimeMillis(),
                                    DateUtils.SECOND_IN_MILLIS)
                                    .toString().toLowerCase());
                }
                // FIXME updating drawable doesn't seem to be working, just create a new one
                //((ThreadedCommentDrawable) holder.threadDepth.getDrawable())
                //     .setDepth(comment.depth);

                holder.getThreadDepth().setImageDrawable(
                        new ThreadedCommentDrawable(threadWidth, threadGap, comment.depth));
            }

            // set/clear expanded comment state
            holder.itemView.setActivated(holder.getAdapterPosition() == expandedCommentPosition);
            if (holder.getAdapterPosition() == expandedCommentPosition) {
                final int threadDepthWidth = holder.getThreadDepth().getDrawable().getIntrinsicWidth();
                final float leftShift = -(threadDepthWidth + ((ViewGroup.MarginLayoutParams)
                        holder.getThreadDepth().getLayoutParams()).getMarginEnd());
                holder.getAuthor().setTranslationX(leftShift);
                holder.getComment().setTranslationX(leftShift);
                holder.getThreadDepth().setTranslationX(-(threadDepthWidth
                        + ((ViewGroup.MarginLayoutParams)
                        holder.getThreadDepth().getLayoutParams()).getMarginStart()));
            } else {
                holder.getThreadDepth().setTranslationX(0f);
                holder.getAuthor().setTranslationX(0f);
                holder.getComment().setTranslationX(0f);
            }
        }

        private void upvoteComment(Long id) {
            final Call<Comment> upvoteComment =
                    designerNewsPrefs.getApi().upvoteComment(id);
            upvoteComment.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(Call<Comment> call,
                        Response<Comment> response) {
                }

                @Override
                public void onFailure(Call<Comment> call, Throwable t) {
                }
            });
        }

        private void replyToComment(Long commentId, String reply) {
            final Call<Comment> replyToComment = designerNewsPrefs.getApi()
                    .replyToComment(commentId, reply);
            replyToComment.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(Call<Comment> call, Response<Comment> response) {

                }

                @Override
                public void onFailure(Call<Comment> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),
                            "Failed to post comment :(", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void handleCommentVotesClick(CommentReplyHolder holder,
                boolean isUserLoggedIn,
                Comment comment) {
            if (isUserLoggedIn) {
                if (!holder.getCommentVotes().isActivated()) {
                    upvoteComment(comment.id);
                    comment.upvoted = true;
                    comment.vote_count++;
                    holder.getCommentVotes().setText(String.valueOf(comment.vote_count));
                    holder.getCommentVotes().setActivated(true);
                } else {
                    comment.upvoted = false;
                    comment.vote_count--;
                    holder.getCommentVotes().setText(String.valueOf(comment.vote_count));
                    holder.getCommentVotes().setActivated(false);
                    // TODO actually delete upvote - florina: why?
                }
            } else {
                needsLogin(holder.getCommentVotes(), 0);
            }
            holder.getCommentReply().clearFocus();
        }

        private void handleCommentReplyFocus(CommentReplyHolder holder,
                Interpolator interpolator) {
            holder.getCommentVotes().animate()
                    .translationX(-holder.getCommentVotes().getWidth())
                    .alpha(0f)
                    .setDuration(200L)
                    .setInterpolator(interpolator);
            holder.getReplyLabel().animate()
                    .translationX(-holder.getCommentVotes().getWidth())
                    .setDuration(200L)
                    .setInterpolator(interpolator);
            holder.getPostReply().setVisibility(View.VISIBLE);
            holder.getPostReply().setAlpha(0f);
            holder.getPostReply().animate()
                    .alpha(1f)
                    .setDuration(200L)
                    .setInterpolator(interpolator)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            holder.itemView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setHasTransientState(false);
                        }
                    });
        }

        private void handleCommentReplyFocusLoss(CommentReplyHolder holder,
                Interpolator interpolator) {
            holder.getCommentVotes().animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(200L)
                    .setInterpolator(interpolator);
            holder.getReplyLabel().animate()
                    .translationX(0f)
                    .setDuration(200L)
                    .setInterpolator(interpolator);
            holder.getPostReply().animate()
                    .alpha(0f)
                    .setDuration(200L)
                    .setInterpolator(interpolator)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            holder.itemView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.getPostReply().setVisibility(View.INVISIBLE);
                            holder.itemView.setHasTransientState(true);
                        }
                    });
        }

        @NonNull
        private CommentReplyHolder createCommentReplyHolder(ViewGroup parent) {
            final CommentReplyHolder holder = new CommentReplyHolder(getLayoutInflater()
                    .inflate(R.layout.designer_news_comment_actions, parent, false));

            holder.getCommentVotes().setOnClickListener(v -> {
                Comment comment = getComment(holder.getAdapterPosition());
                handleCommentVotesClick(holder, designerNewsPrefs.isLoggedIn(), comment);
            });

            holder.getPostReply().setOnClickListener(v -> {
                if (designerNewsPrefs.isLoggedIn()) {
                    String reply = holder.getCommentReply().getText().toString();
                    if (reply.isEmpty()) return;

                    final int inReplyToCommentPosition = holder.getAdapterPosition() - 1;
                    final Comment replyingTo = getComment(inReplyToCommentPosition);
                    collapseExpandedComment();

                    // insert a locally created comment before actually
                    // hitting the API for immediate response
                    int replyDepth = replyingTo.depth + 1;
                    User user = designerNewsPrefs.getUser();
                    final int newReplyPosition = commentsAdapter.addCommentReply(
                            new Comment.Builder()
                                    .setBody(holder.getCommentReply().getText().toString())
                                    .setCreatedAt(new Date())
                                    .setDepth(replyDepth)
                                    .setUserId(user.id)
                                    .setUserDisplayName(user.display_name)
                                    .setUserPortraitUrl(user.portrait_url)
                                    .build(),
                            inReplyToCommentPosition);

                    replyToComment(replyingTo.id, reply);
                    holder.getCommentReply().getText().clear();
                    ImeUtils.hideIme(holder.getCommentReply());
                    commentsList.scrollToPosition(newReplyPosition);
                } else {
                    needsLogin(holder.getPostReply(), 0);
                }
                holder.getCommentReply().clearFocus();
            });

            holder.getCommentReply().setOnFocusChangeListener((v, hasFocus) -> {
                replyToCommentFocused = hasFocus;
                final Interpolator interpolator = getFastOutSlowInInterpolator(holder
                        .itemView.getContext());
                if (hasFocus) {
                    handleCommentReplyFocus(holder, interpolator);
                    updateFabVisibility();
                } else {
                    handleCommentReplyFocusLoss(holder, interpolator);
                    updateFabVisibility();
                }
                holder.getPostReply().setActivated(hasFocus);
            });
            return holder;
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

    public static class CommentItemHolderInfo extends
            RecyclerView.ItemAnimator.ItemHolderInfo {
        boolean doExpand;
        boolean doCollapse;
    }
}
