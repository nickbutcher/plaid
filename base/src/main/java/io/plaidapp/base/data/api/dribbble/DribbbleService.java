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

package io.plaidapp.base.data.api.dribbble;

import io.plaidapp.base.data.api.dribbble.model.Shot;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Dribbble API - http://developer.dribbble.com/v1/
 */
public interface DribbbleService {

    String ENDPOINT = "https://api.dribbble.com/";
    String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss Z";

    @GET("v1/shots/{id}")
    Call<Shot> getShot(@Path("id") long shotId);

}
