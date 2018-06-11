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

package io.plaidapp.ui.span;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import in.uncod.android.bypass.style.TouchableUrlSpan;
import io.plaidapp.util.Activities;
import io.plaidapp.util.ActivityHelper;

/**
 * A span for marking up a dribbble player
 */
public class PlayerSpan extends TouchableUrlSpan {

    private final String playerName;
    private final long playerId;
    private final String playerUsername;

    public PlayerSpan(String url,
                      String playerName,
                      long playerId,
                      @Nullable String playerUsername,
                      ColorStateList textColor,
                      int pressedBackgroundColor) {
        super(url, textColor, pressedBackgroundColor);
        this.playerName = playerName;
        this.playerId = playerId;
        this.playerUsername = playerUsername;
    }

    @Override
    public void onClick(View view) {
        Intent intent = ActivityHelper.intentTo(Activities.Player.INSTANCE);
        intent.putExtra(Activities.Player.EXTRA_PLAYER_NAME, playerName);
        if (playerId > 0L) {
            intent.putExtra(Activities.Player.EXTRA_PLAYER_ID, playerId);
        }
        if (!TextUtils.isEmpty(playerUsername)) {
            intent.putExtra(Activities.Player.EXTRA_PLAYER_USERNAME, playerUsername);
        }
        view.getContext().startActivity(intent);
    }
}
