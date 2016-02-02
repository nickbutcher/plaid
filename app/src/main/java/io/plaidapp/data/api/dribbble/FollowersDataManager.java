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
import io.plaidapp.data.api.dribbble.model.Follow;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public abstract class FollowersDataManager extends PaginatedDataManager {

    private final long playerId;

    public FollowersDataManager(Context context, long playerId) {
        super(context);
        this.playerId = playerId;
    }

    public abstract void onFollowersLoaded(List<Follow> followers);

    @Override
    public void onDataLoaded(List<? extends PlaidItem> data) {
        /* no-op. Use #onFollowersLoaded instead please. */
    }

    @Override
    protected void loadData(int page) {
        getDribbbleApi().getUserFollowers(playerId, page, DribbbleService.PER_PAGE_DEFAULT,
                new Callback<List<Follow>>() {

            @Override
            public void success(List<Follow> followers, Response response) {
                loadFinished();
                moreDataAvailable = followers.size() == DribbbleService.PER_PAGE_DEFAULT;
                onFollowersLoaded(followers);
            }

            @Override
            public void failure(RetrofitError error) {
                loadFinished();
                moreDataAvailable = false;
            }
        });
    }
}
