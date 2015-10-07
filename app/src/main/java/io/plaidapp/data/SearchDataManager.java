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
import android.os.AsyncTask;

import java.util.List;

import io.plaidapp.data.api.designernews.model.StoriesResponse;
import io.plaidapp.data.api.dribbble.DribbbleSearch;
import io.plaidapp.data.api.dribbble.model.Shot;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class SearchDataManager extends BaseDataManager implements DataLoadingSubject {

    // state
    private String query = "";
    private boolean loadingDribbble = false;
    private boolean loadingDesignerNews = false;
    private int page = 1;

    public SearchDataManager(Context context) {
        super(context);
    }

    @Override
    public boolean isDataLoading() {
        return loadingDribbble || loadingDesignerNews;
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
        query = "";
        page = 1;
        loadingDribbble = false;
        loadingDesignerNews = false;

    }

    public String getQuery() {
        return query;
    }

    private void searchDesignerNews(final String query, final int resultsPage) {
        loadingDesignerNews = true;
        getDesignerNewsApi().search(query, resultsPage, new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null) {
                    setPage(storiesResponse.stories, resultsPage);
                    setDataSource(storiesResponse.stories,
                            Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX + query);
                    onDataLoaded(storiesResponse.stories);
                }
                loadingDesignerNews = false;
            }

            @Override
            public void failure(RetrofitError error) {
                loadingDesignerNews = false;
            }
        });
    }

    private void searchDribbble(final String query, final int page) {
        loadingDribbble = true;
        new AsyncTask<Void, Void, List<Shot>>() {
            @Override
            protected List<Shot> doInBackground(Void... params) {
                return DribbbleSearch.search(query, DribbbleSearch.SORT_POPULAR, page);
            }

            @Override
            protected void onPostExecute(List<Shot> shots) {
                if (shots != null && shots.size() > 0) {
                    setPage(shots, page);
                    setDataSource(shots, "Dribbble Search");
                    onDataLoaded(shots);
                }
                loadingDribbble = false;
            }
        }.execute();
    }

}
