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

import io.plaidapp.core.data.PlaidItemSorting;
import io.plaidapp.core.producthunt.data.api.model.Post;

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
            maxVotes = Math.max(maxVotes, post.getVotesCount());
            maxComments = Math.max(maxComments, post.getCommentsCount());
        }
        for (Post post : posts) {
            float weight = 1f - ((((float) post.getCommentsCount()) / maxComments) +
                    ((float) post.getVotesCount() / maxVotes)) / 2f;
            post.setWeight(post.getPage() + weight);
        }
    }

}
