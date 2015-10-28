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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.ArcMotion;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import in.uncod.android.bypass.style.ImageLoadingSpan;
import io.plaidapp.R;
import io.plaidapp.data.api.designernews.UpvoteStoryService;
import io.plaidapp.data.api.designernews.model.Comment;
import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.ui.drawable.ThreadedCommentDrawable;
import io.plaidapp.ui.widget.AuthorTextView;
import io.plaidapp.ui.widget.CollapsingTitleLayout;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.FontTextView;
import io.plaidapp.ui.widget.PinnedOffsetView;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.ImageUtils;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.ImageSpanTarget;

public class DesignerNewsStory extends Activity {

    protected static final String EXTRA_STORY = "story";

    @Bind(R.id.comments_list) RecyclerView commentsList;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.fab_expand) View fabExpand;
    @Bind(R.id.comments_container) ElasticDragDismissFrameLayout draggableFrame;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    @BindInt(R.integer.fab_expand_duration) int fabExpandDuration;
    @BindDimen(R.dimen.comment_thread_width) int threadWidth;
    @BindDimen(R.dimen.comment_thread_gap) int threadGap;

    private Story story;
    private CollapsingTitleLayout collapsingToolbar;
    private PinnedOffsetView toolbarBackground;
    private Bypass markdown;
    private CustomTabActivityHelper customTab;

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

        View storyDescription = getLayoutInflater().inflate(R.layout
                .designer_news_story_description, commentsList, false);
        bindDescription(storyDescription);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.story_toolbar);
        if (toolbar != null) { // portrait: collapsing toolbar
            collapsingToolbar = (CollapsingTitleLayout) findViewById(R.id.backdrop_toolbar);
            collapsingToolbar.setTitle(story.title);
            toolbarBackground = (PinnedOffsetView) findViewById(R.id.story_title_background);
            commentsList.addOnScrollListener(headerScrollListener);
            collapsingToolbar.addOnLayoutChangeListener(titlebarLayout);
        } else { // landscape: scroll toolbar with content
            toolbar = (Toolbar) storyDescription.findViewById(R.id.story_toolbar);
            FontTextView title = (FontTextView) toolbar.findViewById(R.id.story_title);
            title.setText(story.title);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        if (story.comment_count > 0) {
            // flatten the comments from a nested structure {@see Comment#comments} to an
            // array for our adapter (saving the depth).
            List<ThreadedComment> wrapped = new ArrayList<>(story.comment_count);
            addComments(story.comments, 0, wrapped);
            commentsList.setAdapter(new DesignerNewsCommentsAdapter(storyDescription, wrapped));

        } else {
            commentsList.setAdapter(
                    new DesignerNewsCommentsAdapter(storyDescription, Collections.EMPTY_LIST));
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
                .setActionButton(ImageUtils.vectorToBitmap(context, R.drawable.ic_thumb_up),
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

    private int gridScrollY = 0;
    private RecyclerView.OnScrollListener headerScrollListener
            = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            gridScrollY += dy;
            collapsingToolbar.setScrollPixelOffset(gridScrollY);
            toolbarBackground.setOffset(-gridScrollY);
        }
    };

    // title can expand up to a max number of lines.  If it does then adjust the list padding
    // & reset scroll trackers
    private View.OnLayoutChangeListener titlebarLayout = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int
                oldLeft, int oldTop, int oldRight, int oldBottom) {
            commentsList.setPaddingRelative(commentsList.getPaddingStart(),
                    collapsingToolbar.getHeight(),
                    commentsList.getPaddingEnd(),
                    commentsList.getPaddingBottom());
            commentsList.scrollToPosition(0);
            gridScrollY = 0;
            collapsingToolbar.setScrollPixelOffset(0);
            toolbarBackground.setOffset(0);
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

    private void bindDescription(View storyDescription) {
        TextView storyPoster = (TextView) storyDescription.findViewById(R.id.story_poster);
        storyPoster.setText(DateUtils.getRelativeTimeSpanString(story.created_at.getTime(),
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS)
                + " by " + story.user_display_name
                + ", " + story.user_job);

        final TextView storyComment = (TextView) storyDescription.findViewById(R.id.story_comment);
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
        }
        storyComment.setVisibility(TextUtils.isEmpty(story.comment) ? View.GONE : View.VISIBLE);
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

        private View header;
        private List<ThreadedComment> comments;

        DesignerNewsCommentsAdapter(@NonNull View header, @NonNull List<ThreadedComment> comments) {
            this.header = header;
            this.comments = comments;
        }

        private boolean hasComments() {
            return !comments.isEmpty();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
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
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_COMMENT) {
                bindComment((CommentHolder) holder, comments.get(position - 1)); // minus header
            } // nothing to bind for header / no comment views
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
            return hasComments() ? comments.size() + 1 // add one for header
                    : 2; // header + no comments view
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
}
