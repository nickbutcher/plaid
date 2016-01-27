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

package io.plaidapp.data;

import android.content.Context;

import java.util.List;

import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Responsible for loading a dribbble player's shots. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class PlayerDataManager extends BaseDataManager {

    public static final String SOURCE_PLAYER_SHOTS = "SOURCE_PLAYER_SHOTS";
    public static final String SOURCE_TEAM_SHOTS = "SOURCE_TEAM_SHOTS";

    private final long userId;
    private final boolean isTeam;

    // state
    private int page = 0;
    private boolean moreShotsToLoad = true;

    public PlayerDataManager(Context context, User player) {
        super(context);
        this.userId = player.id;
        this.isTeam = player.type.equals("Team");
    }

    public void loadMore() {
        if (!moreShotsToLoad) return;

        if (!isTeam) {
            loadUserShots();
        } else {
            loadTeamShots();
        }
    }

    private void loadUserShots() {
        page++;
        loadStarted();
        getDribbbleApi().getUsersShots(userId, page, DribbbleService.PER_PAGE_DEFAULT,
                new Callback<List<Shot>>() {
                    @Override
                    public void success(List<Shot> shots, Response response) {
                        setPage(shots, page);
                        setDataSource(shots, SOURCE_PLAYER_SHOTS);
                        onDataLoaded(shots);
                        loadFinished();
                        moreShotsToLoad = shots.size() == DribbbleService.PER_PAGE_DEFAULT;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loadFinished();
                    }
                });
    }

    private void loadTeamShots() {
        page++;
        loadStarted();
        getDribbbleApi().getTeamShots(userId, page, DribbbleService.PER_PAGE_DEFAULT,
                new Callback<List<Shot>>() {
                    @Override
                    public void success(List<Shot> shots, Response response) {
                        setPage(shots, page);
                        setDataSource(shots, SOURCE_TEAM_SHOTS);
                        onDataLoaded(shots);
                        loadFinished();
                        moreShotsToLoad = shots.size() == DribbbleService.PER_PAGE_DEFAULT;
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loadFinished();
                    }
                });
    }

}
