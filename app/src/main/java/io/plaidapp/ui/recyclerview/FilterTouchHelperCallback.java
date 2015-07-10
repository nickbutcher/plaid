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

package io.plaidapp.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import io.plaidapp.ui.FilterAdapter;

/**
 * Callback for item swipe-dismissing
 */
public class FilterTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private final ItemTouchHelperAdapter adapter;

    public FilterTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        super(0, ItemTouchHelper.START);
        this.adapter = adapter;
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView
            .ViewHolder target) {
        // nothing to do here
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // can only swipe-dismiss certain sources
        return makeMovementFlags(0, ((FilterAdapter.FilterViewHolder) viewHolder).isSwipeable ?
                ItemTouchHelper.START : 0);
    }
}
