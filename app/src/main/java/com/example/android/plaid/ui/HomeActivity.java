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

package com.example.android.plaid.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.ArcMotion;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.android.plaid.R;
import com.example.android.plaid.data.PlaidItem;
import com.example.android.plaid.data.Source;
import com.example.android.plaid.data.api.AuthInterceptor;
import com.example.android.plaid.data.api.designernews.DesignerNewsService;
import com.example.android.plaid.data.api.designernews.model.StoriesResponse;
import com.example.android.plaid.data.api.dribbble.DribbbleSearch;
import com.example.android.plaid.data.api.dribbble.DribbbleService;
import com.example.android.plaid.data.api.dribbble.model.Shot;
import com.example.android.plaid.data.api.hackernews.HackerNewsService;
import com.example.android.plaid.data.api.hackernews.model.Post;
import com.example.android.plaid.data.api.hackernews.model.Posts;
import com.example.android.plaid.data.api.producthunt.ProductHuntService;
import com.example.android.plaid.data.api.producthunt.model.PostsResponse;
import com.example.android.plaid.data.pocket.PocketUtils;
import com.example.android.plaid.data.prefs.DesignerNewsPrefs;
import com.example.android.plaid.data.prefs.DribbblePrefs;
import com.example.android.plaid.data.prefs.ProductHuntPrefs;
import com.example.android.plaid.data.prefs.SourceManager;
import com.example.android.plaid.ui.recyclerview.FilterTouchHelperCallback;
import com.example.android.plaid.ui.util.AnimUtils;
import com.example.android.plaid.ui.util.ColorUtils;
import com.example.android.plaid.ui.util.ImageUtils;
import com.example.android.plaid.ui.util.ImeUtils;
import com.example.android.plaid.ui.util.ViewUtils;
import com.example.android.plaid.ui.widget.DismissibleViewCallback;
import com.example.android.plaid.ui.widget.ElasticDragDismissFrameLayout;
import com.example.android.plaid.ui.widget.ImmersiveGridView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;


public class HomeActivity extends Activity {

    private static final int RC_SEARCH = 0;
    private static final int ANIMATION_DURATION_NEAR_INSTANT = 100; //ms
    private static final int ANIMATION_DURATION_SHORT = 300; // ms
    private static final int ANIMATION_DURATION_MED = 450; // ms
    private static final int ANIMATION_DURATION_SLOOOOW = 1500; // ms
    @Bind(R.id.stories_grid) ImmersiveGridView grid;
    //@Bind(R.id.new_story_post) View newPost;
    @Bind(R.id.fab) ImageButton fab;
    @Bind(R.id.scrim) View scrim;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.new_story_post) Button submitNewPost;
    @Bind(R.id.drawer) DrawerLayout drawer;
    @Bind(R.id.filters) RecyclerView filtersList;
    @Bind(R.id.filters_toolbar) Toolbar filtersToolbar;
    //@Bind(R.id.draggable_frame) DragDownDismissFrameLayout draggableFrame;
    @Bind(R.id.draggable_frame) ElasticDragDismissFrameLayout newPost;
    @Bind(R.id.new_story_container) View newPostContainer;
    @Bind(R.id.new_story_title) EditText newStoryTitle;
    @Bind(R.id.new_story_url) EditText newStoryUrl;
    MenuItem searchMenuItem;
    private FeedAdapter adapter;
    private FilterAdapter filtersAdapter;
    private DribbblePrefs dribbblePrefs;
    private DribbbleService dribbbleApi;
    private DesignerNewsPrefs designerNewsPrefs;
    private DesignerNewsService designerNewsApi;
    private ProductHuntService productHuntApi;

    private Toolbar.OnMenuItemClickListener mFiltersMenuClick = new Toolbar
            .OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_filter_add:
                    // TODO
                    return true;
                case R.id.menu_filter_edit:
                    // TODO
                    return true;
            }
            return false;
        }
    };

    private FilterAdapter.FiltersChangedListener filtersChangedListener = new FilterAdapter
            .FiltersChangedListener() {
        @Override
        public void onFiltersChanged(Source changedFilter) {

            if (changedFilter != null && changedFilter.active) {
                loadSource(changedFilter);
            } else {
                // have deactivated a source but currently no way to remove just those items so
                // for now just clear and re-query everything. TODO be smarter about this
                adapter.clear();
                for (Source filter : filtersAdapter.getFilters()) {
                    loadSource(filter);
                }
            }
        }
    };

    private AbsListView.OnScrollListener gridScroll = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int
                totalItemCount) {
            if (grid.getChildAt(0) != null) {
                int scrolled = toolbar.getBottom() - grid.getChildAt(0).getTop();
                if (scrolled > 0 && toolbar.getTranslationZ() != -1f) {
                    toolbar.setTranslationZ(-1f);
                } else if (scrolled == 0 && toolbar.getTranslationZ() != 0) {
                    toolbar.setTranslationZ(0f);
                }
            }
        }
    };

    private View.OnClickListener fabClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // translate the new post view so that it is centered on the FAB
            int fabCenterX = (fab.getLeft() + fab.getRight()) / 2;
            int fabCenterY = ((fab.getTop() + fab.getBottom()) / 2) - newPost.getTop();
            int translateX = fabCenterX - (newPost.getWidth() / 2);
            int translateY = fabCenterY - (newPost.getHeight() / 2);
            newPost.setTranslationX(translateX);
            newPost.setTranslationY(translateY);

            // then reveal the new post view, starting from the center & same dimens as fab
            newPost.setVisibility(View.VISIBLE);
            Animator reveal = ViewAnimationUtils.createCircularReveal(
                    newPost,
                    newPost.getWidth() / 2,
                    newPost.getHeight() / 2,
                    fab.getWidth() / 2,
                    (int) Math.hypot(newPost.getWidth() / 2, newPost.getHeight() / 2))
                    .setDuration(ANIMATION_DURATION_SHORT);

            // translate the new post view back into position along an arc
            Path motionPath = new ArcMotion().getPath(translateX, translateY, 0, 0);
            Animator position = ObjectAnimator.ofFloat(newPost, View.TRANSLATION_X, View
                    .TRANSLATION_Y, motionPath)
                    .setDuration(ANIMATION_DURATION_SHORT);

            // animate from the FAB colour to our dialog colour
            Animator background = ObjectAnimator.ofArgb(newPostContainer,
                    "backgroundColor",
                    ContextCompat.getColor(HomeActivity.this, R.color.accent),
                    //getResources().ContextCompat.getColor(this, R.color.designer_news))
                    ContextCompat.getColor(HomeActivity.this, R.color.background_light))
                    .setDuration(ANIMATION_DURATION_MED);

            // rise up in Z
            Animator raise = ObjectAnimator.ofFloat(newPost, View.TRANSLATION_Z, getResources()
                    .getDimensionPixelSize(R.dimen.spacing_micro))
                    .setDuration(ANIMATION_DURATION_SHORT);

            // animate in a scrim as background protection (as we're kinda faking a dialog)
            scrim.setVisibility(View.VISIBLE);
            Animator scrimColour = ObjectAnimator.ofArgb(scrim,
                    "backgroundColor",
                    ContextCompat.getColor(HomeActivity.this, R.color.scrim))
                    .setDuration(ANIMATION_DURATION_SLOOOOW);
            Animator scrimBounds = ViewAnimationUtils.createCircularReveal(
                    scrim,
                    fabCenterX,
                    fabCenterY + newPost.getTop(),
                    fab.getWidth() / 2,
                    (int) Math.hypot(fabCenterX, fabCenterY + newPost.getTop()))
                    .setDuration(ANIMATION_DURATION_MED);

            // fade out the fab (faster than the others)
            Animator fadeOutFab = ObjectAnimator.ofFloat(fab, View.ALPHA, 0f)
                    .setDuration(ANIMATION_DURATION_NEAR_INSTANT);
            fadeOutFab.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setVisibility(View.INVISIBLE);
                }
            });

            // play 'em all together with the material interpolator
            AnimatorSet show = new AnimatorSet();
            show.setInterpolator(AnimUtils.getMaterialInterpolator(HomeActivity.this));
            show.playTogether(reveal, background, raise, scrimColour, scrimBounds, position,
                    fadeOutFab);
            show.start();
        }
    };

    private DismissibleViewCallback dismissNewPost = new DismissibleViewCallback() {
        @Override
        public void onViewDismissed() {
            // called when drag dismissed the new post dialog past the threshold distance
            float distanceToGo = 0f;
            if (newPost.getTranslationY() >= 0f) {
                distanceToGo = newPost.getHeight();
            } else {
                distanceToGo = -((ViewGroup) newPost.getParent()).getHeight();
            }
            newPost.animate()
                    .translationY(distanceToGo)
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_SHORT)
                    .setInterpolator(AnimationUtils.loadInterpolator(HomeActivity.this, android.R
                            .interpolator.accelerate_quad))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // 1. do some clean up - reset values
                            newPost.setVisibility(View.INVISIBLE);
                            newPost.setAlpha(1f);
                            newPost.setTranslationX(0f);
                            newPost.setTranslationY(0f);
                            newPost.setScaleX(1f);
                            newPost.setScaleY(1f);

                            // 2. remove the dialog scrim
                            Animator scrim = removeScrim();
                            scrim.setInterpolator(AnimUtils.getMaterialInterpolator(HomeActivity
                                    .this));
                            scrim.start();

                            // 3. and then re-show the FAB
                            fab.setVisibility(View.VISIBLE);
                            fab.setAlpha(0f);
                            fab.setScaleX(0f);
                            fab.setScaleY(0f);
                            fab.setTranslationY(fab.getHeight() / 2);
                            fab.animate()
                                    .alpha(1f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .translationY(0f)
                                    .setDuration(ANIMATION_DURATION_SHORT)
                                    .setInterpolator(AnimUtils.getMaterialInterpolator
                                            (HomeActivity.this));
                        }
                    })
                    .start();
            ImeUtils.hideIme(newStoryTitle);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        //toolbar.inflateMenu(R.menu.main);
        setActionBar(toolbar);
        if (savedInstanceState == null) {
            animateToolbar();
        }

        fab.setOnClickListener(fabClick);
        grid.setEmptyView(findViewById(android.R.id.empty));
        adapter = new FeedAdapter(this, PocketUtils.isPocketInstalled(this));
        grid.setAdapter(adapter);
        grid.setOnScrollListener(gridScroll);
        drawer.setDrawerListener(
                new SystemBarDrawerTinter(
                        ContextCompat.getColor(this, R.color.immersive_bars),
                        ContextCompat.getColor(this, R.color.background_super_dark)));
        filtersList.setLayoutManager(new LinearLayoutManager(this));
        filtersToolbar.inflateMenu(R.menu.filters);
        filtersToolbar.setOnMenuItemClickListener(mFiltersMenuClick);

        newPost.setVisibility(View.INVISIBLE);
        submitNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideNewPost();
            }
        });

//        draggableFrame.setDragDismissView(findViewById(R.id.new_story_container));
//        draggableFrame.setCallbacks(newPostDismissed);
        newPost.setCallback(dismissNewPost);

        // drawer layout treats fitsSystemWindows specially so we have to handle insets ourselves
        // this is super gross and breaks when you show a keyboard.  TODO FIXME
        drawer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // inset the toolbar down by the status bar height
                ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                        .getLayoutParams();
                lpToolbar.topMargin += insets.getSystemWindowInsetTop();
                lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
                toolbar.setLayoutParams(lpToolbar);

                // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
                grid.setPadding(grid.getPaddingLeft(),
                        insets.getSystemWindowInsetTop() + ViewUtils.getActionBarSize
                                (HomeActivity.this),
                        grid.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                        grid.getPaddingBottom());
                grid.setChromePaddingBottom(insets.getSystemWindowInsetBottom());

                // inset the fab for the navbar
                ViewGroup.MarginLayoutParams lpFab = (ViewGroup.MarginLayoutParams) fab
                        .getLayoutParams();
                lpFab.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
                lpFab.rightMargin += insets.getSystemWindowInsetRight(); // landscape
                fab.setLayoutParams(lpFab);

                // we place a background behind the status bar to combine with it's semi-transparent
                // color to get the desired appearance.  Set it's height to the status bar height
                View statusBarBackground = findViewById(R.id.status_bar_background);
                FrameLayout.LayoutParams lpStatus = (FrameLayout.LayoutParams)
                        statusBarBackground.getLayoutParams();
                lpStatus.height = insets.getSystemWindowInsetTop();
                statusBarBackground.setLayoutParams(lpStatus);

                // inset the filters pane for the status bar / navbar
                LinearLayout filtersPane = (LinearLayout) findViewById(R.id.filters_pane);
                ViewGroup.MarginLayoutParams lpFilters = (ViewGroup.MarginLayoutParams)
                        filtersPane.getLayoutParams();
                lpFilters.topMargin += insets.getSystemWindowInsetTop();
                lpFilters.bottomMargin += insets.getSystemWindowInsetBottom();
                filtersPane.setLayoutParams(lpFilters);
                // we also need to set the padding right for landscape
                if (filtersPane.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
                    filtersPane.setPadding(filtersPane.getPaddingLeft(),
                            filtersPane.getPaddingTop(),
                            filtersPane.getPaddingRight() + insets.getSystemWindowInsetRight(),
                            filtersPane.getPaddingBottom());
                }

                // we place a background behind the nav bar when the new post pane is showing. Set
                // it's height to the nav bar height
                View newStoryContainer = findViewById(R.id.new_story_container);
                ViewGroup.MarginLayoutParams lpNewStory = (ViewGroup.MarginLayoutParams)
                        newStoryContainer.getLayoutParams();
                lpNewStory.bottomMargin += insets.getSystemWindowInsetBottom();
                newStoryContainer.setLayoutParams(lpNewStory);

                return insets.consumeSystemWindowInsets();
            }
        });
        setupTaskDescription();

        // setup the API access objects
        createDribbbleApi();
        createDesignerNewsApi();
        createProductHuntApi();

        // check which data sources are enabled and load them
        filtersAdapter = new FilterAdapter(SourceManager.getSources(this), filtersChangedListener);
        filtersList.setAdapter(filtersAdapter);
        ItemTouchHelper.Callback callback = new FilterTouchHelperCallback(filtersAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(filtersList);
        filtersChangedListener.onFiltersChanged(null); // kick off an initial load
        checkConnectivity();
    }

    private void setupTaskDescription() {
        // set a silhouette icon in overview as the launcher icon is a bit busy
        // and looks bad on top of colorPrimary
        Bitmap overviewIcon = ImageUtils.vectorToBitmap(this, R.drawable.ic_launcher_silhouette);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),
                overviewIcon,
                ContextCompat.getColor(this, R.color.primary)));
        overviewIcon.recycle();
    }

    private void createDesignerNewsApi() {
        designerNewsPrefs = new DesignerNewsPrefs(getApplicationContext());
        RestAdapter designerNewsRestAdapter = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .build();
        designerNewsApi = designerNewsRestAdapter.create(DesignerNewsService.class);
    }

    private void createProductHuntApi() {
        //Gson gson = new GsonBuilder()
        //        .setDateFormat(ProductHuntService.DATE_FORMAT)
        //       .create();
        RestAdapter productHuntRestAdapter = new RestAdapter.Builder()
                .setEndpoint(ProductHuntService.ENDPOINT)
                        //.setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new AuthInterceptor(ProductHuntPrefs.DEVELOPER_TOKEN))
                .build();
        productHuntApi = productHuntRestAdapter.create(ProductHuntService.class);
    }

    private void createDribbbleApi() {
        dribbblePrefs = new DribbblePrefs(getApplicationContext());
        Gson gson = new GsonBuilder()
                .setDateFormat(DribbbleService.DATE_FORMAT)
                .create();
        RestAdapter dribbbleRestAdapter = new RestAdapter.Builder()
                .setEndpoint(DribbbleService.ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new AuthInterceptor(dribbblePrefs.getAccessToken()))
                .build();
        dribbbleApi = dribbbleRestAdapter.create((DribbbleService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // re-initialise the dribble and dn API objects (as user may have logged in in child
        // activity)
        createDribbbleApi();
        createDesignerNewsApi();
    }

    private void animateToolbar() {
        // this is gross but toolbar doesn't expose it's children to animate them :(
        View t = toolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            // fade in and space out the title.  Animating the letterSpacing performs horribly so
            // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
            title.setAlpha(0f);
            title.setScaleX(0.8f);

            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900)
                    .setInterpolator(AnimUtils.getMaterialInterpolator(this));
        }
        View amv = toolbar.getChildAt(1);
        if (amv != null & amv instanceof ActionMenuView) {
            ActionMenuView actions = (ActionMenuView) amv;
            popAnim(actions.getChildAt(0), 500, 200); // filter
            popAnim(actions.getChildAt(1), 700, 200); // overflow
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator
                            .overshoot));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem dribbbleLogin = menu.findItem(R.id.menu_dribbble_login);
        if (dribbbleLogin != null) {
            dribbbleLogin.setTitle(dribbblePrefs.isLoggedIn() ? R.string.dribbble_log_out : R
                    .string.dribbble_login);
        }
        MenuItem designerNewsLogin = menu.findItem(R.id.menu_designer_news_login);
        if (designerNewsLogin != null) {
            designerNewsLogin.setTitle(designerNewsPrefs.isLoggedIn() ? R.string
                    .designer_news_log_out : R.string.designer_news_login);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
                drawer.openDrawer(GravityCompat.END);
                return true;
            case R.id.menu_search:
                searchMenuItem = item;
                View v = LayoutInflater.from(this).inflate(R.layout.search_action_view, null);
                //v.setBackground(new MorphDrawable(ContextCompat.getColor(this, R.color.background_dark),
                // getResources().getDimension(R.dimen.dialog_corners)));
                item.setActionView(v);
                ActivityOptions options =
                        ActivityOptions.makeSceneTransitionAnimation(this,
                                Pair.create(v, v.getTransitionName()),
                                Pair.create(((ViewGroup) v).getChildAt(0), "search_icon"));
                startActivityForResult(new Intent(this, SearchActivity.class), RC_SEARCH, options
                        .toBundle());
                //startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.menu_dribbble_login:
                if (!dribbblePrefs.isLoggedIn()) {
                    dribbblePrefs.login(HomeActivity.this);
                } else {
                    dribbblePrefs.logout();
                    createDribbbleApi(); // recreate to clear the access token
                    filtersAdapter.disableAuthorisedDribbleSources(this);
                    // TODO something better than a toast!!
                    Toast.makeText(getApplicationContext(), R.string.dribbble_logged_out, Toast
                            .LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_designer_news_login:
                if (!designerNewsPrefs.isLoggedIn()) {
                    startActivity(new Intent(this, DesignerNewsLogin.class));
                } else {
                    designerNewsPrefs.logout();
                    createDesignerNewsApi(); // recreate to clear the access token
                    // TODO something better than a toast!!
                    Toast.makeText(getApplicationContext(), R.string.designer_news_logged_out,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_about:
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // todo handle search
        if (searchMenuItem != null) {
            searchMenuItem.setActionView(null);
            searchMenuItem = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isNewPostShowing()) {
            hideNewPost();
        } else {
            super.onBackPressed();
        }
    }

    private void loadSource(Source source) {
        if (source.active) {
            if (SourceManager.SOURCE_DESIGNER_NEWS_POPULAR.equals(source.key)) {
                loadDesignerNewsTopStories();
            } else if (SourceManager.SOURCE_DESIGNER_NEWS_RECENT.equals(source.key)) {
                loadDesignerNewsRecent();
            } else if (SourceManager.SOURCE_DRIBBBLE_FOLLOWING.equals(source.key)) {
                loadDribbbleFollowing();
            } else if (SourceManager.SOURCE_DRIBBBLE_POPULAR.equals(source.key)) {
                loadDribbblePopular();
            } else if (SourceManager.SOURCE_DRIBBBLE_RECENT.equals(source.key)) {
                loadDribbbleRecent();
            } else if (SourceManager.SOURCE_DRIBBBLE_DEBUTS.equals(source.key)) {
                loadDribbbleDebuts();
            } else if (SourceManager.SOURCE_DRIBBBLE_ANIMATED.equals(source.key)) {
                loadDribbbleAnimated();
            } else if (SourceManager.SOURCE_HACKER_NEWS.equals(source.key)) {
                loadHackerNews();
            } else if (SourceManager.SOURCE_PRODUCT_HUNT.equals(source.key)) {
                loadProductHunt();
            } else if (source instanceof Source.DribbbleSearchSource) {
                loadDribbbleSearch(((Source.DribbbleSearchSource) source).query);
            }
        }
    }

    private void loadDesignerNewsTopStories() {
        designerNewsApi.getTopStories(new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null) {
                    addItems(storiesResponse.stories);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Loading DN Top Stories", error.getMessage(), error);
            }
        });
    }

    private void loadDesignerNewsRecent() {
        designerNewsApi.getRecentStories(new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null) {
                    addItems(storiesResponse.stories);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loadDribbblePopular() {
        dribbbleApi.getPopular(25, new Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                addItems(shots);
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void loadDribbbleDebuts() {
        dribbbleApi.getDebuts(0, 25, new Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                addItems(shots);
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void loadDribbbleAnimated() {
        dribbbleApi.getAnimated(0, 25, new Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                addItems(shots);
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void loadDribbbleRecent() {
        dribbbleApi.getRecent(0, 25, new Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                addItems(shots);
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void loadDribbbleFollowing() {
        if (dribbblePrefs.isLoggedIn()) {
            dribbbleApi.getFollowing(25, new Callback<List<Shot>>() {
                @Override
                public void success(List<Shot> shots, Response response) {
                    addItems(shots);
                }

                @Override
                public void failure(RetrofitError error) {
                }
            });
        }
    }

    private void loadDribbbleSearch(final String query) {
        new AsyncTask<Void, Void, List<Shot>>() {
            @Override
            protected List<Shot> doInBackground(Void... params) {
                return DribbbleSearch.search(query);
            }

            @Override
            protected void onPostExecute(List<Shot> shots) {
                if (shots != null && shots.size() > 0) {
                    addItems(shots);
                }
            }
        }.execute();
    }

    private void loadHackerNews() {
        new AsyncTask<Void, Void, Posts>() {
            @Override
            protected Posts doInBackground(Void... params) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(HackerNewsService.ENDPOINT)
                        .build();
                HackerNewsService service = restAdapter.create(HackerNewsService.class);
                Posts topNews = null;
                try {
                    topNews = service.getTopNews();
                } catch (RetrofitError error) {
                    // TODO
                }
                return topNews;
            }

            @Override
            protected void onPostExecute(Posts posts) {
                if (posts != null) {
                    List<Post> topTen = posts.items.subList(0, 10);
                    for (int i = 0; i < 10; i++) {
                        topTen.get(i).weight = (float) (0.6 - (i * 0.05));
                    }
                    addItems(topTen);
                }
            }
        }.execute();
    }

    private void loadProductHunt() {
        productHuntApi.getPosts(new Callback<PostsResponse>() {
            @Override
            public void success(PostsResponse postsResponse, Response response) {
                if (postsResponse != null) {
                    addItems(postsResponse.posts);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Ohnoes", error.getMessage(), error);
            }
        });
    }

    private void addItems(Collection<? extends PlaidItem> items) {
        adapter.addAndResort(items);

        // ensure that the fab is not obscuring any items when scrolled to the bottom
        // i.e. if there's an item in the bottom right cell then add more padding
        int desiredPadding = (adapter.getCount() % getResources().getInteger(R.integer
                .num_columns) == 0) ?
                getResources().getDimensionPixelSize(R.dimen.padding_room_for_fab) : 0;
        if (grid.getContentPaddingBottom() != desiredPadding) {
            grid.setContentPaddingBottom(desiredPadding);
        }
    }

    boolean isNewPostShowing() {
        return newPost.getVisibility() == View.VISIBLE;
    }

    void hideNewPost() {

        // calculate where the new post needs to end up (centered over the FAB)
        int fabCenterX = (fab.getLeft() + fab.getRight()) / 2;
        int fabCenterY = ((fab.getTop() + fab.getBottom()) / 2);
        int translateX = fabCenterX - (newPost.getWidth() / 2);
        int translateY = fabCenterY - (newPost.getTop() + (newPost.getHeight() / 2));

        // then circular clip the new post view down to the FAB dimens
        Animator clip = ViewAnimationUtils.createCircularReveal(
                newPost,
                newPost.getWidth() / 2,
                newPost.getHeight() / 2,
                (int) Math.hypot(newPost.getWidth() / 2, newPost.getHeight() / 2),
                fab.getWidth() / 2)
                .setDuration(ANIMATION_DURATION_SHORT);
        clip.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                newPost.setVisibility(View.INVISIBLE);
            }
        });

        // translate the new post view back toward the FAB along an arc
        Path motionPath = new ArcMotion().getPath(0, 0, translateX, translateY);
        Animator position = ObjectAnimator.ofFloat(newPost, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(ANIMATION_DURATION_SHORT);

        // animate from our dialog colour back to the FAB colour
        Animator background = ObjectAnimator.ofArgb(newPostContainer,
                "backgroundColor",
                ContextCompat.getColor(this, R.color.background_light),
                ContextCompat.getColor(this, R.color.accent))
                .setDuration(ANIMATION_DURATION_MED);

        // lower up in Z
        Animator lower = ObjectAnimator.ofFloat(newPost, View.TRANSLATION_Z, 0)
                .setDuration(ANIMATION_DURATION_SHORT);

        // animate out the scrim & re-raise the toolbar
        Animator scrim = removeScrim();

        // fade the FAB back in – quickly & a little delayed so that it's at the end of the
        // clip/reveal
        fab.setVisibility(View.VISIBLE);
        Animator fadeInFab = ObjectAnimator.ofFloat(fab, View.ALPHA, 1f)
                .setDuration(ANIMATION_DURATION_NEAR_INSTANT);
        fadeInFab.setStartDelay(ANIMATION_DURATION_SHORT - ANIMATION_DURATION_NEAR_INSTANT);

        // play 'em all together with the material interpolator
        AnimatorSet hide = new AnimatorSet();
        hide.setInterpolator(AnimUtils.getMaterialInterpolator(HomeActivity.this));
        hide.playTogether(clip, position, background, lower, scrim, fadeInFab);
        hide.start();
    }

//    private DragDownDismissFrameLayout.Callbacks newPostDismissed = new
// DragDownDismissFrameLayout.Callbacks() {
//
//        @Override public boolean shouldCapture() {
//            return true;
//        }
//
//        @Override
//        public void onViewDismissed(int viewTop, int viewBottom) {
//            // called when we have drag dismissed the new post dialog
//
//            // TODO: dismiss the IME if it is shown
//
//            // 1. need to do some clean up
//            newPost.setVisibility(View.INVISIBLE);
//            newPost.setAlpha(ALPHA_OPAQUE);
//            newPost.setTop(viewTop);
//            newPost.setBottom(viewBottom);
//            newPost.setTranslationX(0f);
//            newPost.setTranslationY(0f);
//
//            // 2. remove the dialog scrim
//            Animator scrim = removeScrim();
//            scrim.setInterpolator(AnimUtils.getMaterialInterpolator(HomeActivity.this));
//            scrim.start();
//
//            // 3. and then re-show the FAB
//            fab.setVisibility(View.VISIBLE);
//            fab.setAlpha(ALPHA_TRANSPARENT);
//            fab.setScaleX(0f);
//            fab.setScaleY(0f);
//            fab.setTranslationY(fab.getHeight() / 2);
//            fab.animate()
//                    .alpha(ALPHA_OPAQUE)
//                    .scaleX(1f)
//                    .scaleY(1f)
//                    .translationY(0f)
//                    .setDuration(ANIMATION_DURATION_SHORT)
//                    .setInterpolator(AnimUtils.getMaterialInterpolator(HomeActivity.this));
//        }
//
//        @Override public void onDrag(int top) { }
//    };

    private Animator removeScrim() {
        Animator scrim = ObjectAnimator.ofFloat(this.scrim, View.ALPHA, 0f)
                .setDuration(ANIMATION_DURATION_MED);
        scrim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                HomeActivity.this.scrim.setVisibility(View.INVISIBLE);
                HomeActivity.this.scrim.setAlpha(1.0f);
            }
        });
        return scrim;
    }

    public void hideNewPost(View view) {
        hideNewPost();
    }

    private void checkConnectivity() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!connected) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
            ViewStub stub = (ViewStub) findViewById(R.id.stub_no_connection);
            ImageView iv = (ImageView) stub.inflate();
            final AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getDrawable(R.drawable
                    .avd_no_connection);
            iv.setImageDrawable(avd);
            avd.start();
        }
    }

    private class SystemBarDrawerTinter extends DrawerLayout.SimpleDrawerListener {

        private final int startColor;
        private final int endColor;

        public SystemBarDrawerTinter(@ColorInt int startColor, @ColorInt int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            int color = ColorUtils.blendColors(startColor, endColor, slideOffset);
            getWindow().setStatusBarColor(color);
            getWindow().setNavigationBarColor(color);
        }
    }
}
