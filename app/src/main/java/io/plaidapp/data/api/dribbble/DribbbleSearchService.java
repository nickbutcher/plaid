/*
 * Copyright 2016 Google Inc.
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

import io.plaidapp.data.api.dribbble.model.Shot;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *  Fake-API for searching dribbble
 */
public interface DribbbleSearchService {

    String ENDPOINT = "https://dribbble.com/";
    String SORT_POPULAR = "";
    String SORT_RECENT = "latest";
    int PER_PAGE_DEFAULT = 12;

    @GET("search")
    Call<List<Shot>> search(@Query("q") String query,
                            @Query("page") Integer page,
                            @Query("per_page") Integer pageSize,
                            @Query("s") @SortOrder String sort);


    /** magic constants **/

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SORT_POPULAR,
            SORT_RECENT
    })
    @interface SortOrder { }

}
