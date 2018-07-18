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

package io.plaidapp.core.designernews.data.votes;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import io.plaidapp.core.designernews.data.stories.model.Story;
import io.plaidapp.core.designernews.DesignerNewsPrefs;
import retrofit2.Call;
import retrofit2.Response;

public class UpvoteStoryService extends IntentService {

    public static final String ACTION_UPVOTE = "ACTION_UPVOTE";
    public static final String EXTRA_STORY_ID = "EXTRA_STORY_ID";

    public UpvoteStoryService() {
        super("UpvoteStoryService");
    }

    public static void startActionUpvote(@NonNull Context context, long storyId) {
        final Intent intent = new Intent(context, UpvoteStoryService.class);
        intent.setAction(ACTION_UPVOTE);
        intent.putExtra(EXTRA_STORY_ID, storyId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPVOTE.equals(action)) {
                handleActionUpvote(intent.getLongExtra(EXTRA_STORY_ID, 0L));
            }
        }
    }

    private void handleActionUpvote(long storyId) {
        if (storyId == 0L) return;
        final DesignerNewsPrefs designerNewsPrefs = DesignerNewsPrefs.get(this);
        if (!designerNewsPrefs.isLoggedIn()) {
            // TODO prompt for login
            return;
        }

        final Call<Story> upvoteStoryCall = designerNewsPrefs.getApi().upvoteStory(storyId);
        try {
            final Response<Story> response = upvoteStoryCall.execute();
            // TODO report success
        } catch (Exception e) {
            // TODO report failure
        }
    }
}
