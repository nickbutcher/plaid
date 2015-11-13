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

package io.plaidapp.data.api.designernews;

import java.util.Map;

import io.plaidapp.data.api.designernews.model.AccessToken;
import io.plaidapp.data.api.designernews.model.Comment;
import io.plaidapp.data.api.designernews.model.NewStoryRequest;
import io.plaidapp.data.api.designernews.model.StoriesResponse;
import io.plaidapp.data.api.designernews.model.StoryResponse;
import io.plaidapp.data.api.designernews.model.UserResponse;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Models the Designer News API.
 *
 * <p/>
 * v1 docs: https://github.com/layervault/dn_api
 * v2 docs: https://github.com/DesignerNews/dn_api_v2
 */
public interface DesignerNewsService {

    String ENDPOINT = "https://www.designernews.co/";

    @GET("api/v1/stories")
    Call<StoriesResponse> getTopStories(@Query("page") Integer page);

    @GET("api/v1/stories/recent")
    Call<StoriesResponse> getRecentStories(@Query("page") Integer page);

    @GET("api/v1/stories/search")
    Call<StoriesResponse> search(@Query("query") String query,
                                 @Query("page") Integer page);

    @FormUrlEncoded
    @POST("oauth/token")
    Call<AccessToken> login(@FieldMap() Map loginParams);

    @GET("api/v1/me")
    Call<UserResponse> getAuthedUser();

    @POST("api/v1/stories/{id}/upvote")
    Call<StoryResponse> upvoteStory(@Path("id") long storyId);


    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/stories")
    Call<StoriesResponse> postStory(@Body NewStoryRequest story);

    @FormUrlEncoded
    @POST("/api/v1/stories/{id}/reply")
    void comment(@Path("id") long storyId,
                 @Field("comment[body]") String comment,
                 Callback<Comment> callback);

}
