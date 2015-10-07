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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.plaidapp.data.api.dribbble.model.Comment;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Dribbble API - http://developer.dribbble.com/v1/
 */
public interface DribbbleService {

    String ENDPOINT = "https://api.dribbble.com/v1/";
    String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
    int PER_PAGE_MAX = 100;
    int PER_PAGE_DEFAULT = 30;


    /* Shots */

    @GET("/shots")
    void getPopular(@Query("page") Integer page,
                    @Query("per_page") Integer pageSize,
                    Callback<List<Shot>> callback);

    @GET("/shots?sort=recent")
    void getRecent(@Query("page") Integer page,
                   @Query("per_page") Integer pageSize,
                   Callback<List<Shot>> callback);

    @GET("/shots?list=debuts")
    void getDebuts(@Query("page") Integer page,
                   @Query("per_page") Integer pageSize,
                   Callback<List<Shot>> callback);

    @GET("/shots?list=animated")
    void getAnimated(@Query("page") Integer page,
                     @Query("per_page") Integer pageSize,
                     Callback<List<Shot>> callback);

    @GET("/shots")
    void getShots(@Query("list") @ShotType String shotType,
                  @Query("timeframe") @ShotTimeframe String timeframe,
                  @Query("sort") @ShotSort String shotSort,
                  Callback<List<Shot>> callback);

    @GET("/shots/{id}")
    Shot getShot(@Path("id") long shotId);

    @GET("/shots/{id}")
    void getShot(@Path("id") long shotId,
                 Callback<Shot> callback);

    @GET("/user/following/shots")
    void getFollowing(@Query("page") Integer page,
                      @Query("per_page") Integer pageSize,
                      Callback<List<Shot>> callback);

    /* List the authenticated user’s shot likes */
    @GET("/user/likes")
    void getUserLikes(@Query("page") Integer page,
                      @Query("per_page") Integer pageSize,
                      Callback<List<Like>> callback);

    /* List the authenticated user’s shots */
    @GET("/user/shots")
    void getUserShots(@Query("page") Integer page,
                      @Query("per_page") Integer pageSize,
                      Callback<List<Shot>> callback);

    /* Shot likes */

    @GET("/shots/{id}/likes")
    void getUserLikes(@Path("id") long shotId,
                      Callback<List<Like>> callback);

    @GET("/shots/{id}/like")
    void liked(@Path("id") long shotId,
               Callback<Like> callback);

    @POST("/shots/{id}/like")
    void like(@Path("id") long shotId,
              @Body String ignored,  // can remove when retrofit releases this fix:
              // https://github.com/square/retrofit/commit/19ac1e2c4551448184ad66c4a0ec172e2741c2ee
              Callback<Like> callback);

    @DELETE("/shots/{id}/like")
    void unlike(@Path("id") long shotId,
                Callback<Void> callback);


    /* Comments */

    @GET("/shots/{id}/comments")
    void getComments(@Path("id") long shotId,
                     @Query("page") Integer page,
                     @Query("per_page") Integer pageSize,
                     Callback<List<Comment>> callback);

    @GET("/shots/{shot}/comments/{id}/likes")
    void getCommentLikes(@Path("shot") long shotId,
                         @Path("id") long commentId,
                         Callback<List<Like>> callback);

    @POST("/shots/{shot}/comments")
    void postComment(@Path("shot") long shotId,
                     @Query("body") String body,
                     Callback<Comment> callback);


    @DELETE("/shots/{shot}/comments/{id}")
    void deleteComment(@Path("shot") long shotId,
                       @Path("id") long commentId,
                       Callback<Void> callback);

    @GET("/shots/{shot}/comments/{id}/like")
    void likedComment(@Path("shot") long shotId,
                      @Path("id") long commentId,
                      Callback<Like> callback);

    @POST("/shots/{shot}/comments/{id}/like")
    void likeComment(@Path("shot") long shotId,
                     @Path("id") long commentId,
                     @Body String ignored,  // can remove when retrofit releases this fix:
                     // https://github
                     // .com/square/retrofit/commit/19ac1e2c4551448184ad66c4a0ec172e2741c2ee
                     Callback<Like> callback);

    @DELETE("/shots/{shot}/comments/{id}/like")
    void unlikeComment(@Path("shot") long shotId,
                       @Path("id") long commentId,
                       Callback<Void> callback);


    /* Users */

    @GET("/users/{user}")
    User getUser(@Path("user") long userId);

    @GET("/users/{user}")
    void getUser(@Path("user") long userId, Callback<User> callback);

    @GET("/users/{user}")
    User getUser(@Path("user") String username);

    @GET("/users/{user}")
    void getUser(@Path("user") String username, Callback<User> callback);

    @GET("/user")
    User getAuthenticatedUser();

    @GET("/user")
    void getAuthenticatedUser(Callback<User> callback);


    /* Magic Constants */

    String SHOT_TYPE_ANIMATED = "animated";
    String SHOT_TYPE_ATTACHMENTS = "attachments";
    String SHOT_TYPE_DEBUTS = "debuts";
    String SHOT_TYPE_PLAYOFFS = "playoffs";
    String SHOT_TYPE_REBOUNDS = "rebounds";
    String SHOT_TYPE_TEAMS = "teams";
    String SHOT_TIMEFRAME_WEEK = "week";
    String SHOT_TIMEFRAME_MONTH = "month";
    String SHOT_TIMEFRAME_YEAR = "year";
    String SHOT_TIMEFRAME_EVER = "ever";
    String SHOT_SORT_COMMENTS = "comments";
    String SHOT_SORT_RECENT = "recent";
    String SHOT_SORT_VIEWS = "views";

    // Shot type
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SHOT_TYPE_ANIMATED,
            SHOT_TYPE_ATTACHMENTS,
            SHOT_TYPE_DEBUTS,
            SHOT_TYPE_PLAYOFFS,
            SHOT_TYPE_REBOUNDS,
            SHOT_TYPE_TEAMS
    })
    @interface ShotType {}

    // Shot timeframe
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SHOT_TIMEFRAME_WEEK,
            SHOT_TIMEFRAME_MONTH,
            SHOT_TIMEFRAME_YEAR,
            SHOT_TIMEFRAME_EVER
    })
    @interface ShotTimeframe {}

    // Short sort order
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SHOT_SORT_COMMENTS,
            SHOT_SORT_RECENT,
            SHOT_SORT_VIEWS
    })
    @interface ShotSort {}

}
