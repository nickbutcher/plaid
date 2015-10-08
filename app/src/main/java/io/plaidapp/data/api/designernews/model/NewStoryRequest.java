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
 * Models the editable attributes usable when posting a new story. New stories can have either a
 * comment or a URL, not both.
 */
public class NewStoryRequest {

    public static NewStoryRequest createWithUrl(String title, String url) {
        return new NewStoryRequest(new NewStory(title, url, null));
    }

    public static NewStoryRequest createWithComment(String title, String comment) {
        return new NewStoryRequest(new NewStory(title, null, comment));
    }

    public final NewStory stories;

    private static class NewStory {

        public final String title;
        public final String url;
        public final String comment;

        private NewStory(String title, String url, String comment) {
            this.title = title;
            this.url = url;
            this.comment = comment;
        }
    }

    private NewStoryRequest(NewStory story) {
        this.stories = story;
    }

}
