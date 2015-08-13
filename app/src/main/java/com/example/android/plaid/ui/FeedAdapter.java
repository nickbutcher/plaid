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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.transition.ArcMotion;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.android.plaid.R;
import com.example.android.plaid.data.PlaidItem;
import com.example.android.plaid.data.PlaidItemComparator;
import com.example.android.plaid.data.api.designernews.model.Story;
import com.example.android.plaid.data.api.dribbble.model.Shot;
import com.example.android.plaid.data.api.hackernews.HackerNewsService;
import com.example.android.plaid.data.api.hackernews.model.Post;
import com.example.android.plaid.data.pocket.PocketUtils;
import com.example.android.plaid.ui.util.ObservableColorMatrix;
import com.example.android.plaid.ui.util.glide.DribbbleTarget;
import com.example.android.plaid.ui.widget.BadgedFourThreeImageView;

import org.chromium.customtabsclient.CustomTabActivityManager;
import org.chromium.customtabsclient.CustomTabUiBuilder;

import java.util.Collection;

/**
 * Created by nickbutcher on 7/16/14.
 */
public class FeedAdapter extends ArrayAdapter<PlaidItem> {

    public static final float DUPE_WEIGHT_BOOST = 0.4f;
    private static final int TYPE_DESIGNER_NEWS_STORY = 0;
    private static final int TYPE_DRIBBBLE_SHOT = 1;
    private static final int TYPE_HACKER_NEWS_POST = 2;
    private static final int TYPE_PRODUCT_HINT_POST = 3;
    private final LayoutInflater layoutInflater;
    private final PlaidItemComparator comparator;
    private final boolean pocketIsInstalled;

    public FeedAdapter(Context context, boolean pocketInstalled) {
        super(context, R.layout.designer_news_story_item);
        pocketIsInstalled = pocketInstalled;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        comparator = new PlaidItemComparator();
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {

        switch (getItemViewType(position)) {
            case TYPE_DESIGNER_NEWS_STORY:
                if (view == null) {
                    view = newDesignerNewsStoryView(position, container);
                }
                bindDesignerNewsStoryView((Story) getItem(position), position, view);
                break;
            case TYPE_DRIBBBLE_SHOT:
                if (view == null) {
                    view = newDribbbleShotView(position, container);
                }
                bindDribbbleShotView((Shot) getItem(position), position, view);
                break;
            case TYPE_HACKER_NEWS_POST:
                if (view == null) {
                    view = newHackerNewsPostView(position, container);
                }
                bindHackerNewsPostView((Post) getItem(position), position, view);
                break;
            case TYPE_PRODUCT_HINT_POST:
                if (view == null) {
                    view = newProductHuntPostView(position, container);
                }
                bindProductHuntPostView((com.example.android.plaid.data.api.producthunt.model
                        .Post) getItem(position), position, view);
                break;
        }
        return view;
    }

    private View newDesignerNewsStoryView(final int position, final ViewGroup container) {
        View v = layoutInflater.inflate(R.layout.designer_news_story_item, container, false);
        v.setTag(R.id.story_title_background, v.findViewById(R.id.story_title_background));
        v.setTag(R.id.story_title, v.findViewById(R.id.story_title));
        v.setTag(R.id.story_comments, v.findViewById(R.id.story_comments));
        if (pocketIsInstalled) {
            v.setTag(R.id.pocket, v.findViewById(R.id.pocket));
        } else {
            v.findViewById(R.id.pocket).setVisibility(View.GONE);
        }
        return v;
    }

    private void bindDesignerNewsStoryView(final Story story, final int position, final View view) {
        final TextView title = (TextView) view.getTag(R.id.story_title);
        title.setText(story.title);
        ((View) view.getTag(R.id.story_title_background)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomTabActivityManager.getInstance().launchUrl((Activity) getContext(),
                                null,
                                story.url,
                                DesignerNewsStory.createChromeTabUi(getContext()));
                    }
                }
                                                                            );
        final TextView comments = (TextView) view.getTag(R.id.story_comments);
        comments.setText(String.valueOf(story.comment_count));
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View commentsView) {
                Intent intent = new Intent();
                intent.setClass(getContext(), DesignerNewsStory.class);
                intent.putExtra(DesignerNewsStory.EXTRA_STORY, story);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation((Activity) getContext(),
                                Pair.create((View) view.getTag(R.id.story_title_background),
                                        getContext().getString(R.string
                                                .transition_story_title_background)),
                                Pair.create(view, getContext().getString(R.string
                                        .transition_story_background)));
                getContext().startActivity(intent, options.toBundle());
            }
        });
        if (pocketIsInstalled) {
            final ImageButton pocketButton = (ImageButton) view.getTag(R.id.pocket);
            pocketButton.setImageAlpha(178); // grumble... no xml setter, grumble...
            pocketButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // actually add to pocket
                    PocketUtils.addToPocket(getContext(), story.url);

                    // setup for anim
                    view.setHasTransientState(true);
                    ((ViewGroup) pocketButton.getParent().getParent()).setClipChildren(false);
                    final int initialLeft = pocketButton.getLeft();
                    final int initialTop = pocketButton.getTop();
                    final int translatedLeft = (view.getWidth() - pocketButton.getWidth()) / 2;
                    final int translatedTop = initialTop - ((view.getHeight() - pocketButton
                            .getHeight()) / 2);
                    final ArcMotion arc = new ArcMotion();

                    // animate the title & pocket icon up, scale the pocket icon up
                    PropertyValuesHolder pvhTitleUp = PropertyValuesHolder.ofFloat(View
                            .TRANSLATION_Y, -(view.getHeight() / 5));
                    PropertyValuesHolder pvhTitleFade = PropertyValuesHolder.ofFloat(View.ALPHA,
                            0.54f);
                    Animator titleMoveFadeOut = ObjectAnimator.ofPropertyValuesHolder(title,
                            pvhTitleUp, pvhTitleFade);

                    Animator pocketMoveUp = ObjectAnimator.ofFloat(pocketButton, View
                                    .TRANSLATION_X, View.TRANSLATION_Y,
                            arc.getPath(initialLeft, initialTop, translatedLeft, translatedTop));
                    PropertyValuesHolder pvhPocketScaleUpX = PropertyValuesHolder.ofFloat(View
                            .SCALE_X, 3f);
                    PropertyValuesHolder pvhPocketScaleUpY = PropertyValuesHolder.ofFloat(View
                            .SCALE_Y, 3f);
                    Animator pocketScaleUp = ObjectAnimator.ofPropertyValuesHolder(pocketButton,
                            pvhPocketScaleUpX, pvhPocketScaleUpY);
                    ObjectAnimator pocketFadeUp = ObjectAnimator.ofInt(pocketButton,
                            "imageAlpha", 255);

                    AnimatorSet up = new AnimatorSet();
                    up.playTogether(titleMoveFadeOut, pocketMoveUp, pocketScaleUp, pocketFadeUp);
                    up.setDuration(300);
                    up.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R
                            .interpolator.fast_out_slow_in));

                    // animate everything back into place
                    PropertyValuesHolder pvhTitleMoveUp = PropertyValuesHolder.ofFloat(View
                            .TRANSLATION_Y, 0f);
                    PropertyValuesHolder pvhTitleFadeUp = PropertyValuesHolder.ofFloat(View
                            .ALPHA, 1f);
                    Animator titleMoveFadeIn = ObjectAnimator.ofPropertyValuesHolder(title,
                            pvhTitleMoveUp, pvhTitleFadeUp);
                    Animator pocketMoveDown = ObjectAnimator.ofFloat(pocketButton, View
                                    .TRANSLATION_X, View.TRANSLATION_Y,
                            arc.getPath(translatedLeft, translatedTop, 0, 0));
                    PropertyValuesHolder pvhPocketScaleDownX = PropertyValuesHolder.ofFloat(View
                            .SCALE_X, 1f);
                    PropertyValuesHolder pvhPocketScaleDownY = PropertyValuesHolder.ofFloat(View
                            .SCALE_Y, 1f);
                    Animator pvhPocketScaleDown = ObjectAnimator.ofPropertyValuesHolder
                            (pocketButton, pvhPocketScaleDownX, pvhPocketScaleDownY);
                    ObjectAnimator pocketFadeDown = ObjectAnimator.ofInt(pocketButton,
                            "imageAlpha", 138);

                    AnimatorSet down = new AnimatorSet();
                    down.playTogether(titleMoveFadeIn, pocketMoveDown, pvhPocketScaleDown,
                            pocketFadeDown);
                    down.setDuration(300);
                    down.setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R
                            .interpolator.fast_out_slow_in));
                    down.setStartDelay(500);

                    // play it
                    AnimatorSet upDown = new AnimatorSet();
                    upDown.playSequentially(up, down);

                    // clean up
                    upDown.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewGroup) pocketButton.getParent().getParent()).setClipChildren
                                    (true);
                            view.setHasTransientState(false);
                        }
                    });
                    upDown.start();
                }
            });
        }
    }

    private View newDribbbleShotView(final int position, final ViewGroup container) {
        return layoutInflater.inflate(R.layout.dribbble_shot_item, container, false);
    }

    private void bindDribbbleShotView(final Shot shot, final int position, final View view) {
        final BadgedFourThreeImageView iv = (BadgedFourThreeImageView) view;
        iv.setBackgroundResource(R.color.background_dark);

        Glide.with(getContext())
                .load(shot.images.best())
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean
                                                           isFromMemoryCache, boolean
                                                           isFirstResource) {
                        if (!shot.hasFadedIn) {
                            view.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            ObjectAnimator saturation = ObjectAnimator.ofFloat(cm,
                                    ObservableColorMatrix.SATURATION, 0f, 1f);
                            saturation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener
                                    () {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    // just animating the color matrix does not invalidate the
                                    // drawable so
                                    // need this update listener.  Also have to create a new CMCF
                                    // as the
                                    // matrix is immutable :(
                                    if (iv.getDrawable() != null) {
                                        iv.getDrawable().setColorFilter(new
                                                ColorMatrixColorFilter(cm));
                                    }
                                }
                            });
                            saturation.setDuration(2000);
                            saturation.setInterpolator(AnimationUtils.loadInterpolator(getContext
                                    (), android.R.interpolator.fast_out_slow_in));
                            saturation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    view.setHasTransientState(false);
                                }
                            });
                            iv.setAlpha(0.2f);
                            iv.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            iv.setBackground(null);
                                        }
                                    })
                                    .setInterpolator(AnimationUtils.loadInterpolator(getContext()
                                            , android.R.interpolator.fast_out_slow_in));
                            saturation.start();
                            shot.hasFadedIn = true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable>
                            target, boolean isFirstResource) {
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new DribbbleTarget(iv, false));

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv.setTransitionName(iv.getResources().getString(R.string.transition_shot));
                iv.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.background_light));
                Intent intent = new Intent();
                intent.setClass(getContext(), DribbbleShot.class);
                intent.putExtra(DribbbleShot.EXTRA_SHOT, shot);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation((Activity) getContext(),
                                Pair.create(view, getContext().getString(R.string.transition_shot)),
                                Pair.create(view, getContext().getString(R.string
                                        .transition_shot_background)));
                getContext().startActivity(intent, options.toBundle());
            }
        });
    }

    private View newHackerNewsPostView(final int position, final ViewGroup container) {
        View v = layoutInflater.inflate(R.layout.hacker_news_post_item, container, false);
        v.setTag(R.id.post_title, v.findViewById(R.id.post_title));
        v.setTag(R.id.post_comments, v.findViewById(R.id.post_comments));
        return v;
    }

    private void bindHackerNewsPostView(final Post post, final int position, final View view) {
        ((TextView) view.getTag(R.id.post_title)).setText(post.title);
        view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(post.url)));
                                    }
                                }
                               );
        final TextView comments = (TextView) view.getTag(R.id.post_comments);
        comments.setText(String.valueOf(post.commentCount));
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        (HackerNewsService.STORY_URL + post.id)));
            }
        });
    }

    private View newProductHuntPostView(final int position, final ViewGroup container) {
        View v = layoutInflater.inflate(R.layout.product_hunt_item, container, false);
        v.setTag(R.id.hunt_title, v.findViewById(R.id.hunt_title));
        // v.setTag(R.id.hunt_screenshot, v.findViewById(R.id.hunt_screenshot));
        v.setTag(R.id.tagline, v.findViewById(R.id.tagline));
        v.setTag(R.id.story_comments, v.findViewById(R.id.story_comments));
        return v;
    }

    private void bindProductHuntPostView(final com.example.android.plaid.data.api.producthunt
            .model.Post item, int position, final View view) {
        ((TextView) view.getTag(R.id.hunt_title)).setText(item.name);
        /*view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                Glide.with(getContext()).load(item.getScreenshotUrl(view.getWidth())).into(
                (ImageView) view.getTag(R.id.hunt_screenshot));
                return true;
            }
        });*/
        ((TextView) view.getTag(R.id.tagline)).setText(item.tagline);
        TextView comments = (TextView) view.getTag(R.id.story_comments);
        comments.setText(String.valueOf(item.comments_count));
        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabActivityManager.getInstance().launchUrl((Activity) getContext(), null,
                        item.discussion_url,
                        new CustomTabUiBuilder().setToolbarColorRes(getContext(), R.color
                                .product_hunt));
                //getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item
                // .discussion_url)));
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabActivityManager.getInstance().launchUrl((Activity) getContext(), null,
                        item.redirect_url,
                        new CustomTabUiBuilder().setToolbarColorRes(getContext(), R.color
                                .product_hunt));
                //getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item
                // .redirect_url)));
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        PlaidItem item = getItem(position);
        if (item instanceof Story) {
            return TYPE_DESIGNER_NEWS_STORY;
        } else if (item instanceof Shot) {
            return TYPE_DRIBBBLE_SHOT;
        } else if (item instanceof Post) {
            return TYPE_HACKER_NEWS_POST;
        } else if (item instanceof com.example.android.plaid.data.api.producthunt.model.Post) {
            return TYPE_PRODUCT_HINT_POST;
        }
        return -1;
    }

    public void addAndResort(Collection<? extends PlaidItem> items) {
        // de-dupe results as the same item can be returned by multiple feeds
        boolean add = true;
        for (PlaidItem newItem : items) {
            int count = getCount();
            for (int i = 0; i < count; i++) {
                PlaidItem existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    existingItem.weightBoost = DUPE_WEIGHT_BOOST;
                    add = false;
                    break;
                }
            }
            if (add) {
                add(newItem);
                add = true;
            }
        }
        sort();
    }

    protected void sort() {
        int count = getCount();
        int maxDesignNewsVotes = 0;
        int maxDesignNewsComments = 0;
        long maxDribbleLikes = 0;
        int maxProductHuntVotes = 0;
        int maxProductHuntComments = 0;

        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Story) {
                maxDesignNewsComments = Math.max(((Story) item).comment_count,
                        maxDesignNewsComments);
                maxDesignNewsVotes = Math.max(((Story) item).vote_count, maxDesignNewsVotes);
            } else if (item instanceof Shot) {
                maxDribbleLikes = Math.max(((Shot) item).likes_count, maxDribbleLikes);
            } else if (item instanceof com.example.android.plaid.data.api.producthunt.model.Post) {
                maxProductHuntComments = Math.max(((com.example.android.plaid.data.api
                        .producthunt.model.Post) item).comments_count, maxProductHuntComments);
                maxProductHuntVotes = Math.max(((com.example.android.plaid.data.api.producthunt
                        .model.Post) item).votes_count, maxProductHuntVotes);
            }
        }

        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Story) {
                item.weight = ((((float) ((Story) item).comment_count) / maxDesignNewsComments) +
                        ((float) ((Story) item).vote_count / maxDesignNewsVotes)) / 2;
            } else if (item instanceof Shot) {
                ((Shot) item).setWeightRelativeToMax(maxDribbleLikes);
            } else if (item instanceof com.example.android.plaid.data.api.producthunt.model.Post) {
                item.weight = ((((float) ((com.example.android.plaid.data.api.producthunt.model
                        .Post) item).comments_count) / maxProductHuntComments) + ((float) ((com
                        .example.android.plaid.data.api.producthunt.model.Post) item).votes_count
                        / maxProductHuntVotes)) / 2;
            }
        }

        // hacker news item's weighting already set (based on their ranking)

        sort(comparator);
        notifyDataSetChanged();
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
