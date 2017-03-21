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

package io.plaidapp.data.api.dribbble;

import android.content.Context;

import java.util.List;

import io.plaidapp.data.PaginatedDataManager;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Responsible for loading a dribbble player's shots. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class PlayerShotsDataManager extends PaginatedDataManager<List<Shot>> {

    public static final String SOURCE_PLAYER_SHOTS = "SOURCE_PLAYER_SHOTS";
    public static final String SOURCE_TEAM_SHOTS = "SOURCE_TEAM_SHOTS";

    private final long userId;
    private final boolean isTeam;
    private Call<List<Shot>> loadShotsCall;

    public PlayerShotsDataManager(Context context, User player) {
        super(context);
        userId = player.id;
        isTeam = player.type.equals("Team");
    }

    @Override
    protected void loadData(int page) {
        if (!isTeam) {
            loadUserShots(page);
        } else {
            loadTeamShots(page);
        }
    }

    @Override
    public void cancelLoading() {
        if (loadShotsCall != null) loadShotsCall.cancel();
    }

    private void loadUserShots(final int page) {
        loadShotsCall = getDribbbleApi()
                .getUsersShots(userId, page, DribbbleService.PER_PAGE_DEFAULT);
        loadShotsCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    final List<Shot> shots = response.body();
                    setPage(shots, page);
                    setDataSource(shots, SOURCE_PLAYER_SHOTS);
                    onDataLoaded(shots);
                    loadFinished();
                    moreDataAvailable = shots.size() == DribbbleService.PER_PAGE_DEFAULT;
                    loadShotsCall = null;
                } else {
                    failure();
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                failure();
            }
        });
    }

    private void loadTeamShots(final int page) {
        loadShotsCall = getDribbbleApi()
                .getTeamShots(userId, page, DribbbleService.PER_PAGE_DEFAULT);
        loadShotsCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    final List<Shot> shots = response.body();
                    setPage(shots, page);
                    setDataSource(shots, SOURCE_TEAM_SHOTS);
                    onDataLoaded(shots);
                    loadFinished();
                    moreDataAvailable = shots.size() == DribbbleService.PER_PAGE_DEFAULT;
                    loadShotsCall = null;
                } else {
                    failure();
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                failure();
            }
        });
    }

    private void failure() {
        loadFinished();
        loadShotsCall = null;
        moreDataAvailable = false;
    }

}
