/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.producthunt.data.api;

import java.util.List;

import io.plaidapp.core.data.api.EnvelopePayload;
import io.plaidapp.core.producthunt.data.api.model.Post;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Models the Product Hunt API. See https://api.producthunt.com/v1/docs
 */
public interface ProductHuntService {

    String ENDPOINT = "https://api.producthunt.com/";

    @EnvelopePayload("posts")
    @GET("v1/posts")
    Call<List<Post>> getPosts(@Query("days_ago") Integer page);
}
