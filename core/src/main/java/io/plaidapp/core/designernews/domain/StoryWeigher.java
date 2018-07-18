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

package io.plaidapp.core.designernews.domain;

import java.util.List;

import io.plaidapp.core.data.PlaidItemSorting;
import io.plaidapp.core.designernews.data.stories.model.Story;

/**
 * Utility class for applying weights to a group of {@link Story}s for sorting. Weighs stories
 * relative to the most upvoted & commented stories in the group.
 */
public class StoryWeigher implements PlaidItemSorting.PlaidItemGroupWeigher<Story> {

    @Override
    public void weigh(List<Story> stories) {
        float maxVotes = 0f;
        float maxComments = 0f;
        for (Story story : stories) {
            maxVotes = Math.max(maxVotes, story.getVoteCount());
            maxComments = Math.max(maxComments, story.getCommentCount());
        }
        for (Story story : stories) {
            float weight = 1f - ((((float) story.getCommentCount()) / maxComments) +
                    ((float) story.getVoteCount() / maxVotes)) / 2f;
            story.setWeight(story.getPage() + weight);
        }
    }

}
