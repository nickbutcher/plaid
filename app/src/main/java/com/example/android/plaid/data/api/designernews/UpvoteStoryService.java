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

package com.example.android.plaid.data.api.designernews;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class UpvoteStoryService extends IntentService {

    public static final String ACTION_UPVOTE =
            "com.example.android.plaid.data.api.designernews.action.UPVOTE";

    public UpvoteStoryService() {
        super("UpvoteStoryService");
    }

    public static void startActionUpvote(Context context) {
        Intent intent = new Intent(context, UpvoteStoryService.class);
        intent.setAction(ACTION_UPVOTE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPVOTE.equals(action)) {
                handleActionUpvote();
            }
        }
    }

    private void handleActionUpvote() {
        // TODO actually upvote it
        Toast.makeText(getApplicationContext(), "Story upvoted", Toast.LENGTH_SHORT).show();
    }
}
