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

import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Models a like of a Dribbble shot.
 */
public class Like {

    public final long id;
    public final Date created_at;
    public final @Nullable User user; // some calls do not populate the user field
    public final @Nullable Shot shot; // some calls do not populate the shot field

    public Like(long id, Date created_at, User user, Shot shot) {
        this.id = id;
        this.created_at = created_at;
        this.user = user;
        this.shot = shot;
    }
}
