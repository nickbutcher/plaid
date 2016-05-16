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

package io.plaidapp.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.plaidapp.R;
import io.plaidapp.data.api.dribbble.PlayerShotsDataManager;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import io.plaidapp.data.pocket.PocketUtils;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.ui.recyclerview.InfiniteScrollListener;
import io.plaidapp.ui.recyclerview.SlideInItemAnimator;
import io.plaidapp.ui.transitions.MorphTransform;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.util.DribbbleUtils;
import io.plaidapp.util.ViewUtils;
import io.plaidapp.util.glide.CircleTransform;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A screen displaying a player's details and their shots.
 */
public class PlayerActivity extends Activity {

    public static final String EXTRA_PLAYER = "EXTRA_PLAYER";
    public static final String EXTRA_PLAYER_NAME = "EXTRA_PLAYER_NAME";
    public static final String EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID";
    public static final String EXTRA_PLAYER_USERNAME = "EXTRA_PLAYER_USERNAME";

    private User player;
    private CircleTransform circleTransform;
    private PlayerShotsDataManager dataManager;
    private FeedAdapter adapter;
    private GridLayoutManager layoutManager;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private Boolean following;
    private int followerCount;

    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.player_description) ViewGroup playerDescription;
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.player_name) TextView playerName;
    @BindView(R.id.follow) Button follow;
    @BindView(R.id.player_bio) TextView bio;
    @BindView(R.id.shot_count) TextView shotCount;
    @BindView(R.id.followers_count) TextView followersCount;
    @BindView(R.id.likes_count) TextView likesCount;
    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.player_shots) RecyclerView shots;
    @BindInt(R.integer.num_columns) int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_player);
        ButterKnife.bind(this);
        circleTransform = new CircleTransform(this);
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PLAYER)) {
            player = intent.getParcelableExtra(EXTRA_PLAYER);
            bindPlayer();
        } else if (intent.hasExtra(EXTRA_PLAYER_NAME)) {
            String name = intent.getStringExtra(EXTRA_PLAYER_NAME);
            playerName.setText(name);
            if (intent.hasExtra(EXTRA_PLAYER_ID)) {
                long userId = intent.getLongExtra(EXTRA_PLAYER_ID, 0l);
                loadPlayer(userId);
            } else if (intent.hasExtra(EXTRA_PLAYER_USERNAME)) {
                String username = intent.getStringExtra(EXTRA_PLAYER_USERNAME);
                loadPlayer(username);
            }
        } else if (intent.getData() != null) {
            // todo support url intents
        }

        // setup immersive mode i.e. draw behind the system chrome & adjust insets
        draggableFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        draggableFrame.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                ((ViewGroup.MarginLayoutParams) draggableFrame.getLayoutParams()).rightMargin
                        += insets.getSystemWindowInsetRight(); // landscape
                ((ViewGroup.MarginLayoutParams) avatar.getLayoutParams()).topMargin
                    += insets.getSystemWindowInsetTop();
                ViewUtils.setPaddingTop(playerDescription, insets.getSystemWindowInsetTop());
                ViewUtils.setPaddingBottom(shots, insets.getSystemWindowInsetBottom());
                // clear this listener so insets aren't re-applied
                draggableFrame.setOnApplyWindowInsetsListener(null);
                return insets;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        draggableFrame.addListener(chromeFader);
    }

    @Override
    protected void onPause() {
        draggableFrame.removeListener(chromeFader);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (dataManager != null) {
            dataManager.cancelLoading();
        }
        super.onDestroy();
    }

    private void bindPlayer() {
        if (player == null) {
            return;
        }

        final Resources res = getResources();
        final NumberFormat nf = NumberFormat.getInstance();

        Glide.with(this)
                .load(player.getHighQualityAvatarUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .transform(circleTransform)
                .into(avatar);
        playerName.setText(player.name.toLowerCase());
        if (!TextUtils.isEmpty(player.bio)) {
            DribbbleUtils.parseAndSetText(bio, player.bio);
        } else {
            bio.setVisibility(View.GONE);
        }

        shotCount.setText(res.getQuantityString(R.plurals.shots, player.shots_count,
                nf.format(player.shots_count)));
        if (player.shots_count == 0) {
            shotCount.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, getDrawable(R.drawable.avd_no_shots), null, null);
        }
        setFollowerCount(player.followers_count);
        likesCount.setText(res.getQuantityString(R.plurals.likes, player.likes_count,
                nf.format(player.likes_count)));

        // load the users shots
        dataManager = new PlayerShotsDataManager(this, player) {
            @Override
            public void onDataLoaded(List<Shot> data) {
                if (data != null && data.size() > 0) {
                    if (adapter.getDataItemCount() == 0) {
                        loading.setVisibility(View.GONE);
                        ViewUtils.setPaddingTop(shots, playerDescription.getHeight());
                    }
                    adapter.addAndResort(data);
                }
            }
        };
        adapter = new FeedAdapter(this, dataManager, columns, PocketUtils.isPocketInstalled(this));
        shots.setAdapter(adapter);
        shots.setItemAnimator(new SlideInItemAnimator());
        shots.setVisibility(View.VISIBLE);
        layoutManager = new GridLayoutManager(this, columns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemColumnSpan(position);
            }
        });
        shots.setLayoutManager(layoutManager);
        shots.addOnScrollListener(new InfiniteScrollListener(layoutManager, dataManager) {
            @Override
            public void onLoadMore() {
                dataManager.loadData();
            }
        });
        shots.setHasFixedSize(true);

        // forward on any clicks above the first item in the grid (i.e. in the paddingTop)
        // to 'pass through' to the view behind
        shots.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible > 0) return false;

                // if no data loaded then pass through
                if (adapter.getDataItemCount() == 0) {
                    return playerDescription.dispatchTouchEvent(event);
                }

                final RecyclerView.ViewHolder vh = shots.findViewHolderForAdapterPosition(0);
                if (vh == null) return false;
                final int firstTop = vh.itemView.getTop();
                if (event.getY() < firstTop) {
                     return playerDescription.dispatchTouchEvent(event);
                }
                return false;
            }
        });

        // check if following
        if (dataManager.getDribbblePrefs().isLoggedIn()) {
            if (player.id == dataManager.getDribbblePrefs().getUserId()) {
                TransitionManager.beginDelayedTransition(playerDescription);
                follow.setVisibility(View.GONE);
                ViewUtils.setPaddingTop(shots, playerDescription.getHeight() - follow.getHeight()
                        - ((ViewGroup.MarginLayoutParams) follow.getLayoutParams()).bottomMargin);
            } else {
                final Call<Void> followingCall = dataManager.getDribbbleApi().following(player.id);
                followingCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        following = response.isSuccessful();
                        if (!following) return;
                        TransitionManager.beginDelayedTransition(playerDescription);
                        follow.setText(R.string.following);
                        follow.setActivated(true);
                    }

                    @Override public void onFailure(Call<Void> call, Throwable t) { }
                });
            }
        }

        if (player.shots_count > 0) {
            dataManager.loadData(); // kick off initial load
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    private void loadPlayer(long userId) {
        final Call<User> userCall = DribbblePrefs.get(this).getApi().getUser(userId);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                player = response.body();
                bindPlayer();
            }

            @Override public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    private void loadPlayer(String username) {
        final Call<User> userCall = DribbblePrefs.get(this).getApi().getUser(username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                player = response.body();
                bindPlayer();
            }

            @Override public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    private void setFollowerCount(int count) {
        followerCount = count;
        followersCount.setText(getResources().getQuantityString(R.plurals.follower_count,
                followerCount, NumberFormat.getInstance().format(followerCount)));
        if (followerCount == 0) {
            followersCount.setBackground(null);
        }
    }

    @OnClick(R.id.follow)
    /* package */ void follow() {
        if (DribbblePrefs.get(this).isLoggedIn()) {
            if (following != null && following) {
                final Call<Void> unfollowCall = dataManager.getDribbbleApi().unfollow(player.id);
                unfollowCall.enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) { }

                    @Override public void onFailure(Call<Void> call, Throwable t) { }
                });
                following = false;
                TransitionManager.beginDelayedTransition(playerDescription);
                follow.setText(R.string.follow);
                follow.setActivated(false);
                setFollowerCount(followerCount - 1);
            } else {
                final Call<Void> followCall = dataManager.getDribbbleApi().follow(player.id);
                followCall.enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) { }

                    @Override public void onFailure(Call<Void> call, Throwable t) { }
                });
                following = true;
                TransitionManager.beginDelayedTransition(playerDescription);
                follow.setText(R.string.following);
                follow.setActivated(true);
                setFollowerCount(followerCount + 1);
            }
        } else {
            Intent login = new Intent(this, DribbbleLogin.class);
            MorphTransform.addExtras(login,
                    ContextCompat.getColor(this, R.color.dribbble),
                    getResources().getDimensionPixelSize(R.dimen.dialog_corners));
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (this, follow, getString(R.string.transition_dribbble_login));
            startActivity(login, options.toBundle());
        }
    }

    @OnClick({R.id.shot_count, R.id.followers_count, R.id.likes_count })
    /* package */ void playerActionClick(TextView view) {
        ((AnimatedVectorDrawable) view.getCompoundDrawables()[1]).start();
        switch (view.getId()) {
            case R.id.followers_count:
                PlayerSheet.start(PlayerActivity.this, player);
                break;
        }
    }

}
