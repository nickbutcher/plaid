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
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.plaid.R;
import com.example.android.plaid.data.Source;
import com.example.android.plaid.data.prefs.DribbblePrefs;
import com.example.android.plaid.data.prefs.SourceManager;
import com.example.android.plaid.ui.recyclerview.ItemTouchHelperAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickbutcher on 7/31/14.
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder>
        implements ItemTouchHelperAdapter {

    private static final float FILTER_TEXT_ENABLED_ALPHA = 1.0f;
    private static final float FILTER_TEXT_DISABLED_ALPHA = 0.4f;
    private static final float FILTER_ICON_ENABLED_ALPHA = 0.6f;
    private static final float FILTER_ICON_DISABLED_ALPHA = 0.2f;
    private final List<Source> filters;
    private @Nullable List<FiltersChangedListener> listeners;

    public FilterAdapter(List<Source> filters) {
        this.filters = filters;
    }

    public List<Source> getFilters() {
        return filters;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new FilterViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                .filter_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder vh, int position) {
        final Source filter = filters.get(position);
        vh.filterName.setText(filter.name);
        vh.filterName.setAlpha(filter.active ? FILTER_TEXT_ENABLED_ALPHA :
                FILTER_TEXT_DISABLED_ALPHA);
        if (filter.res > 0) {
            vh.filterIcon.setImageDrawable(vh.itemView.getContext().getDrawable(filter.res));
        }
        vh.filterIcon.setAlpha(filter.active ? FILTER_ICON_ENABLED_ALPHA :
                FILTER_ICON_DISABLED_ALPHA);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // special case for dribble following which requires auth
                if (!filter.key.equals(SourceManager.SOURCE_DRIBBBLE_FOLLOWING) || new
                        DribbblePrefs(vh.itemView.getContext()).isLoggedIn()) {

                    vh.itemView.setHasTransientState(true);
                    AnimatorSet fade = new AnimatorSet();
                    fade.playTogether(
                            ObjectAnimator.ofFloat(vh.filterName, View.ALPHA, filter.active ?
                                    FILTER_TEXT_DISABLED_ALPHA : FILTER_TEXT_ENABLED_ALPHA),
                            ObjectAnimator.ofFloat(vh.filterIcon, View.ALPHA, filter.active ?
                                    FILTER_ICON_DISABLED_ALPHA : FILTER_ICON_ENABLED_ALPHA));
                    fade.setDuration(300);
                    fade.setInterpolator(AnimationUtils.loadInterpolator(vh.itemView.getContext()
                            , android.R.interpolator.fast_out_slow_in));
                    fade.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            vh.itemView.setHasTransientState(false);
                        }
                    });
                    fade.start();
                    filter.active = !filter.active;
                    notifyItemChanged(vh.getPosition());
                    SourceManager.updateSource(filter, vh.itemView.getContext());
                    dispatchFiltersChanged(filter);
                } else {
                    // TODO enable the filter after a successful login
                    //ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    // (Activity) getContext(), view, "login");
                    vh.itemView.getContext().startActivity(new Intent(vh.itemView.getContext(),
                            DribbbleLogin.class)); //, options.toBundle());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public void disableAuthorisedDribbleSources(Context context) {
        for (Source filter : filters) {
            if (filter.key.equals(SourceManager.SOURCE_DRIBBBLE_FOLLOWING)) {
                if (filter.active) {
                    filter.active = false;
                    SourceManager.updateSource(filter, context);
                    dispatchFiltersChanged(filter);
                    notifyDataSetChanged();
                }
                break;
            }
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // todo
    }

    @Override
    public void onItemDismiss(int position) {
        // todo
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
        listeners.remove(listener);
    }

    private void dispatchFiltersChanged(Source filter) {
        if (listeners != null) {
            for (FiltersChangedListener listener : listeners) {
                listener.onFiltersChanged(filter);
            }
        }
    }

    public interface FiltersChangedListener {
        void onFiltersChanged(Source changedFilter);
    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder {

        public TextView filterName;
        public ImageView filterIcon;

        public FilterViewHolder(View itemView) {
            super(itemView);
            filterName = (TextView) itemView.findViewById(R.id.filter_name);
            filterIcon = (ImageView) itemView.findViewById(R.id.filter_icon);
        }
    }

}
