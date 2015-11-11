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
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.transition.ArcMotion;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.plaidapp.R;
import io.plaidapp.data.DataLoadingSubject;
import io.plaidapp.data.PlaidItem;
import io.plaidapp.data.PlaidItemComparator;
import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.producthunt.model.Post;
import io.plaidapp.data.pocket.PocketUtils;
import io.plaidapp.ui.widget.BadgedFourThreeImageView;
import io.plaidapp.util.ObservableColorMatrix;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.DribbbleTarget;

/**
 * Adapter for the main screen grid of items
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DESIGNER_NEWS_STORY = 0;
    private static final int TYPE_DRIBBBLE_SHOT = 1;
    private static final int TYPE_PRODUCT_HUNT_POST = 2;
    private static final int TYPE_LOADING_MORE = -1;
    public static final float DUPE_WEIGHT_BOOST = 0.4f;

    // we need to hold on to an activity ref for the shared element transitions :/
    private final Activity host;
    private final LayoutInflater layoutInflater;
    private final PlaidItemComparator comparator;
    private final boolean pocketIsInstalled;
    private @Nullable DataLoadingSubject dataLoading;
    private final int columns;

    private List<PlaidItem> items;

    public FeedAdapter(Activity hostActivity,
                       DataLoadingSubject dataLoading,
                       int columns,
                       boolean pocketInstalled) {
        this.host = hostActivity;
        this.dataLoading = dataLoading;
        this.columns = columns;
        this.pocketIsInstalled = pocketInstalled;
        layoutInflater = LayoutInflater.from(host);
        comparator = new PlaidItemComparator();
        items = new ArrayList<>();
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DESIGNER_NEWS_STORY:
                return new DesignerNewsStoryHolder(
                        layoutInflater.inflate(R.layout.designer_news_story_item, parent, false),
                        pocketIsInstalled);
            case TYPE_DRIBBBLE_SHOT:
                return new DribbbleShotHolder(
                        layoutInflater.inflate(R.layout.dribbble_shot_item, parent, false));
            case TYPE_PRODUCT_HUNT_POST:
                return new ProductHuntStoryHolder(
                        layoutInflater.inflate(R.layout.product_hunt_item, parent, false));
            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(
                        layoutInflater.inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            PlaidItem item = getItem(position);
            if (item instanceof Story) {
                bindDesignerNewsStory((Story) getItem(position), (DesignerNewsStoryHolder) holder);
            } else if (item instanceof Shot) {
                bindDribbbleShotView((Shot) item, (DribbbleShotHolder) holder);
            } else if (item instanceof Post) {
                bindProductHuntPostView((Post) item, (ProductHuntStoryHolder) holder);
            }
        } else {
            bindLoadingViewHolder((LoadingMoreHolder) holder, position);
        }
    }

    private void bindDesignerNewsStory(final Story story, final DesignerNewsStoryHolder holder) {
        holder.title.setText(story.title);
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomTabActivityHelper.openCustomTab(host,
                                DesignerNewsStory.getCustomTabIntent(host, story, null).build(),
                                Uri.parse(story.url));
                    }
                }
                                          );
        holder.comments.setText(String.valueOf(story.comment_count));
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View commentsView) {
                final Intent intent = new Intent();
                intent.setClass(host, DesignerNewsStory.class);
                intent.putExtra(DesignerNewsStory.EXTRA_STORY, story);
                final ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(host,
                                Pair.create(holder.itemView,
                                        host.getString(R.string.transition_story_title_background)),
                                Pair.create(holder.itemView,
                                        host.getString(R.string.transition_story_background)));
                host.startActivity(intent, options.toBundle());
            }
        });
        if (pocketIsInstalled) {
            holder.pocket.setImageAlpha(178); // grumble... no xml setter, grumble...
            holder.pocket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final ImageButton pocketButton = (ImageButton) view;
                    // actually add to pocket
                    PocketUtils.addToPocket(host, story.url);

                    // setup for anim
                    holder.itemView.setHasTransientState(true);
                    ((ViewGroup) pocketButton.getParent().getParent()).setClipChildren(false);
                    final int initialLeft = pocketButton.getLeft();
                    final int initialTop = pocketButton.getTop();
                    final int translatedLeft =
                            (holder.itemView.getWidth() - pocketButton.getWidth()) / 2;
                    final int translatedTop =
                        initialTop - ((holder.itemView.getHeight() - pocketButton.getHeight()) / 2);
                    final ArcMotion arc = new ArcMotion();

                    // animate the title & pocket icon up, scale the pocket icon up
                    PropertyValuesHolder pvhTitleUp = PropertyValuesHolder.ofFloat(View
                            .TRANSLATION_Y, -(holder.itemView.getHeight() / 5));
                    PropertyValuesHolder pvhTitleFade = PropertyValuesHolder.ofFloat(View.ALPHA,
                            0.54f);
                    Animator titleMoveFadeOut = ObjectAnimator.ofPropertyValuesHolder(holder.title,
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
                            ViewUtils.IMAGE_ALPHA, 255);

                    AnimatorSet up = new AnimatorSet();
                    up.playTogether(titleMoveFadeOut, pocketMoveUp, pocketScaleUp, pocketFadeUp);
                    up.setDuration(300);
                    up.setInterpolator(AnimationUtils.loadInterpolator(host, android.R
                            .interpolator.fast_out_slow_in));

                    // animate everything back into place
                    PropertyValuesHolder pvhTitleMoveUp = PropertyValuesHolder.ofFloat(View
                            .TRANSLATION_Y, 0f);
                    PropertyValuesHolder pvhTitleFadeUp = PropertyValuesHolder.ofFloat(View
                            .ALPHA, 1f);
                    Animator titleMoveFadeIn = ObjectAnimator.ofPropertyValuesHolder(holder.title,
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
                            ViewUtils.IMAGE_ALPHA, 138);

                    AnimatorSet down = new AnimatorSet();
                    down.playTogether(titleMoveFadeIn, pocketMoveDown, pvhPocketScaleDown,
                            pocketFadeDown);
                    down.setDuration(300);
                    down.setInterpolator(AnimationUtils.loadInterpolator(host, android.R
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
                            holder.itemView.setHasTransientState(false);
                        }
                    });
                    upDown.start();
                }
            });
        }
    }

    private void bindDribbbleShotView(final Shot shot, final DribbbleShotHolder holder) {
        final BadgedFourThreeImageView iv = (BadgedFourThreeImageView) holder.itemView;
        Glide.with(host)
                .load(shot.images.best())
                .listener(new RequestListener<String, GlideDrawable>() {

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean
                                                           isFromMemoryCache, boolean
                                                           isFirstResource) {
                        if (!shot.hasFadedIn) {
                            iv.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            ObjectAnimator saturation = ObjectAnimator.ofFloat(cm,
                                    ObservableColorMatrix.SATURATION, 0f, 1f);
                            saturation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener
                                    () {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    // just animating the color matrix does not invalidate the
                                    // drawable so need this update listener.  Also have to create a
                                    // new CMCF as the matrix is immutable :(
                                    if (iv.getDrawable() != null) {
                                        iv.getDrawable().setColorFilter(new
                                                ColorMatrixColorFilter(cm));
                                    }
                                }
                            });
                            saturation.setDuration(2000);
                            saturation.setInterpolator(AnimationUtils.loadInterpolator(host,
                                    android.R.interpolator.fast_out_slow_in));
                            saturation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    iv.setHasTransientState(false);
                                }
                            });
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
                // needed to prevent seeing through view as it fades in
                .placeholder(R.color.background_dark)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new DribbbleTarget(iv, false));

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv.setTransitionName(iv.getResources().getString(R.string.transition_shot));
                iv.setBackgroundColor(
                        ContextCompat.getColor(host, R.color.background_light));
                Intent intent = new Intent();
                intent.setClass(host, DribbbleShot.class);
                intent.putExtra(DribbbleShot.EXTRA_SHOT, shot);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(host,
                                Pair.create(view, host.getString(R.string.transition_shot)),
                                Pair.create(view, host.getString(R.string
                                        .transition_shot_background)));
                host.startActivity(intent, options.toBundle());
            }
        });
    }

    private void bindProductHuntPostView(final Post item, ProductHuntStoryHolder holder) {
        holder.title.setText(item.name);
        holder.tagline.setText(item.tagline);
        holder.comments.setText(String.valueOf(item.comments_count));
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabActivityHelper.openCustomTab(
                        host,
                        new CustomTabsIntent.Builder()
                                .setToolbarColor(ContextCompat.getColor(host, R.color.product_hunt))
                                .build(),
                        Uri.parse(item.discussion_url));
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabActivityHelper.openCustomTab(
                        host,
                        new CustomTabsIntent.Builder()
                                .setToolbarColor(ContextCompat.getColor(host, R.color.product_hunt))
                                .build(),
                        Uri.parse(item.redirect_url));
            }
        });
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility(position > 0 && dataLoading.isDataLoading() ?
                View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {
            PlaidItem item = getItem(position);
            if (item instanceof Story) {
                return TYPE_DESIGNER_NEWS_STORY;
            } else if (item instanceof Shot) {
                return TYPE_DRIBBBLE_SHOT;
            } else if (item instanceof Post) {
                return TYPE_PRODUCT_HUNT_POST;
            }
        }
        return TYPE_LOADING_MORE;
    }

    private PlaidItem getItem(int position) {
        return items.get(position);
    }

    public int getItemColumnSpan(int position) {
        switch (getItemViewType(position)) {
            case TYPE_LOADING_MORE:
                return columns;
            default:
                return getItem(position).colspan;
        }
    }

    private void add(PlaidItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addAndResort(Collection<? extends PlaidItem> newItems) {
        // de-dupe results as the same item can be returned by multiple feeds
        boolean add = true;
        for (PlaidItem newItem : newItems) {
            int count = getDataItemCount();
            for (int i = 0; i < count; i++) {
                PlaidItem existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    // if we find a dupe mark the weight boost field on the first-in, but don't add
                    // the dupe. We use the fact that an item comes from multiple sources to indicate it
                    // is more important and sort it higher
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
        expandPopularItems();
    }

    private void expandPopularItems() {
        // for now just expand the first dribbble image per page which should be
        // the most popular according to #sort.
        // TODO make this smarter & handle other item types
        List<Integer> expandedPositions = new ArrayList<>();
        int page = -1;
        final int count = items.size();
        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Shot && item.page > page) {
                item.colspan = columns;
                page = item.page;
                expandedPositions.add(i);
            } else {
                item.colspan = 1;
            }
        }

        // make sure that any expanded items are at the start of a row
        // so that we don't leave any gaps in the grid
        for (int expandedPos = 0; expandedPos < expandedPositions.size(); expandedPos++) {
            int pos = expandedPositions.get(expandedPos);
            int extraSpannedSpaces = expandedPos * (columns - 1);
            int rowPosition = (pos + extraSpannedSpaces) % columns;
            if (rowPosition != 0) {
                int swapWith = pos + (columns - rowPosition);
                Collections.swap(items, pos, swapWith);
            }
        }
    }

    protected void sort() {
        // calculate the 'weight' for each data type and then sort by that. Each data type has a
        // different metric for weighing it e.g. Dribbble uses likes etc. Weights are 'scoped' to
        // the page they belong to and lower weights are sorted higher in the grid.
        int count = getDataItemCount();
        int maxDesignNewsVotes = 0;
        int maxDesignNewsComments = 0;
        long maxDribbleLikes = 0;
        int maxProductHuntVotes = 0;
        int maxProductHuntComments = 0;

        // work out some maximum values to weigh individual items against
        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Story) {
                maxDesignNewsComments = Math.max(((Story) item).comment_count,
                        maxDesignNewsComments);
                maxDesignNewsVotes = Math.max(((Story) item).vote_count, maxDesignNewsVotes);
            } else if (item instanceof Shot) {
                maxDribbleLikes = Math.max(((Shot) item).likes_count, maxDribbleLikes);
            } else if (item instanceof Post) {
                maxProductHuntComments = Math.max(((Post) item).comments_count,
                        maxProductHuntComments);
                maxProductHuntVotes = Math.max(((Post) item).votes_count, maxProductHuntVotes);
            }
        }

        // now go through and set the weight of each item
        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Story) {
                ((Story) item).weigh(maxDesignNewsComments, maxDesignNewsVotes);
            } else if (item instanceof Shot) {
                ((Shot) item).weigh(maxDribbleLikes);
            } else if (item instanceof Post) {
                ((Post) item).weigh(maxProductHuntComments, maxProductHuntVotes);
            }
            // scope it to the page it came from
            item.weight += item.page;
        }

        // sort by weight
        Collections.sort(items, comparator);
        notifyDataSetChanged(); // TODO call the more specific RV variants
    }

    public void removeDataSource(String dataSource) {
        int i = items.size() - 1;
        while (i >= 0) {
            PlaidItem item = items.get(i);
            if (dataSource.equals(item.dataSource)) {
                items.remove(i);
            }
            i--;
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            return -1L;
        }
        return getItem(position).id;
    }

    @Override
    public int getItemCount() {
        // include loading footer
        return getDataItemCount() + 1;
    }

    public int getDataItemCount() {
        return items.size();
    }

    /* protected */ class DribbbleShotHolder extends RecyclerView.ViewHolder {

        public DribbbleShotHolder(View itemView) {
            super(itemView);
        }

    }

    /* protected */ class DesignerNewsStoryHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.story_title) TextView title;
        @Bind(R.id.story_comments) TextView comments;
        @Bind(R.id.pocket) ImageButton pocket;

        public DesignerNewsStoryHolder(View itemView, boolean pocketIsInstalled) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            pocket.setVisibility(pocketIsInstalled ? View.VISIBLE : View.GONE);
        }
    }

    /* protected */ class ProductHuntStoryHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.hunt_title) TextView title;
        @Bind(R.id.tagline) TextView tagline;
        @Bind(R.id.story_comments) TextView comments;

        public ProductHuntStoryHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /* protected */ class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }

}
