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

package com.example.android.plaid.data.api.hackernews;

import com.example.android.plaid.data.api.hackernews.model.Posts;

import retrofit.http.GET;

/**
 * Created by nickbutcher on 7/21/14.
 */
public interface HackerNewsService {

    // TODO update to the new officail API https://github.com/HackerNews/API

    public static final String ENDPOINT = "http://api.ihackernews.com/";

    public static final String STORY_URL = "https://news.ycombinator.com/item?id=";

    // https://hacker-news.firebaseio.com/v0/topstories

    @GET("/page")
    Posts getTopNews();
}
