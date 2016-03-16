/*
 * Copyright 2016 Google Inc.
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

package io.plaidapp.data.api.dribbble;

import android.content.Context;

import java.util.List;

import io.plaidapp.data.PaginatedDataManager;
import io.plaidapp.data.PlaidItem;
import io.plaidapp.data.api.dribbble.model.Like;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ShotLikesDataManager extends PaginatedDataManager {

    private final long shotId;
    private Call shotLikesCall;

    public ShotLikesDataManager(Context context, long shotId) {
        super(context);
        this.shotId = shotId;
    }

    public abstract void onLikesLoaded(List<Like> likes);

    @Override
    public void onDataLoaded(List<? extends PlaidItem> data) {
        /* no-op. Use #onLikesLoaded instead please. */
    }

    @Override
    public void cancelLoading() {
        if (shotLikesCall != null) shotLikesCall.cancel();
    }

    @Override
    protected void loadData(int page) {
        shotLikesCall = getDribbbleApi()
                .getShotLikes(shotId, page, DribbbleService.PER_PAGE_DEFAULT);
        shotLikesCall.enqueue(new Callback<List<Like>>() {

            @Override
            public void onResponse(Call<List<Like>> call, Response<List<Like>> response) {
                loadFinished();
                final List<Like> likes = response.body();
                moreDataAvailable = likes.size() == DribbbleService.PER_PAGE_DEFAULT;
                onLikesLoaded(likes);
                shotLikesCall = null;
            }

            @Override
            public void onFailure(Call<List<Like>> call, Throwable t) {
                loadFinished();
                moreDataAvailable = false;
                shotLikesCall = null;
            }
        });
    }
}
