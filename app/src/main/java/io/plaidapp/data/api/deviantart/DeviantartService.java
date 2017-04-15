package io.plaidapp.data.api.deviantart;

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


import java.util.List;

import io.plaidapp.data.api.deviantart.model.Deviation;
import io.plaidapp.data.api.deviantart.model.Popular;
import io.plaidapp.data.api.deviantart.model.User;
import io.plaidapp.ui.DeviationActivity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Deviantart API - https://www.deviantart.com/developers/rss
 */
public interface DeviantartService {

    String ENDPOINT = "https://www.deviantart.com/api/";
    String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
    int PER_PAGE_MAX = 100;
    int PER_PAGE_DEFAULT = 12;



//    @GET("/browse/popular")
//    Call<List<UserFeed>> getPopularDeviations(@Query("access_token") String accessToken, @Query("limit") int limit, @Query("offset") int offset);

    @GET("v1/oauth2/user/whoami")
    Call<User> getAuthenticatedUser();

    @GET("v1/oauth2/browse/popular")
    Call<Popular> getPopularDeviations(@Query("category_path") String category_path,
                                             @Query("limit") int limit,
                                             @Query("offset") int offset);

    @GET("v1/oauth2/browse/dailydeviations")
    Call<List<Deviation>> getDailyDeviations();

//    @GET("v1/oauth2/gallery/all")
//    Call<Popular> getUserGallery(@Query("username") String username,
//                                       @Query("limit") int limit,
//                                       @Query("offset") int offset);


    @GET("v1/oauth2/deviation/{id}")
    Call<Deviation> getDeviation(@Path("id") long deviationId);

}
