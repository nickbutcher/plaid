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

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.data.api.dribbble.DribbbleSearchService;
import io.plaidapp.data.api.dribbble.model.Shot;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class SearchDataManager extends BaseDataManager<List<? extends PlaidItem>> {

    // state
    private String query = "";
    private int page = 1;
    private List<Call> inflight;

    public SearchDataManager(Context context) {
        super(context);
        inflight = new ArrayList<>();
    }

    public void searchFor(String query) {
        if (!this.query.equals(query)) {
            clear();
            this.query = query;
        } else {
            page++;
        }
        searchDribbble(query, page);
        searchDesignerNews(query, page);
    }

    public void loadMore() {
        searchFor(query);
    }

    public void clear() {
        cancelLoading();
        query = "";
        page = 1;
        resetLoadingCount();
    }

    @Override
    public void cancelLoading() {
        if (inflight.size() > 0) {
            for (Call call : inflight) {
                call.cancel();
            }
            inflight.clear();
        }
    }

    public String getQuery() {
        return query;
    }

    private void searchDesignerNews(final String query, final int resultsPage) {
        loadStarted();
        final Call<List<Story>> dnSearchCall = getDesignerNewsApi().search(query, resultsPage);
        dnSearchCall.enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful()) {
                    loadFinished();
                    List<Story> stories = response.body();
                    if (stories != null) {
                        setPage(stories, resultsPage);
                        setDataSource(stories,
                                Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX + query);
                        onDataLoaded(stories);
                    }
                    inflight.remove(dnSearchCall);
                } else {
                    failure(dnSearchCall);
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                failure(dnSearchCall);
            }
        });
        inflight.add(dnSearchCall);
    }

    private void searchDribbble(final String query, final int resultsPage) {
        loadStarted();
        final Call<List<Shot>> dribbbleSearchCall = getDribbbleSearchApi().search(
                query, resultsPage, DribbbleSearchService.PER_PAGE_DEFAULT,
                DribbbleSearchService.SORT_POPULAR);
        dribbbleSearchCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    loadFinished();
                    final List<Shot> shots = response.body();
                    if (shots != null) {
                        setPage(shots, resultsPage);
                        setDataSource(shots,
                                Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX + query);
                        onDataLoaded(shots);
                    }
                    inflight.remove(dribbbleSearchCall);
                } else {
                    failure(dribbbleSearchCall);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                failure(dribbbleSearchCall);
            }
        });
        inflight.add(dribbbleSearchCall);
    }

    private void failure(Call call) {
        loadFinished();
        inflight.remove(call);
    }

}
