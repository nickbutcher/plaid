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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

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
import io.plaidapp.util.glide.GlideApp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * A screen displaying a player's details and their shots.
 */
public class PlayerActivity extends Activity {

    public static final String EXTRA_PLAYER = "EXTRA_PLAYER";
    public static final String EXTRA_PLAYER_NAME = "EXTRA_PLAYER_NAME";
    public static final String EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID";
    public static final String EXTRA_PLAYER_USERNAME = "EXTRA_PLAYER_USERNAME";

    User player;
    PlayerShotsDataManager dataManager;
    FeedAdapter adapter;
    GridLayoutManager layoutManager;
    Boolean following;
    private ElasticDragDismissFrameLayout.SystemChromeFader chromeFader;
    private int followerCount;

    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.container) ViewGroup container;
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
        chromeFader = new ElasticDragDismissFrameLayout.SystemChromeFader(this);

        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PLAYER)) {
            player = intent.getParcelableExtra(EXTRA_PLAYER);
            bindPlayer();
        } else if (intent.hasExtra(EXTRA_PLAYER_NAME)) {
            String name = intent.getStringExtra(EXTRA_PLAYER_NAME);
            playerName.setText(name);
            if (intent.hasExtra(EXTRA_PLAYER_ID)) {
                long userId = intent.getLongExtra(EXTRA_PLAYER_ID, 0L);
                loadPlayer(userId);
            } else if (intent.hasExtra(EXTRA_PLAYER_USERNAME)) {
                String username = intent.getStringExtra(EXTRA_PLAYER_USERNAME);
                loadPlayer(username);
            }
        } else if (intent.getData() != null) {
            // todo support url intents
        }

        // setup immersive mode i.e. draw behind the system chrome & adjust insets
        draggableFrame.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        draggableFrame.setOnApplyWindowInsetsListener((v, insets) -> {
            final ViewGroup.MarginLayoutParams lpFrame = (ViewGroup.MarginLayoutParams)
                    draggableFrame.getLayoutParams();
            lpFrame.leftMargin += insets.getSystemWindowInsetLeft();    // landscape
            lpFrame.rightMargin += insets.getSystemWindowInsetRight();  // landscape
            ((ViewGroup.MarginLayoutParams) avatar.getLayoutParams()).topMargin
                += insets.getSystemWindowInsetTop();
            ViewUtils.setPaddingTop(container, insets.getSystemWindowInsetTop());
            ViewUtils.setPaddingBottom(shots, insets.getSystemWindowInsetBottom());
            // clear this listener so insets aren't re-applied
            draggableFrame.setOnApplyWindowInsetsListener(null);
            return insets;
        });
        setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this));
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

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (data == null || resultCode != RESULT_OK
                || !data.hasExtra(DribbbleShot.RESULT_EXTRA_SHOT_ID)) return;

        // When reentering, if the shared element is no longer on screen (e.g. after an
        // orientation change) then scroll it into view.
        final long sharedShotId = data.getLongExtra(DribbbleShot.RESULT_EXTRA_SHOT_ID, -1L);
        if (sharedShotId != -1L                                             // returning from a shot
                && adapter.getDataItemCount() > 0                           // grid populated
                && shots.findViewHolderForItemId(sharedShotId) == null) {   // view not attached
            final int position = adapter.getItemPosition(sharedShotId);
            if (position == RecyclerView.NO_POSITION) return;

            // delay the transition until our shared element is on-screen i.e. has been laid out
            postponeEnterTransition();
            shots.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b,
                                           int oL, int oT, int oR, int oB) {
                    shots.removeOnLayoutChangeListener(this);
                    startPostponedEnterTransition();
                }
            });
            shots.scrollToPosition(position);
        }
    }

    void bindPlayer() {
        if (player == null) return;

        final Resources res = getResources();
        final NumberFormat nf = NumberFormat.getInstance();

        GlideApp.with(this)
                .load(player.getHighQualityAvatarUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .transition(withCrossFade())
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
                        ViewUtils.setPaddingTop(shots, likesCount.getBottom());
                    }
                    adapter.addAndResort(data);
                }
            }
        };
        ViewPreloadSizeProvider<Shot> shotPreloadSizeProvider = new ViewPreloadSizeProvider<>();
        adapter = new FeedAdapter(this, dataManager, columns, PocketUtils.isPocketInstalled(this),
                shotPreloadSizeProvider);
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
        RecyclerViewPreloader<Shot> shotPreloader =
                new RecyclerViewPreloader<>(this, adapter, shotPreloadSizeProvider, 4);
        shots.addOnScrollListener(shotPreloader);

        // forward on any clicks above the first item in the grid (i.e. in the paddingTop)
        // to 'pass through' to the view behind
        shots.setOnTouchListener((v, event) -> {
            final int firstVisible = layoutManager.findFirstVisibleItemPosition();
            if (firstVisible > 0) return false;

            // if no data loaded then pass through
            if (adapter.getDataItemCount() == 0) {
                return container.dispatchTouchEvent(event);
            }

            final RecyclerView.ViewHolder vh = shots.findViewHolderForAdapterPosition(0);
            if (vh == null) return false;
            final int firstTop = vh.itemView.getTop();
            if (event.getY() < firstTop) {
                 return container.dispatchTouchEvent(event);
            }
            return false;
        });

        // check if following
        if (dataManager.getDribbblePrefs().isLoggedIn()) {
            if (player.id == dataManager.getDribbblePrefs().getUserId()) {
                TransitionManager.beginDelayedTransition(container);
                follow.setVisibility(View.GONE);
                ViewUtils.setPaddingTop(shots, container.getHeight() - follow.getHeight()
                        - ((ViewGroup.MarginLayoutParams) follow.getLayoutParams()).bottomMargin);
            } else {
                final Call<Void> followingCall = dataManager.getDribbbleApi().following(player.id);
                followingCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        following = response.isSuccessful();
                        if (!following) return;
                        TransitionManager.beginDelayedTransition(container);
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

    @OnClick(R.id.follow)
    void follow() {
        if (DribbblePrefs.get(this).isLoggedIn()) {
            if (following != null && following) {
                final Call<Void> unfollowCall = dataManager.getDribbbleApi().unfollow(player.id);
                unfollowCall.enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) { }

                    @Override public void onFailure(Call<Void> call, Throwable t) { }
                });
                following = false;
                TransitionManager.beginDelayedTransition(container);
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
                TransitionManager.beginDelayedTransition(container);
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

    @OnClick({R.id.shot_count, R.id.followers_count, R.id.likes_count})
    void playerActionClick(TextView view) {
        ((AnimatedVectorDrawable) view.getCompoundDrawables()[1]).start();
        switch (view.getId()) {
            case R.id.followers_count:
                PlayerSheet.start(PlayerActivity.this, player);
                break;
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

}
