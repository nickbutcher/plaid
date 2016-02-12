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
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.plaidapp.R;
import io.plaidapp.data.Source;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.data.prefs.SourceManager;
import io.plaidapp.ui.recyclerview.ItemTouchHelperAdapter;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.ViewUtils;

/**
 * Adapter for showing the list of data sources used as filters for the home grid.
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder>
        implements ItemTouchHelperAdapter, DribbblePrefs.DribbbleLoginStatusListener {

    public interface FilterAuthoriser {
        void requestDribbbleAuthorisation(View sharedElement, Source forSource);
    }

    private static final int FILTER_ICON_ENABLED_ALPHA = 179; // 70%
    private static final int FILTER_ICON_DISABLED_ALPHA = 51; // 20%

    private final List<Source> filters;
    private final FilterAuthoriser authoriser;
    private final Context context;
    private @Nullable List<FiltersChangedCallbacks> callbacks;

    public FilterAdapter(@NonNull Context context,
                         @NonNull List<Source> filters,
                         @NonNull FilterAuthoriser authoriser) {
        this.context = context.getApplicationContext();
        this.filters = filters;
        this.authoriser = authoriser;
        setHasStableIds(true);
    }

    public List<Source> getFilters() {
        return filters;
    }

    /**
     * Adds a new data source to the list of filters. If the source already exists then it is simply
     * activated.
     *
     * @param toAdd the source to add
     * @return whether the filter was added (i.e. if it did not already exist)
     */
    public boolean addFilter(Source toAdd) {
        // first check if it already exists
        final int count = filters.size();
        for (int i = 0; i < count; i++) {
            Source existing = filters.get(i);
            if (existing.getClass() == toAdd.getClass()
                    && existing.key.equalsIgnoreCase(toAdd.key)) {
                // already exists, just ensure it's active
                if (!existing.active) {
                    existing.active = true;
                    dispatchFiltersChanged(existing);
                    notifyItemChanged(i, FilterAnimator.FILTER_ENABLED);
                    SourceManager.updateSource(existing, context);
                }
                return false;
            }
        }
        // didn't already exist, so add it
        filters.add(toAdd);
        Collections.sort(filters, new Source.SourceComparator());
        dispatchFiltersChanged(toAdd);
        notifyDataSetChanged();
        SourceManager.addSource(toAdd, context);
        return true;
    }

    public void removeFilter(Source removing) {
        int position = filters.indexOf(removing);
        filters.remove(position);
        notifyItemRemoved(position);
        dispatchFilterRemoved(removing);
        SourceManager.removeSource(removing, context);
    }

    public int getFilterPosition(Source filter) {
        return filters.indexOf(filter);
    }

    public void enableFilterByKey(@NonNull String key, @NonNull Context context) {
        final int count = filters.size();
        for (int i = 0; i < count; i++) {
            Source filter = filters.get(i);
            if (filter.key.equals(key)) {
                if (!filter.active) {
                    filter.active = true;
                    notifyItemChanged(i, FilterAnimator.FILTER_ENABLED);
                    dispatchFiltersChanged(filter);
                    SourceManager.updateSource(filter, context);
                }
                return;
            }
        }
    }

    public void highlightFilter(int adapterPosition) {
        notifyItemChanged(adapterPosition, FilterAnimator.HIGHLIGHT);
    }

    @Override
    public void onDribbbleLogin() {
        // no-op
    }

    @Override
    public void onDribbbleLogout() {
        for (int i = 0; i < filters.size(); i++) {
            Source filter = filters.get(i);
            if (filter.active && isAuthorisedDribbbleSource(filter)) {
                filter.active = false;
                SourceManager.updateSource(filter, context);
                dispatchFiltersChanged(filter);
                notifyItemChanged(i, FilterAnimator.FILTER_DISABLED);
            }
        }
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final FilterViewHolder holder = new FilterViewHolder(LayoutInflater.from(viewGroup
                .getContext()).inflate(R.layout.filter_item, viewGroup, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                final Source filter = filters.get(position);
                if (isAuthorisedDribbbleSource(filter) &&
                        !DribbblePrefs.get(holder.itemView.getContext()).isLoggedIn()) {
                    authoriser.requestDribbbleAuthorisation(holder.filterIcon, filter);
                } else {
                    filter.active = !filter.active;
                    holder.filterName.setEnabled(filter.active);
                    notifyItemChanged(position, filter.active ? FilterAnimator.FILTER_ENABLED
                            : FilterAnimator.FILTER_DISABLED);
                    SourceManager.updateSource(filter, holder.itemView.getContext());
                    dispatchFiltersChanged(filter);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        final Source filter = filters.get(position);
        holder.isSwipeable = filter.isSwipeDismissable();
        holder.filterName.setText(filter.name);
        holder.filterName.setEnabled(filter.active);
        if (filter.iconRes > 0) {
            holder.filterIcon.setImageDrawable(
                    holder.itemView.getContext().getDrawable(filter.iconRes));
        }
        holder.filterIcon.setImageAlpha(filter.active ? FILTER_ICON_ENABLED_ALPHA :
                FILTER_ICON_DISABLED_ALPHA);
    }

    @Override
    public void onBindViewHolder(FilterViewHolder holder,
                                 int position,
                                 List<Object> partialChangePayloads) {
        if (!partialChangePayloads.isEmpty()) {
            // if we're doing a partial re-bind i.e. an item is enabling/disabling or being
            // highlighted then data hasn't changed. Just set state based on the payload
            boolean filterEnabled = partialChangePayloads.contains(FilterAnimator.FILTER_ENABLED);
            boolean filterDisabled = partialChangePayloads.contains(FilterAnimator.FILTER_DISABLED);
            if (filterEnabled || filterDisabled) {
                holder.filterName.setEnabled(filterEnabled);
                // icon is handled by the animator
            }
            // nothing to do for highlight
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    @Override
    public long getItemId(int position) {
        return filters.get(position).key.hashCode();
    }

    @Override
    public void onItemDismiss(int position) {
        Source removing = filters.get(position);
        if (removing.isSwipeDismissable()) {
            removeFilter(removing);
        }
    }

    public int getEnabledSourcesCount() {
        int count = 0;
        for (Source source : filters) {
            if (source.active) {
                count++;
            }
        }
        return count;
    }

    public void registerFilterChangedCallback(FiltersChangedCallbacks callback) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(callback);
    }

    public void unregisterFilterChangedCallback(FiltersChangedCallbacks callback) {
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(callback);
        }
    }

    private boolean isAuthorisedDribbbleSource(Source source) {
        return source.key.equals(SourceManager.SOURCE_DRIBBBLE_FOLLOWING)
                || source.key.equals(SourceManager.SOURCE_DRIBBBLE_USER_LIKES)
                || source.key.equals(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
    }

    private void dispatchFiltersChanged(Source filter) {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (FiltersChangedCallbacks callback : callbacks) {
                callback.onFiltersChanged(filter);
            }
        }
    }

    private void dispatchFilterRemoved(Source filter) {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (FiltersChangedCallbacks callback : callbacks) {
                callback.onFilterRemoved(filter);
            }
        }
    }

    public static abstract class FiltersChangedCallbacks {
        public void onFiltersChanged(Source changedFilter) { }
        public void onFilterRemoved(Source removed) { }
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {

        public TextView filterName;
        public ImageView filterIcon;
        public boolean isSwipeable;

        public FilterViewHolder(View itemView) {
            super(itemView);
            filterName = (TextView) itemView.findViewById(R.id.filter_name);
            filterIcon = (ImageView) itemView.findViewById(R.id.filter_icon);
        }
    }

    public static class FilterAnimator extends DefaultItemAnimator {

        public static final int FILTER_ENABLED = 1;
        public static final int FILTER_DISABLED = 2;
        public static final int HIGHLIGHT = 3;

        @Override
        public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
            return true;
        }

        @Override
        public ItemHolderInfo obtainHolderInfo() {
            return new FilterHolderInfo();
        }

        /* package */ static class FilterHolderInfo extends ItemHolderInfo {
            boolean doEnable;
            boolean doDisable;
            boolean doHighlight;
        }

        @NonNull
        @Override
        public ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state,
                                                         RecyclerView.ViewHolder viewHolder,
                                                         int changeFlags,
                                                         List<Object> payloads) {
            FilterHolderInfo info = (FilterHolderInfo)
                    super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
            if (!payloads.isEmpty()) {
                info.doEnable = payloads.contains(FILTER_ENABLED);
                info.doDisable = payloads.contains(FILTER_DISABLED);
                info.doHighlight = payloads.contains(HIGHLIGHT);
            }
            return info;
        }

        @Override
        public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                     RecyclerView.ViewHolder newHolder,
                                     ItemHolderInfo preInfo,
                                     ItemHolderInfo postInfo) {
            if (newHolder instanceof FilterViewHolder && preInfo instanceof FilterHolderInfo) {
                final FilterViewHolder holder = (FilterViewHolder) newHolder;
                final FilterHolderInfo info = (FilterHolderInfo) preInfo;

                if (info.doEnable || info.doDisable) {
                    ObjectAnimator iconAlpha = ObjectAnimator.ofInt(holder.filterIcon,
                                    ViewUtils.IMAGE_ALPHA,
                                    info.doEnable ? FILTER_ICON_ENABLED_ALPHA :
                                            FILTER_ICON_DISABLED_ALPHA);
                    iconAlpha.setDuration(300L);
                    iconAlpha.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(holder
                            .itemView.getContext()));
                    iconAlpha.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            dispatchChangeStarting(holder, false);
                            holder.itemView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setHasTransientState(false);
                            dispatchChangeFinished(holder, false);
                        }
                    });
                    iconAlpha.start();
                } else if (info.doHighlight) {
                    int highlightColor =
                            ContextCompat.getColor(holder.itemView.getContext(), R.color.accent);
                    int fadeFromTo = ColorUtils.modifyAlpha(highlightColor, 0);
                    ObjectAnimator highlightBackground = ObjectAnimator.ofArgb(
                            holder.itemView,
                            ViewUtils.BACKGROUND_COLOR,
                            fadeFromTo,
                            highlightColor,
                            fadeFromTo);
                    highlightBackground.setDuration(1000L);
                    highlightBackground.setInterpolator(new LinearInterpolator());
                    highlightBackground.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            dispatchChangeStarting(holder, false);
                            holder.itemView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setBackground(null);
                            holder.itemView.setHasTransientState(false);
                            dispatchChangeFinished(holder, false);
                        }
                    });
                    highlightBackground.start();
                }
            }
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        }
    }

}
