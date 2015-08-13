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

package com.example.android.plaid.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.transition.ArcMotion;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.plaid.R;
import com.example.android.plaid.data.api.designernews.UpvoteStoryService;
import com.example.android.plaid.data.api.designernews.model.Comment;
import com.example.android.plaid.data.api.designernews.model.Story;
import com.example.android.plaid.ui.drawable.ThreadedCommentDrawable;
import com.example.android.plaid.ui.span.ImageLoadingSpan;
import com.example.android.plaid.ui.util.AnimUtils;
import com.example.android.plaid.ui.util.HtmlUtils;
import com.example.android.plaid.ui.util.ImageUtils;
import com.example.android.plaid.ui.util.glide.ImageSpanTarget;
import com.example.android.plaid.ui.widget.AuthorTextView;
import com.example.android.plaid.ui.widget.CollapsingTitleLayout;
import com.example.android.plaid.ui.widget.DismissibleViewCallback;
import com.example.android.plaid.ui.widget.ElasticDragDismissFrameLayout;
import com.example.android.plaid.ui.widget.FontTextView;
import com.example.android.plaid.ui.widget.PinnedOffsetView;

import org.chromium.customtabsclient.CustomTabActivityManager;
import org.chromium.customtabsclient.CustomTabUiBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

public class DesignerNewsStory extends Activity {

    protected static final String EXTRA_STORY = "story";

    @Bind(R.id.comments_list) ListView commentsList;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.fab_expand) View fabExpand;
    @BindInt(R.integer.fab_expand_duration) int fabExpandDuration;

    private Story story;
    private CollapsingTitleLayout collapsingToolbar;
    private PinnedOffsetView toolbarBackground;
    private Bypass markdown;
    private CustomTabActivityManager chromeCustomTab;
    private CustomTabsSession chromeCustomTabSession;
    private boolean chromeCustomTabSupported;

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doFabExpand();
            if (chromeCustomTabSupported) {
                CustomTabUiBuilder tabBuilder = createChromeTabUi(DesignerNewsStory.this);
                tabBuilder.setStartAnimations(getApplicationContext(),
                        R.anim.chrome_custom_tab_enter,
                        R.anim.fade_out_rapidly);
                chromeCustomTab.launchUrl(DesignerNewsStory.this, chromeCustomTabSession, story
                        .url, tabBuilder);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(story.url)));
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

    public static CustomTabUiBuilder createChromeTabUi(Context context) {
        Intent upvoteStory = new Intent(context, UpvoteStoryService.class);
        upvoteStory.setAction(UpvoteStoryService.ACTION_UPVOTE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, upvoteStory, 0);
        return new CustomTabUiBuilder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.designer_news))
                .setActionButton(ImageUtils.vectorToBitmap(context, R.drawable.ic_thumb_up),
                        pendingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer_news_story);
        ButterKnife.bind(this);
        getWindow().getSharedElementReturnTransition().addListener(returnHomeListener);

        story = getIntent().getParcelableExtra(EXTRA_STORY);
        fab.setOnClickListener(fabClick);

        ElasticDragDismissFrameLayout draggableFrame = ButterKnife.findById(this, R.id
                .comments_container);
        draggableFrame.setCallback(new DismissibleViewCallback() {
            @Override
            public void onViewDismissed() {
                finishAfterTransition();
            }
        });

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
        if (toolbar != null) { // portrait > collapsing toolbar
            collapsingToolbar = (CollapsingTitleLayout) findViewById(R.id.backdrop_toolbar);
            collapsingToolbar.setTitle(story.title);
            toolbarBackground = (PinnedOffsetView) findViewById(R.id.story_title_background);
            commentsList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override public void onScrollStateChanged(AbsListView view, int scrollState) { }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int
                        visibleItemCount, int totalItemCount) {
                    if (commentsList.getMaxScrollAmount() > 0
                            && commentsList.getChildAt(0) != null
                            && firstVisibleItem == 0) {
                        int scrolled = commentsList.getPaddingTop() - commentsList.getChildAt(0)
                                .getTop();
                        collapsingToolbar.setScrollPixelOffset(scrolled);
                        toolbarBackground.setOffset(-scrolled);
                    } else if (visibleItemCount > 0) {
                        collapsingToolbar.setScrollOffset(1f);
                    }
                }
            });
        } else { // landscape > scroll toolbar with content
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

        commentsList.addHeaderView(storyDescription);

        if (story.comment_count > 0) {
            // flatten the comments from a nested structure {@see Comment#comments} to an
            // array for our adapter (saving the depth).
            List<ThreadedComment> wrapped = new ArrayList<>(story.comment_count);
            addComments(story.comments, 0, wrapped);
            commentsList.setAdapter(new DesignerNewsCommentsAdapter(this, R.layout
                    .designer_news_comment, wrapped));

        } else {
            // controlling manually rather than using ListView#setEmptyView as we always want
            // to display the header view
            commentsList.setAdapter(getNoCommentsAdapter());
        }

        // setup chrome custom tab stuff if it's supported
        chromeCustomTab = CustomTabActivityManager.getInstance();
        chromeCustomTabSupported = chromeCustomTab.bindService(this, new CustomTabActivityManager
                .ServiceConnectionCallback() {
            @Override
            public void onServiceConnected() {
                if (chromeCustomTab.warmup()) {
                    chromeCustomTabSession = chromeCustomTab.newSession(null);
                    if (chromeCustomTabSession != null) {
                        chromeCustomTabSession.mayLaunchUrl(Uri.parse(story.url), null, null);
                    }
                } else {
                    // TODO remove this debug effect
                    fab.animate().rotation(180f).setDuration(600L).setStartDelay(500L);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        chromeCustomTab.unbindService(this);
        super.onDestroy();
    }

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
                "backgroundColor",
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

    @Override
    protected void onResume() {
        super.onResume();
        // clean up after any fab expansion
        // todo, circular reval (hide) this?
        fab.setAlpha(1f);
        fabExpand.setVisibility(View.INVISIBLE);
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


    private ListAdapter getNoCommentsAdapter() {
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
                return DesignerNewsStory.this.getLayoutInflater().inflate(R.layout
                        .designer_news_no_comments, parent, false);
            }
        };
    }

    private boolean isOP(Long userId) {
        return userId.equals(story.user_id);
    }

    // convenience class used to convert nested comment structure returned from the API to a flat
    // structure with a depth attribute, suitible for showing in a list.
    protected class ThreadedComment {
        final int depth;
        final Comment comment;

        ThreadedComment(int depth,
                        Comment comment) {
            this.depth = depth;
            this.comment = comment;
        }
    }

    protected class DesignerNewsCommentsAdapter extends ArrayAdapter<ThreadedComment> {

        private int threadWidth;
        private int threadGap;

        DesignerNewsCommentsAdapter(Context context, int resource, List<ThreadedComment> comments) {
            super(context, resource, comments);
            threadWidth = context.getResources().getDimensionPixelSize(R.dimen
                    .comment_thread_width);
            threadGap = context.getResources().getDimensionPixelSize(R.dimen.comment_thread_gap);
        }

        @Override
        public View getView(int position, View view, ViewGroup container) {
            ThreadedComment comment = getItem(position);
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.designer_news_comment, container,
                        false);
                TextView tvBody = (TextView) view.findViewById(R.id.comment_text);
                view.setTag(R.id.comment_text, tvBody);
                view.setTag(R.id.depth, view.findViewById(R.id.depth));
                ((ImageView) view.getTag(R.id.depth)).setImageDrawable(new
                        ThreadedCommentDrawable(threadWidth, threadGap));
                // view.setTag(R.id.user_image, view.findViewById(R.id.user_image));
                view.setTag(R.id.comment_author, view.findViewById(R.id.comment_author));
                view.setTag(R.id.comment_time_ago, view.findViewById(R.id.comment_time_ago));
            }
            final TextView commentText = (TextView) view.getTag(R.id.comment_text);
            HtmlUtils.setTextWithNiceLinks(commentText, markdown.markdownToSpannable(comment
                    .comment.body, commentText, new Bypass.LoadImageCallback() {
                @Override
                public void loadImage(String src, ImageLoadingSpan loadingSpan) {
                    Glide.with(DesignerNewsStory.this)
                            .load(src)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new ImageSpanTarget(commentText, loadingSpan));
                }
            }));
            ((AuthorTextView) view.getTag(R.id.comment_author)).setText(comment.comment
                    .user_display_name);
            ((AuthorTextView) view.getTag(R.id.comment_author)).setOriginalPoster(isOP(comment
                    .comment.user_id));
            ((TextView) view.getTag(R.id.comment_time_ago)).setText(
                    DateUtils.getRelativeTimeSpanString(comment.comment.created_at.getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS));
            ImageView depth = (ImageView) view.getTag(R.id.depth);
            ThreadedCommentDrawable depthDrawable = new ThreadedCommentDrawable(threadWidth,
                    threadGap);
            depthDrawable.setDepth(comment.depth);
            depth.setImageDrawable(depthDrawable);
            return view;
        }
    }
}
