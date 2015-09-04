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

package com.example.android.plaid.data.api.designernews;

import com.example.android.plaid.BuildConfig;
import com.example.android.plaid.data.api.designernews.model.AccessToken;
import com.example.android.plaid.data.api.designernews.model.StoriesResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Modeling the Designer News API
 */
public interface DesignerNewsService {

    String ENDPOINT = "https://www.designernews.co/";

    String CLIENT_ID_QUERY = "?client_id=" + BuildConfig.DESIGNER_NEWS_CLIENT_ID;

    @GET("/api/v1/stories" + CLIENT_ID_QUERY)
    void getTopStories(@Query("page") Integer page,
                       Callback<StoriesResponse> callback);

    @GET("/api/v1/stories/recent" + CLIENT_ID_QUERY)
    void getRecentStories(@Query("page") Integer page,
                          Callback<StoriesResponse> callback);

    @GET("/api/v1/stories/search" + CLIENT_ID_QUERY)
    void search(@Query("query") String query, Callback<StoriesResponse> callback);

    @Headers({
            "grant_type: password",
            "client_id: " + BuildConfig.DESIGNER_NEWS_CLIENT_ID,
            "client_secret: " + BuildConfig.DESIGNER_NEWS_CLIENT_SECRET
    })
    @POST("/oauth/token")
    void login(@Header("username") String username,
               @Header("password") String password,
               @Body String ignored,  // can remove when retrofit releases this fix:
               // https://github.com/square/retrofit/commit/19ac1e2c4551448184ad66c4a0ec172e2741c2ee
               Callback<AccessToken> callback);

}
