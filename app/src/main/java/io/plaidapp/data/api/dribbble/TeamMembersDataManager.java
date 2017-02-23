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
import io.plaidapp.data.api.dribbble.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Loads the members of a given dribbble team.
 */
public abstract class TeamMembersDataManager extends PaginatedDataManager<List<User>> {

    private final String teamName;
    private Call<List<User>> teamMembersCall;

    public TeamMembersDataManager(Context context, String teamName) {
        super(context);
        this.teamName = teamName;
    }

    @Override
    public void cancelLoading() {
        if (teamMembersCall != null) teamMembersCall.cancel();
    }

    @Override
    protected void loadData(int page) {
        teamMembersCall = getDribbbleApi()
                .getTeamMembers(teamName, page, DribbbleService.PER_PAGE_DEFAULT);
        teamMembersCall.enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    loadFinished();
                    final List<User> teamMembers = response.body();
                    moreDataAvailable = teamMembers.size() == DribbbleService.PER_PAGE_DEFAULT;
                    onDataLoaded(teamMembers);
                    teamMembersCall = null;
                } else {
                    failure();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                failure();
            }

            private void failure() {
                loadFinished();
                moreDataAvailable = false;
                teamMembersCall = null;
            }
        });
    }
}
