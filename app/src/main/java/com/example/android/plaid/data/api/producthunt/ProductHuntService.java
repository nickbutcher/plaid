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

package com.example.android.plaid.data.api.producthunt;

import com.example.android.plaid.data.api.producthunt.model.PostsResponse;

import retrofit.Callback;
import retrofit.http.GET;

public interface ProductHuntService {

    String ENDPOINT = "https://api.producthunt.com/v1/";

    @GET("/posts")
    void getPosts(Callback<PostsResponse> callback);
}
