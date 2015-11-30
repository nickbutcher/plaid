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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
    private @Nullable List<FiltersChangedListener> listeners;

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
                    notifyItemChanged(i);
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
                    notifyItemChanged(i);
                    dispatchFiltersChanged(filter);
                    SourceManager.updateSource(filter, context);
                    return;
                }
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
                    holder.itemView.setHasTransientState(true);
                    ObjectAnimator fade = ObjectAnimator.ofInt(holder.filterIcon, ViewUtils.IMAGE_ALPHA,
                            filter.active ? FILTER_ICON_DISABLED_ALPHA : FILTER_ICON_ENABLED_ALPHA);
                    fade.setDuration(300);
                    fade.setInterpolator(AnimationUtils.loadInterpolator(holder.itemView.getContext()
                            , android.R.interpolator.fast_out_slow_in));
                    fade.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setHasTransientState(false);
                        }
                    });
                    fade.start();
                    filter.active = !filter.active;
                    holder.filterName.setEnabled(filter.active);
                    notifyItemChanged(position);
                    SourceManager.updateSource(filter, holder.itemView.getContext());
                    dispatchFiltersChanged(filter);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder vh, int position) {
        final Source filter = filters.get(position);
        vh.isSwipeable = filter.isSwipeDismissable();
        vh.filterName.setText(filter.name);
        vh.filterName.setEnabled(filter.active);
        if (filter.iconRes > 0) {
            vh.filterIcon.setImageDrawable(vh.itemView.getContext().getDrawable(filter.iconRes));
        }
        vh.filterIcon.setImageAlpha(filter.active ? FILTER_ICON_ENABLED_ALPHA :
                FILTER_ICON_DISABLED_ALPHA);
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    @Override
    public long getItemId(int position) {
        return filters.get(position).key.hashCode();
    }

    private boolean isAuthorisedDribbbleSource(Source source) {
        return source.key.equals(SourceManager.SOURCE_DRIBBBLE_FOLLOWING)
                || source.key.equals(SourceManager.SOURCE_DRIBBBLE_USER_LIKES)
                || source.key.equals(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
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

    public void addFilterChangedListener(FiltersChangedListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeFilterChangedListener(FiltersChangedListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void dispatchFiltersChanged(Source filter) {
        if (listeners != null) {
            for (FiltersChangedListener listener : listeners) {
                listener.onFiltersChanged(filter);
            }
        }
    }

    private void dispatchFilterRemoved(Source filter) {
        if (listeners != null) {
            for (FiltersChangedListener listener : listeners) {
                listener.onFilterRemoved(filter);
            }
        }
    }

    public interface FiltersChangedListener {
        void onFiltersChanged(Source changedFilter);
        void onFilterRemoved(Source removed);
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

        public void highlightFilter() {
            itemView.setHasTransientState(true);
            int highlightColor = ContextCompat.getColor(itemView.getContext(), R.color.accent);
            int fadeFromTo = ColorUtils.modifyAlpha(highlightColor, 0);
            ObjectAnimator background = ObjectAnimator.ofArgb(
                    itemView,
                    ViewUtils.BACKGROUND_COLOR,
                    fadeFromTo,
                    highlightColor,
                    fadeFromTo);
            background.setDuration(1000L);
            background.setInterpolator(AnimationUtils.loadInterpolator(itemView.getContext(),
                    android.R.interpolator.linear));
            background.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    itemView.setBackground(null);
                    itemView.setHasTransientState(false);
                }
            });
            background.start();
        }
    }

    @Override
    public void onDribbbleLogin() {
        // no-op
    }

    @Override
    public void onDribbbleLogout() {
        boolean changed = false;
        for (Source filter : filters) {
            if (filter.active && isAuthorisedDribbbleSource(filter)) {
                filter.active = false;
                SourceManager.updateSource(filter, context);
                dispatchFiltersChanged(filter);
                changed = true;
            }
        }
        if (changed) {
            notifyDataSetChanged();
        }
    }

}
