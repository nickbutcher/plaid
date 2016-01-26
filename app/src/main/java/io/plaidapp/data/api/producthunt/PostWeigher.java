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

package io.plaidapp.data.api.producthunt;

import java.util.List;

import io.plaidapp.data.PlaidItemSorting;
import io.plaidapp.data.api.producthunt.model.Post;

/**
 * Utility class for applying weights to a group of {@link Post}s for sorting. Weighs posts relative
 * to the most upvoted & commented hunts in the group.
 */
public class PostWeigher implements PlaidItemSorting.PlaidItemGroupWeigher<Post> {

    @Override
    public void weigh(List<Post> posts) {
        float maxVotes = 0f;
        float maxComments = 0f;
        for (Post post : posts) {
            maxVotes = Math.max(maxVotes, post.votes_count);
            maxComments = Math.max(maxComments, post.comments_count);
        }
        for (Post post : posts) {
            float weight = 1f - ((((float) post.comments_count) / maxComments) +
                    ((float) post.votes_count / maxVotes)) / 2f;
            post.weight = post.page + weight;
        }
    }

}
