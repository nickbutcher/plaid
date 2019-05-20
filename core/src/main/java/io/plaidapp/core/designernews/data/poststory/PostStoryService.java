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

package io.plaidapp.core.designernews.data.poststory;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;
import io.plaidapp.core.designernews.data.api.DesignerNewsService;
import io.plaidapp.core.designernews.data.login.LoginRepository;
import io.plaidapp.core.designernews.data.login.model.LoggedInUser;
import io.plaidapp.core.designernews.data.poststory.model.NewStoryRequest;
import io.plaidapp.core.designernews.data.stories.model.Story;
import io.plaidapp.core.designernews.data.stories.model.StoryKt;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;
import java.util.List;

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
    public static final String BROADCAST_ACTION_FAILURE_REASON = "BROADCAST_ACTION_FAILURE_REASON";
    public static final String SOURCE_NEW_DN_POST = "SOURCE_NEW_DN_POST";

    public PostStoryService() {
        super("PostStoryService");
    }

    @Inject DesignerNewsService service;
    @Inject LoginRepository repository;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        if (ACTION_POST_NEW_STORY.equals(intent.getAction())) {
            final boolean broadcastResult = intent.getBooleanExtra(EXTRA_BROADCAST_RESULT, false);

            if (!repository.isLoggedIn()) return; // shouldn't happen...

            final String title = intent.getStringExtra(EXTRA_STORY_TITLE);
            final String url = intent.getStringExtra(EXTRA_STORY_URL);
            final String comment = intent.getStringExtra(EXTRA_STORY_COMMENT);
            if (TextUtils.isEmpty(title)) return;

            NewStoryRequest storyToPost = getNewStoryRequest(title, url, comment);
            if (storyToPost == null) return;

            postStory(broadcastResult, service, repository.getUser(), storyToPost);
        }
    }

    @Nullable
    private NewStoryRequest getNewStoryRequest(String title, String url, String comment) {
        NewStoryRequest storyToPost = null;
        if (!TextUtils.isEmpty(url)) {
            storyToPost = NewStoryRequest.Companion.createWithUrl(title, url);
        } else if (!TextUtils.isEmpty(comment)) {
            storyToPost = NewStoryRequest.Companion.createWithComment(title, comment);
        }
        return storyToPost;
    }

    private void postStory(
            boolean broadcastResult,
            DesignerNewsService service,
            LoggedInUser user,
            NewStoryRequest storyToPost) {
        final Call<List<Story>> postStoryCall =
                service.postStory(storyToPost);
        try {
            final Response<List<Story>> response = postStoryCall.execute();
            final List<Story> stories = response.body();
            if (stories != null && !stories.isEmpty()) {
                if (broadcastResult) {
                    broadcastSuccess(stories, user);
                } else {
                    Toast.makeText(getApplicationContext(), "Story posted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            final String reason = e.getMessage();
            if (broadcastResult) {
                broadcastFailure(reason);
            } else {
                Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void broadcastSuccess(List<Story> stories, LoggedInUser user) {
        final Intent success = new Intent(BROADCAST_ACTION_SUCCESS);
        // API doesn't fill in author details so add them here
        final Story newStory = getStory(stories, user);
        success.putExtra(EXTRA_NEW_STORY, newStory.getId());
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(success);
    }

    private void broadcastFailure(String reason) {
        final Intent failure = new Intent(BROADCAST_ACTION_FAILURE);
        failure.putExtra(BROADCAST_ACTION_FAILURE_REASON, reason);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(failure);
    }

    @NonNull
    private Story getStory(List<Story> stories, LoggedInUser user) {
        final Story returnedStory = stories.get(0);
        // API doesn't add a self URL, so potentially add one for consistency
        String defaultUrl = TextUtils.isEmpty(returnedStory.getUrl()) ?
                StoryKt.getDefaultUrl(returnedStory.getId()) :
                returnedStory.getUrl();

        final Story newStory = new Story(returnedStory.getId(),
                returnedStory.getTitle(),
                defaultUrl,
                returnedStory.getComment(),
                returnedStory.getCommentHtml(),
                returnedStory.getCommentCount(),
                returnedStory.getVoteCount(),
                user.getId(),
                returnedStory.getCreatedAt(),
                returnedStory.getLinks(),
                user.getDisplayName(),
                user.getPortraitUrl(),
                returnedStory.getUserJob()
        );
        newStory.setDataSource(SOURCE_NEW_DN_POST);
        return newStory;
    }
}
