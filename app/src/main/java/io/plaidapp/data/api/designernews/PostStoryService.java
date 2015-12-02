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
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import io.plaidapp.BuildConfig;
import io.plaidapp.data.api.ClientAuthInterceptor;
import io.plaidapp.data.api.designernews.model.NewStoryRequest;
import io.plaidapp.data.api.designernews.model.StoriesResponse;
import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.data.prefs.DesignerNewsPrefs;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * An intent service which posts a new story to Designer News. Invokers can listen for results by
 * setting the {@link #EXTRA_BROADCAST_RESULT} flag as {@code true} in the launching intent and
 * then using a {@link LocalBroadcastManager} to listen for {@link #BROADCAST_ACTION_SUCCESS} and
 * {@link #BROADCAST_ACTION_FAILURE} broadcasts;
 */
public class PostStoryService extends IntentService {

    public static final String ACTION_POST_NEW_STORY = "ACTION_POST_NEW_STORY";
    public static final String EXTRA_STORY_TITLE = "EXTRA_STORY_TITLE";
    public static final String EXTRA_STORY_URL = "EXTRA_STORY_URL";
    public static final String EXTRA_STORY_COMMENT = "EXTRA_STORY_COMMENT";
    public static final String EXTRA_BROADCAST_RESULT = "EXTRA_BROADCAST_RESULT";
    public static final String EXTRA_NEW_STORY = "EXTRA_NEW_STORY";
    public static final String BROADCAST_ACTION_SUCCESS = "BROADCAST_ACTION_SUCCESS";
    public static final String BROADCAST_ACTION_FAILURE = "BROADCAST_ACTION_FAILURE";

    public PostStoryService() {
        super("PostStoryService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        if (ACTION_POST_NEW_STORY.equals(intent.getAction())) {
            final boolean broadcastResult = intent.getBooleanExtra(EXTRA_BROADCAST_RESULT, false);
            final DesignerNewsPrefs designerNewsPrefs = DesignerNewsPrefs.get(this);
            if (!designerNewsPrefs.isLoggedIn()) return; // shouldn't happen...

            String title = intent.getStringExtra(EXTRA_STORY_TITLE);
            String url = intent.getStringExtra(EXTRA_STORY_URL);
            String comment = intent.getStringExtra(EXTRA_STORY_COMMENT);
            if (TextUtils.isEmpty(title)) return;
            NewStoryRequest storyToPost = null;
            if (!TextUtils.isEmpty(url)) {
                storyToPost = NewStoryRequest.createWithUrl(title, url);
            } else if (!TextUtils.isEmpty(comment)) {
                storyToPost = NewStoryRequest.createWithComment(title, comment);
            }
            if (storyToPost == null) return;
            DesignerNewsService designerNewsApi = new RestAdapter.Builder()
                    .setEndpoint(DesignerNewsService.ENDPOINT)
                    .setRequestInterceptor(new ClientAuthInterceptor
                            (designerNewsPrefs.getAccessToken(),
                                    BuildConfig.DESIGNER_NEWS_CLIENT_ID))
                    .build()
                    .create(DesignerNewsService.class);
            designerNewsApi.postStory(storyToPost, new Callback<StoriesResponse>() {
                @Override
                public void success(StoriesResponse story, Response response) {
                    if (story != null && story.stories != null && story.stories.size() > 0) {
                        if (broadcastResult) {
                            Intent success = new Intent(BROADCAST_ACTION_SUCCESS);
                            // API doesn't fill in author details so add them here
                            Story returnedStory = story.stories.get(0);
                            Story.Builder builder = Story.Builder.from(returnedStory)
                                    .setUserId(designerNewsPrefs.getUserId())
                                    .setUserDisplayName(designerNewsPrefs.getUserName())
                                    .setUserPortraitUrl(designerNewsPrefs.getUserAvatar());
                            // API doesn't add a self URL, so potentially add one for consistency
                            if (TextUtils.isEmpty(returnedStory.url)) {
                                builder.setDefaultUrl(returnedStory.id);
                            }
                            Story newStory = builder.build();
                            newStory.dataSource = "SOURCE_NEW_DN_POST";
                            success.putExtra(EXTRA_NEW_STORY, newStory);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(success);
                        } else {
                            Toast.makeText(getApplicationContext(), "Story posted",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("PostStoryService", "Failed posting story", error);
                    if (broadcastResult) {
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(new Intent(BROADCAST_ACTION_FAILURE));
                    }
                }
            });
        }
    }

}
