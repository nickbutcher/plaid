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

package io.plaidapp.data.api.designernews.model;

/**
 * Models a Desinger News User
 */
public class User {

    public final long id;
    public final String first_name;
    public final String last_name;
    public final String display_name;
    public final String job;
    public final String portrait_url;
    public final String cover_photo_url;

    public User(long id,
                String first_name,
                String last_name,
                String display_name,
                String job,
                String portrait_url,
                String cover_photo_url) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.display_name = display_name;
        this.job = job;
        this.portrait_url = portrait_url;
        this.cover_photo_url = cover_photo_url;
    }
}
