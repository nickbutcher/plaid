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

package com.example.android.plaid.data;

import android.content.Context;
import android.os.AsyncTask;

import com.example.android.plaid.BuildConfig;
import com.example.android.plaid.data.api.AuthInterceptor;
import com.example.android.plaid.data.api.ClientAuthInterceptor;
import com.example.android.plaid.data.api.designernews.DesignerNewsService;
import com.example.android.plaid.data.api.designernews.model.StoriesResponse;
import com.example.android.plaid.data.api.dribbble.DribbbleSearch;
import com.example.android.plaid.data.api.dribbble.DribbbleService;
import com.example.android.plaid.data.api.dribbble.model.Shot;
import com.example.android.plaid.data.prefs.DesignerNewsPrefs;
import com.example.android.plaid.data.prefs.DribbblePrefs;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class SearchDataManager implements DataLoadingSubject {

    private DribbblePrefs dribbblePrefs;
    private DribbbleService dribbbleApi;
    private DesignerNewsPrefs designerNewsPrefs;
    private DesignerNewsService designerNewsApi;

    // state
    private String query = "";
    private boolean loadingDribbble = false;
    private boolean loadingDesignerNews = false;
    private int page = 1;

    public SearchDataManager(Context context) {
        // setup the API access objects
        createDesignerNewsApi(context);
        createDribbbleApi(context);
    }

    public abstract void onDataLoaded(List<? extends PlaidItem> data);

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
        designerNewsApi.search(query, resultsPage, new Callback<StoriesResponse>() {
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

    private static void setPage(List<? extends PlaidItem> items, int page) {
        for (PlaidItem item : items) {
            item.page = page;
        }
    }

    private static void setDataSource(List<? extends PlaidItem> items, String dataSource) {
        for (PlaidItem item : items) {
            item.dataSource = dataSource;
        }
    }

    private void createDesignerNewsApi(Context context) {
        designerNewsPrefs = new DesignerNewsPrefs(context);
        designerNewsApi = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .setRequestInterceptor(new ClientAuthInterceptor(designerNewsPrefs.getAccessToken(),
                        BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .build()
                .create(DesignerNewsService.class);
    }

    private void createDribbbleApi(Context context) {
        dribbblePrefs = new DribbblePrefs(context);
        dribbbleApi = new RestAdapter.Builder()
                .setEndpoint(DribbbleService.ENDPOINT)
                .setConverter(new GsonConverter(new GsonBuilder()
                        .setDateFormat(DribbbleService.DATE_FORMAT)
                        .create()))
                .setRequestInterceptor(new AuthInterceptor(dribbblePrefs.getAccessToken()))
                .build()
                .create((DribbbleService.class));
    }
}
