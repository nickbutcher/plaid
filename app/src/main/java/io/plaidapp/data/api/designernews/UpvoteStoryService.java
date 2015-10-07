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

package io.plaidapp.data.api.designernews;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.ClientAuthInterceptor;
import io.plaidapp.data.api.designernews.model.StoryResponse;
import io.plaidapp.data.prefs.DesignerNewsPrefs;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UpvoteStoryService extends IntentService {

    public static final String ACTION_UPVOTE = "ACTION_UPVOTE";
    public static final String EXTRA_STORY_ID = "EXTRA_STORY_ID";

    public UpvoteStoryService() {
        super("UpvoteStoryService");
    }

    public static void startActionUpvote(Context context, long storyId) {
        Intent intent = new Intent(context, UpvoteStoryService.class);
        intent.setAction(ACTION_UPVOTE);
        intent.putExtra(EXTRA_STORY_ID, storyId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPVOTE.equals(action)) {
                handleActionUpvote(intent.getLongExtra(EXTRA_STORY_ID, 0l));
            }
        }
    }

    private void handleActionUpvote(long storyId) {
        if (storyId == 0l) return;
        DesignerNewsPrefs designerNewsPrefs = DesignerNewsPrefs.get(this);
        if (!designerNewsPrefs.isLoggedIn()) {
            // TODO prompt for login
            return;
        }
        DesignerNewsService designerNewsService = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .setRequestInterceptor(
                        new ClientAuthInterceptor(designerNewsPrefs.getAccessToken(),
                                BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                .build()
                .create(DesignerNewsService.class);
        designerNewsService.upvoteStory(storyId, "", new Callback<StoryResponse>() {
            @Override
            public void success(StoryResponse storyResponse, Response response) {
                int newVotesCount = storyResponse.story.vote_count;
                // TODO report success

            }

            @Override
            public void failure(RetrofitError error) {
                // TODO report failure
            }
        });
    }
}
