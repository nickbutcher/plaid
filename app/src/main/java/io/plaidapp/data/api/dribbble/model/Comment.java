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

package io.plaidapp.data.api.dribbble.model;

import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.Date;

import io.plaidapp.util.HtmlUtils;

/**
 * Models a commend on a Dribbble shot.
 */
public class Comment {

    public final long id;
    public final String body;
    public final String likes_url;
    public final Date created_at;
    public final Date updated_at;
    public final User user;
    public long likes_count;
    // todo move this into a decorator
    public Boolean liked;
    public Spanned parsedBody;

    public Comment(long id,
                   String body,
                   long likes_count,
                   String likes_url,
                   Date created_at,
                   Date updated_at,
                   User user) {
        this.id = id;
        this.body = body;
        this.likes_count = likes_count;
        this.likes_url = likes_url;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.user = user;
    }

    public Spanned getParsedBody(TextView textView) {
        if (parsedBody == null && !TextUtils.isEmpty(body)) {
            parsedBody = HtmlUtils.parseHtml(body, textView.getLinkTextColors(), textView
                    .getHighlightColor());
        }
        return parsedBody;
    }

    @Override
    public String toString() {
        return body;
    }
}
