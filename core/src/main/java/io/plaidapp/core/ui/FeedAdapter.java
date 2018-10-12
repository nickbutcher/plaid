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

package io.plaidapp.core.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import io.plaidapp.core.R;
import io.plaidapp.core.data.DataLoadingSubject;
import io.plaidapp.core.data.PlaidItem;
import io.plaidapp.core.data.PlaidItemSorting;
import io.plaidapp.core.data.pocket.PocketUtils;
import io.plaidapp.core.data.prefs.SourceManager;
import io.plaidapp.core.designernews.data.stories.model.Story;
import io.plaidapp.core.designernews.domain.StoryWeigher;
import io.plaidapp.core.designernews.ui.stories.StoryViewHolder;
import io.plaidapp.core.dribbble.data.api.ShotWeigher;
import io.plaidapp.core.dribbble.data.api.model.Images;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.producthunt.data.api.PostWeigher;
import io.plaidapp.core.producthunt.data.api.model.Post;
import io.plaidapp.core.producthunt.ui.ProductHuntPostHolder;
import io.plaidapp.core.ui.transitions.ReflowText;
import io.plaidapp.core.ui.widget.BadgedFourThreeImageView;
import io.plaidapp.core.util.*;
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.core.util.glide.DribbbleTarget;
import io.plaidapp.core.util.glide.GlideApp;
import kotlin.Unit;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator;

/**
 * Adapter for displaying a grid of {@link PlaidItem}s.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DataLoadingSubject.DataLoadingCallbacks,
        ListPreloader.PreloadModelProvider<Shot> {

    public static final int REQUEST_CODE_VIEW_SHOT = 5407;

    private static final int TYPE_DESIGNER_NEWS_STORY = 0;
    private static final int TYPE_DRIBBBLE_SHOT = 1;
    private static final int TYPE_PRODUCT_HUNT_POST = 2;
    private static final int TYPE_LOADING_MORE = -1;

    // we need to hold on to an activity ref for the shared element transitions :/
    final Activity host;
    private final LayoutInflater layoutInflater;
    private final PlaidItemSorting.PlaidItemComparator comparator;
    private final boolean pocketIsInstalled;
    private final @Nullable
    DataLoadingSubject dataLoading;
    private final int columns;
    private final ColorDrawable[] shotLoadingPlaceholders;
    private final ViewPreloadSizeProvider<Shot> shotPreloadSizeProvider;

    private final @ColorInt
    int initialGifBadgeColor;
    private List<PlaidItem> items;
    private boolean showLoadingMore = false;
    private PlaidItemSorting.NaturalOrderWeigher naturalOrderWeigher;
    private ShotWeigher shotWeigher;
    private StoryWeigher storyWeigher;
    private PostWeigher postWeigher;

    @Inject
    public FeedAdapter(Activity hostActivity,
            @Nullable DataLoadingSubject dataLoading,
            int columns,
            boolean pocketInstalled, ViewPreloadSizeProvider<Shot> shotPreloadSizeProvider) {
        this.host = hostActivity;
        this.dataLoading = dataLoading;
        if (dataLoading != null) {
            dataLoading.registerCallback(this);
        }
        this.columns = columns;
        this.pocketIsInstalled = pocketInstalled;
        this.shotPreloadSizeProvider = shotPreloadSizeProvider;
        layoutInflater = LayoutInflater.from(host);
        comparator = new PlaidItemSorting.PlaidItemComparator();
        items = new ArrayList<>();
        setHasStableIds(true);

        // get the dribbble shot placeholder colors & badge color from the theme
        final TypedArray a = host.obtainStyledAttributes(R.styleable.DribbbleFeed);
        final int loadingColorArrayId =
                a.getResourceId(R.styleable.DribbbleFeed_shotLoadingPlaceholderColors, 0);
        if (loadingColorArrayId != 0) {
            int[] placeholderColors = host.getResources().getIntArray(loadingColorArrayId);
            shotLoadingPlaceholders = new ColorDrawable[placeholderColors.length];
            for (int i = 0; i < placeholderColors.length; i++) {
                shotLoadingPlaceholders[i] = new ColorDrawable(placeholderColors[i]);
            }
        } else {
            shotLoadingPlaceholders = new ColorDrawable[]{new ColorDrawable(Color.DKGRAY)};
        }
        final int initialGifBadgeColorId =
                a.getResourceId(R.styleable.DribbbleFeed_initialBadgeColor, 0);
        initialGifBadgeColor = initialGifBadgeColorId != 0 ?
                ContextCompat.getColor(host, initialGifBadgeColorId) : 0x40ffffff;
        a.recycle();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DESIGNER_NEWS_STORY:
                return createDesignerNewsStoryHolder(parent);
            case TYPE_DRIBBBLE_SHOT:
                return createDribbbleShotHolder(parent);
            case TYPE_PRODUCT_HUNT_POST:
                return createProductHuntStoryHolder(parent);
            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(
                        layoutInflater.inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_DESIGNER_NEWS_STORY:
                ((StoryViewHolder) holder).bind((Story) getItem(position));
                break;
            case TYPE_DRIBBBLE_SHOT:
                bindDribbbleShotHolder(
                        (Shot) getItem(position), (DribbbleShotHolder) holder, position);
                break;
            case TYPE_PRODUCT_HUNT_POST:
                ((ProductHuntPostHolder) holder).bind((Post) getItem(position));
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHolder((LoadingMoreHolder) holder, position);
                break;
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof DribbbleShotHolder) {
            // reset the badge & ripple which are dynamically determined
            DribbbleShotHolder shotHolder = (DribbbleShotHolder) holder;
            shotHolder.image.setBadgeColor(initialGifBadgeColor);
            shotHolder.image.setDrawBadge(false);
            shotHolder.image.setForeground(
                    ContextCompat.getDrawable(host, R.drawable.mid_grey_ripple));
        }
    }

    @NonNull
    private StoryViewHolder createDesignerNewsStoryHolder(ViewGroup parent) {
        final StoryViewHolder holder = new StoryViewHolder(
                layoutInflater.inflate(R.layout.designer_news_story_item, parent, false),
                pocketIsInstalled,
                (story, position) -> {
                    PocketUtils.addToPocket(host, story.getUrl());
                    // notify changed with a payload asking RV to run the anim
                    notifyItemChanged(position, HomeGridItemAnimator.ADD_TO_POCKET);
                    return Unit.INSTANCE;
                },
                data -> {
                    openDesignerNewsStory(data);
                    return Unit.INSTANCE;
                },
                data -> {
                    if (data.getStory().getUrl() != null) {
                        openTabDesignerNews(data.getStory());
                    } else {
                        openDesignerNewsStory(data);
                    }
                    return Unit.INSTANCE;
                }
        );

        return holder;
    }

    private void openDesignerNewsStory(StoryViewHolder.TransitionData data) {
        final Intent intent = ActivityHelper.intentTo(Activities.DesignerNews.Story.INSTANCE);
        intent.putExtra(Activities.DesignerNews.Story.EXTRA_STORY_ID, data.getStory().getId());
        ReflowText.addExtras(intent, new ReflowText.ReflowableTextView(data.getTitle()));
        setGridItemContentTransitions(data.getItemView());

        // on return, fade the pocket & comments buttons in
        host.setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View>
                    sharedElements, List<View> sharedElementSnapshots) {
                host.setExitSharedElementCallback(null);
                notifyItemChanged(data.getPosition(), HomeGridItemAnimator.STORY_COMMENTS_RETURN);
            }
        });

        final ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(host,
                data.getSharedElements());
        host.startActivity(intent, options.toBundle());
    }

    private void openTabDesignerNews(Story story) {
        CustomTabActivityHelper.openCustomTab(host,
                Activities.DesignerNews.Story.INSTANCE
                        .customTabIntent(host, story, null).build(),
                Uri.parse(story.getUrl()));
    }

    @NonNull
    private DribbbleShotHolder createDribbbleShotHolder(ViewGroup parent) {
        final DribbbleShotHolder holder = new DribbbleShotHolder(
                layoutInflater.inflate(R.layout.dribbble_shot_item, parent, false));
        holder.image.setBadgeColor(initialGifBadgeColor);
        holder.image.setOnClickListener(view -> {
            Intent intent = ActivityHelper.intentTo(Activities.Dribbble.Shot.INSTANCE);
            intent.putExtra(Activities.Dribbble.Shot.EXTRA_SHOT_ID,
                    getItem(holder.getAdapterPosition()).getId());
            setGridItemContentTransitions(holder.image);
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(host,
                            Pair.create(view, host.getString(R.string.transition_shot)),
                            Pair.create(view, host.getString(R.string
                                    .transition_shot_background)));
            host.startActivityForResult(intent, REQUEST_CODE_VIEW_SHOT, options.toBundle());
        });
        // play animated GIFs whilst touched
        holder.image.setOnTouchListener((v, event) -> {
            // check if it's an event we care about, else bail fast
            final int action = event.getAction();
            if (!(action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL)) {
                return false;
            }

            // get the image and check if it's an animated GIF
            final Drawable drawable = holder.image.getDrawable();
            if (drawable == null) return false;
            GifDrawable gif = null;
            if (drawable instanceof GifDrawable) {
                gif = (GifDrawable) drawable;
            } else if (drawable instanceof TransitionDrawable) {
                // we fade in images on load which uses a TransitionDrawable; check its layers
                TransitionDrawable fadingIn = (TransitionDrawable) drawable;
                for (int i = 0; i < fadingIn.getNumberOfLayers(); i++) {
                    if (fadingIn.getDrawable(i) instanceof GifDrawable) {
                        gif = (GifDrawable) fadingIn.getDrawable(i);
                        break;
                    }
                }
            }
            if (gif == null) return false;
            // GIF found, start/stop it on press/lift
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    gif.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gif.stop();
                    break;
            }
            return false;
        });
        return holder;
    }

    private void bindDribbbleShotHolder(final Shot shot,
                                        final DribbbleShotHolder holder,
                                        int position) {
        final Images.ImageSize imageSize = shot.getImages().bestSize();
        GlideApp.with(host)
                .load(shot.getImages().best())
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        if (!shot.getHasFadedIn()) {
                            holder.image.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            final ObjectAnimator saturation = ObjectAnimator.ofFloat(
                                    cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                            saturation.addUpdateListener(valueAnimator -> {
                                // just animating the color matrix does not invalidate the
                                // drawable so need this update listener.  Also have to create a
                                // new CMCF as the matrix is immutable :(
                                holder.image.setColorFilter(new ColorMatrixColorFilter(cm));
                            });
                            saturation.setDuration(2000L);
                            saturation.setInterpolator(getFastOutSlowInInterpolator(host));
                            saturation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    holder.image.clearColorFilter();
                                    holder.image.setHasTransientState(false);
                                }
                            });
                            saturation.start();
                            shot.setHasFadedIn(true);
                        }
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                })
                .placeholder(shotLoadingPlaceholders[position % shotLoadingPlaceholders.length])
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .override(imageSize.getWidth(), imageSize.getHeight())
                .into(new DribbbleTarget(holder.image, false));
        // need both placeholder & background to prevent seeing through shot as it fades in
        holder.image.setBackground(
                shotLoadingPlaceholders[position % shotLoadingPlaceholders.length]);
        holder.image.setDrawBadge(shot.getAnimated());
        // need a unique transition name per shot, let's use it's url
        holder.image.setTransitionName(shot.getHtmlUrl());
        shotPreloadSizeProvider.setView(holder.image);
    }

    @NonNull
    private ProductHuntPostHolder createProductHuntStoryHolder(ViewGroup parent) {
        final ProductHuntPostHolder holder = new ProductHuntPostHolder(
                layoutInflater.inflate(R.layout.product_hunt_item, parent, false),
                post -> {
                    openTabForProductHunt(post.getDiscussionUrl());
                    return Unit.INSTANCE;
                },
                post -> {
                    openTabForProductHunt(post.getRedirectUrl());
                    return Unit.INSTANCE;
                });
        return holder;
    }

    private void openTabForProductHunt(String uri) {
        CustomTabActivityHelper.openCustomTab(
                host,
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(host, R.color.product_hunt))
                        .addDefaultShareMenuItem()
                        .build(),
                Uri.parse(uri));
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility((position > 0
                && dataLoading != null
                && dataLoading.isDataLoading())
                ? View.VISIBLE : View.INVISIBLE);
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
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public int getItemColumnSpan(int position) {
        switch (getItemViewType(position)) {
            case TYPE_LOADING_MORE:
                return columns;
            default:
                return getItem(position).getColspan();
        }
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * Main entry point for adding items to this adapter. Takes care of de-duplicating items and
     * sorting them (depending on the data source). Will also expand some items to span multiple
     * grid columns.
     */
    public void addAndResort(List<? extends PlaidItem> newItems) {
        weighItems(newItems);
        deduplicateAndAdd(newItems);
        sort();
        expandPopularItems();
        notifyDataSetChanged();
    }

    /**
     * Calculate a 'weight' [0, 1] for each data type for sorting. Each data type/source has a
     * different metric for weighing it e.g. Dribbble uses likes etc. but some sources should keep
     * the order returned by the API. Weights are 'scoped' to the page they belong to and lower
     * weights are sorted earlier in the grid (i.e. in ascending weight).
     */
    private void weighItems(List<? extends PlaidItem> items) {
        if (items == null || items.isEmpty()) return;

        PlaidItemSorting.PlaidItemGroupWeigher weigher = null;
        switch (items.get(0).getDataSource()) {
            // some sources should just use the natural order i.e. as returned by the API as users
            // have an expectation about the order they appear in
            case SourceManager.SOURCE_PRODUCT_HUNT:
                if (naturalOrderWeigher == null) {
                    naturalOrderWeigher = new PlaidItemSorting.NaturalOrderWeigher();
                }
                weigher = naturalOrderWeigher;
                break;
            default:
                // otherwise use our own weight calculation. We prefer this as it leads to a less
                // regular pattern of items in the grid
                if (items.get(0) instanceof Shot) {
                    if (shotWeigher == null) shotWeigher = new ShotWeigher();
                    weigher = shotWeigher;
                } else if (items.get(0) instanceof Story) {
                    if (storyWeigher == null) storyWeigher = new StoryWeigher();
                    weigher = storyWeigher;
                } else if (items.get(0) instanceof Post) {
                    if (postWeigher == null) postWeigher = new PostWeigher();
                    weigher = postWeigher;
                }
        }
        weigher.weigh(items);
    }

    /**
     * De-dupe as the same item can be returned by multiple feeds
     */
    private void deduplicateAndAdd(List<? extends PlaidItem> newItems) {
        final int count = getDataItemCount();
        for (PlaidItem newItem : newItems) {
            boolean add = true;
            for (int i = 0; i < count; i++) {
                PlaidItem existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                add(newItem);
            }
        }
    }

    private void add(PlaidItem item) {
        items.add(item);
    }

    private void sort() {
        Collections.sort(items, comparator); // sort by weight
    }

    private void expandPopularItems() {
        // for now just expand the first dribbble image per page which should be
        // the most popular according to our weighing & sorting
        List<Integer> expandedPositions = new ArrayList<>();
        int page = -1;
        final int count = items.size();
        for (int i = 0; i < count; i++) {
            PlaidItem item = getItem(i);
            if (item instanceof Shot && item.getPage() > page) {
                item.setColspan(columns);
                page = item.getPage();
                expandedPositions.add(i);
            } else {
                item.setColspan(1);
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
                if (swapWith < items.size()) {
                    Collections.swap(items, pos, swapWith);
                }
            }
        }
    }

    public void removeDataSource(String dataSource) {
        for (int i = items.size() - 1; i >= 0; i--) {
            PlaidItem item = items.get(i);
            if (dataSource.equals(item.getDataSource())) {
                items.remove(i);
            }
        }
        sort();
        expandPopularItems();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            return -1L;
        }
        return getItem(position).getId();
    }

    public int getItemPosition(final long itemId) {
        for (int position = 0; position < items.size(); position++) {
            if (getItem(position).getId() == itemId) return position;
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + (showLoadingMore ? 1 : 0);
    }

    /**
     * The shared element transition to dribbble shots & dn stories can intersect with the FAB.
     * This can cause a strange layers-passing-through-each-other effect. On return hide the FAB
     * and animate it back in after the transition.
     */
    private void setGridItemContentTransitions(View gridItem) {
        final View fab = host.findViewById(R.id.fab);
        if (!ViewUtils.viewsIntersect(gridItem, fab)) return;

        Transition reenter = TransitionInflater.from(host)
                .inflateTransition(R.transition.grid_overlap_fab_reenter);
        reenter.addListener(new TransitionUtils.TransitionListenerAdapter() {

            @Override
            public void onTransitionEnd(Transition transition) {
                // we only want these content transitions in certain cases so clear out when done.
                host.getWindow().setReenterTransition(null);
            }
        });
        host.getWindow().setReenterTransition(reenter);
    }

    public int getDataItemCount() {
        return items.size();
    }

    private int getLoadingMoreItemPosition() {
        return showLoadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    @Override
    public void dataStartedLoading() {
        if (showLoadingMore) return;
        showLoadingMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }

    @Override
    public void dataFinishedLoading() {
        if (!showLoadingMore) return;
        final int loadingPos = getLoadingMoreItemPosition();
        showLoadingMore = false;
        notifyItemRemoved(loadingPos);
    }

    public static SharedElementCallback createSharedElementReenterCallback(
            @NonNull Context context) {
        final String shotTransitionName = context.getString(R.string.transition_shot);
        final String shotBackgroundTransitionName =
                context.getString(R.string.transition_shot_background);
        return new SharedElementCallback() {

            /**
             * We're performing a slightly unusual shared element transition i.e. from one view
             * (image in the grid) to two views (the image & also the background of the details
             * view, to produce the expand effect). After changing orientation, the transition
             * system seems unable to map both shared elements (only seems to map the shot, not
             * the background) so in this situation we manually map the background to the
             * same view.
             */
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (sharedElements.size() != names.size()) {
                    // couldn't map all shared elements
                    final View sharedShot = sharedElements.get(shotTransitionName);
                    if (sharedShot != null) {
                        // has shot so add shot background, mapped to same view
                        sharedElements.put(shotBackgroundTransitionName, sharedShot);
                    }
                }
            }
        };
    }

    @NonNull
    @Override
    public List<Shot> getPreloadItems(int position) {
        PlaidItem item = getItem(position);
        if (item instanceof Shot) {
            return Collections.singletonList((Shot) item);
        }
        return Collections.emptyList();
    }

    @Override
    public RequestBuilder<Drawable> getPreloadRequestBuilder(Shot item) {
        return GlideApp.with(host).load(item.getImages().best());
    }

    static class DribbbleShotHolder extends RecyclerView.ViewHolder {

        BadgedFourThreeImageView image;

        DribbbleShotHolder(View itemView) {
            super(itemView);
            image = (BadgedFourThreeImageView) itemView;
        }

    }

    static class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }

}
