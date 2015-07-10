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

import io.plaidapp.data.api.dribbble.model.AccessToken;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Dribbble Auth API (a different endpoint)
 */
public interface DribbbleAuthService {

    public static final String ENDPOINT = "https://dribbble.com/";

    @POST("/oauth/token")
    public AccessToken getAccessToken(@Query("client_id") String client_id,
                                      @Query("client_secret") String client_secret,
                                      @Query("code") String code);

    @POST("/oauth/token")
    public void getAccessToken(@Query("client_id") String client_id,
                               @Query("client_secret") String client_secret,
                               @Query("code") String code,
                               @Body String unused, // can remove when retrofit releases this
                               // fix: https://github
                               // .com/square/retrofit/commit/19ac1e2c4551448184ad66c4a0ec172e2741c2ee
                               Callback<AccessToken> callback);

}
