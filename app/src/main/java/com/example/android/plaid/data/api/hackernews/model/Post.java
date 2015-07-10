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

package com.example.android.plaid.data.api.hackernews.model;

import com.example.android.plaid.data.PlaidItem;

/**
 * Created by nickbutcher on 7/21/14.
 */
public class Post extends PlaidItem {

    public final int commentCount;
    public final int points;
    public final String postedAgo;
    public final String postedBy;

    public Post(Long id,
                String title,
                String url,
                int commentCount,
                int points,
                String postedAgo,
                String postedBy) {
        super(id, title, url);
        this.commentCount = commentCount;
        this.points = points;
        this.postedAgo = postedAgo;
        this.postedBy = postedBy;
    }
}
