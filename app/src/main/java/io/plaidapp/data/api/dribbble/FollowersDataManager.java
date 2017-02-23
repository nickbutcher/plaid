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
import io.plaidapp.data.api.dribbble.model.Follow;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Loads a dribbble user's followers.
 */
public abstract class FollowersDataManager extends PaginatedDataManager<List<Follow>> {

    private final long playerId;
    private Call<List<Follow>> userFollowersCall;

    public FollowersDataManager(Context context, long playerId) {
        super(context);
        this.playerId = playerId;
    }

    @Override
    public void cancelLoading() {
        if (userFollowersCall != null) userFollowersCall.cancel();
    }

    @Override
    protected void loadData(int page) {
        userFollowersCall = getDribbbleApi()
                .getUserFollowers(playerId, page, DribbbleService.PER_PAGE_DEFAULT);
        userFollowersCall.enqueue(new Callback<List<Follow>>() {

            @Override
            public void onResponse(Call<List<Follow>> call, Response<List<Follow>> response) {
                if (response.isSuccessful()) {
                    loadFinished();
                    moreDataAvailable = response.body().size() == DribbbleService.PER_PAGE_DEFAULT;
                    onDataLoaded(response.body());
                    userFollowersCall = null;
                } else {
                    failure();
                }
            }

            @Override
            public void onFailure(Call<List<Follow>> call, Throwable t) {
                failure();
            }

            private void failure() {
                loadFinished();
                moreDataAvailable = false;
                userFollowersCall = null;
            }
        });
    }
}
