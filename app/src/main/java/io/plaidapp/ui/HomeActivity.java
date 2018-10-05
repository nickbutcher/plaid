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

package io.plaidapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import io.plaidapp.R;
import io.plaidapp.core.data.DataManager;
import io.plaidapp.core.data.Source;
import io.plaidapp.core.designernews.data.login.LoginRepository;
import io.plaidapp.core.designernews.data.poststory.PostStoryService;
import io.plaidapp.core.designernews.data.stories.model.Story;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.ui.FeedAdapter;
import io.plaidapp.core.ui.FilterAdapter;
import io.plaidapp.core.ui.FilterAnimator;
import io.plaidapp.core.ui.HomeGridItemAnimator;
import io.plaidapp.core.ui.recyclerview.InfiniteScrollListener;
import io.plaidapp.core.ui.transitions.FabTransform;
import io.plaidapp.core.util.Activities;
import io.plaidapp.core.util.ActivityHelper;
import io.plaidapp.core.util.AnimUtils;
import io.plaidapp.core.util.DrawableUtils;
import io.plaidapp.core.util.ShortcutHelper;
import io.plaidapp.core.util.ViewUtils;
import io.plaidapp.ui.recyclerview.FilterTouchHelperCallback;
import io.plaidapp.ui.recyclerview.GridItemDividerDecoration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.plaidapp.dagger.Injector.inject;

public class HomeActivity extends Activity {

    private static final int RC_SEARCH = 0;
    private static final int RC_NEW_DESIGNER_NEWS_STORY = 4;
    private static final int RC_NEW_DESIGNER_NEWS_LOGIN = 5;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private RecyclerView grid;
    private ImageButton fab;
    private RecyclerView filtersList;
    private ProgressBar loading;
    private @Nullable
    ImageView noConnection;
    ImageButton fabPosting;
    GridLayoutManager layoutManager;
    private int columns;
    boolean connected = true;
    private TextView noFiltersEmptyText;
    private boolean monitoringConnectivity = false;

    // data
    @Inject
    DataManager dataManager;
    @Inject
    FeedAdapter adapter;
    @Inject
    FilterAdapter filtersAdapter;
    @Inject
    LoginRepository loginRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bindResources();
        inject(this, data -> {
            adapter.addAndResort(data);
            checkEmptyState();
        });

        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        setActionBar(toolbar);
        if (savedInstanceState == null) {
            animateToolbar();
        }
        setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this));

        ViewPreloadSizeProvider<Shot> shotPreloadSizeProvider = new ViewPreloadSizeProvider<>();

        grid.setAdapter(adapter);
        layoutManager = new GridLayoutManager(this, columns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemColumnSpan(position);
            }
        });
        grid.setLayoutManager(layoutManager);
        grid.addOnScrollListener(toolbarElevation);
        grid.addOnScrollListener(new InfiniteScrollListener(layoutManager, dataManager) {
            @Override
            public void onLoadMore() {
                dataManager.loadAllDataSources();
            }
        });
        grid.setHasFixedSize(true);
        grid.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height,
                R.color.divider));
        grid.setItemAnimator(new HomeGridItemAnimator());

        RecyclerViewPreloader<Shot> shotPreloader =
                new RecyclerViewPreloader<>(this, adapter, shotPreloadSizeProvider, 4);
        grid.addOnScrollListener(shotPreloader);

        // drawer layout treats fitsSystemWindows specially so we have to handle insets ourselves
        drawer.setOnApplyWindowInsetsListener((v, insets) -> {
            // inset the toolbar down by the status bar height
            ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                    .getLayoutParams();
            lpToolbar.topMargin += insets.getSystemWindowInsetTop();
            lpToolbar.leftMargin += insets.getSystemWindowInsetLeft();
            lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
            toolbar.setLayoutParams(lpToolbar);

            // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
            grid.setPadding(
                    grid.getPaddingLeft() + insets.getSystemWindowInsetLeft(), // landscape
                    insets.getSystemWindowInsetTop()
                            + ViewUtils.getActionBarSize(HomeActivity.this),
                    grid.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                    grid.getPaddingBottom() + insets.getSystemWindowInsetBottom());

            // inset the fab for the navbar
            ViewGroup.MarginLayoutParams lpFab = (ViewGroup.MarginLayoutParams) fab
                    .getLayoutParams();
            lpFab.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
            lpFab.rightMargin += insets.getSystemWindowInsetRight(); // landscape
            fab.setLayoutParams(lpFab);

            View postingStub = findViewById(R.id.stub_posting_progress);
            ViewGroup.MarginLayoutParams lpPosting =
                    (ViewGroup.MarginLayoutParams) postingStub.getLayoutParams();
            lpPosting.bottomMargin += insets.getSystemWindowInsetBottom(); // portrait
            lpPosting.rightMargin += insets.getSystemWindowInsetRight(); // landscape
            postingStub.setLayoutParams(lpPosting);

            // we place a background behind the status bar to combine with it's semi-transparent
            // color to get the desired appearance.  Set it's height to the status bar height
            View statusBarBackground = findViewById(R.id.status_bar_background);
            FrameLayout.LayoutParams lpStatus = (FrameLayout.LayoutParams)
                    statusBarBackground.getLayoutParams();
            lpStatus.height = insets.getSystemWindowInsetTop();
            statusBarBackground.setLayoutParams(lpStatus);

            // inset the filters list for the status bar / navbar
            // need to set the padding end for landscape case
            final boolean ltr = filtersList.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
            filtersList.setPaddingRelative(filtersList.getPaddingStart(),
                    filtersList.getPaddingTop() + insets.getSystemWindowInsetTop(),
                    filtersList.getPaddingEnd() + (ltr ? insets.getSystemWindowInsetRight() :
                            0),
                    filtersList.getPaddingBottom() + insets.getSystemWindowInsetBottom());

            // clear this listener so insets aren't re-applied
            drawer.setOnApplyWindowInsetsListener(null);

            return insets.consumeSystemWindowInsets();
        });
        setupTaskDescription();

        filtersList.setAdapter(filtersAdapter);
        filtersList.setItemAnimator(new FilterAnimator());
        filtersAdapter.registerFilterChangedCallback(filtersChangedCallbacks);
        dataManager.loadAllDataSources();
        ItemTouchHelper.Callback callback = new FilterTouchHelperCallback(filtersAdapter, this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(filtersList);
        checkEmptyState();
    }

    private void bindResources() {
        drawer = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.toolbar);
        grid = findViewById(R.id.grid);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> { fabClick(); });
        filtersList = findViewById(R.id.filters);
        loading = findViewById(android.R.id.empty);
        noConnection = findViewById(R.id.no_connection);

        columns = getResources().getInteger(R.integer.num_columns);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectivity();
    }

    @Override
    protected void onPause() {
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        super.onPause();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (data == null || resultCode != RESULT_OK
                || !data.hasExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID)) return;

        // When reentering, if the shared element is no longer on screen (e.g. after an
        // orientation change) then scroll it into view.
        final long sharedShotId = data.getLongExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID,
                -1L);
        if (sharedShotId != -1L                                             // returning from a shot
                && adapter.getDataItemCount() > 0                           // grid populated
                && grid.findViewHolderForItemId(sharedShotId) == null) {    // view not attached
            final int position = adapter.getItemPosition(sharedShotId);
            if (position == RecyclerView.NO_POSITION) return;

            // delay the transition until our shared element is on-screen i.e. has been laid out
            postponeEnterTransition();
            grid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b,
                                           int oL, int oT, int oR, int oB) {
                    grid.removeOnLayoutChangeListener(this);
                    startPostponedEnterTransition();
                }
            });
            grid.scrollToPosition(position);
            toolbar.setTranslationZ(-1f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem designerNewsLogin = menu.findItem(R.id.menu_designer_news_login);
        if (designerNewsLogin != null) {
            designerNewsLogin.setTitle(loginRepository.isLoggedIn() ?
                    R.string.designer_news_log_out : R.string.designer_news_login);
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
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                startActivityForResult(ActivityHelper.intentTo(Activities.Search.INSTANCE), RC_SEARCH, options);
                return true;
            case R.id.menu_designer_news_login:
                if (!loginRepository.isLoggedIn()) {
                    startActivity(ActivityHelper.intentTo(Activities.DesignerNews.Login.INSTANCE));
                } else {
                    loginRepository.logout();
                    ShortcutHelper.disablePostShortcut(this);
                    // TODO something better than a toast!!
                    Toast.makeText(getApplicationContext(), R.string.designer_news_logged_out,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_about:
                startActivity(ActivityHelper.intentTo(Activities.About.INSTANCE),
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SEARCH:
                // reset the search icon which we hid
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                if (searchMenuView != null) {
                    searchMenuView.setAlpha(1f);
                }
                if (resultCode == Activities.Search.RESULT_CODE_SAVE) {
                    String query = data.getStringExtra(Activities.Search.EXTRA_QUERY);
                    if (TextUtils.isEmpty(query)) return;
                    Source dribbbleSearch = null;
                    Source designerNewsSearch = null;
                    boolean newSource = false;
                    if (data.getBooleanExtra(Activities.Search.EXTRA_SAVE_DRIBBBLE, false)) {
                        dribbbleSearch = new Source.DribbbleSearchSource(query, true);
                        newSource = filtersAdapter.addFilter(dribbbleSearch);
                    }
                    if (data.getBooleanExtra(Activities.Search.EXTRA_SAVE_DESIGNER_NEWS, false)) {
                        designerNewsSearch = new Source.DesignerNewsSearchSource(query, true);
                        newSource |= filtersAdapter.addFilter(designerNewsSearch);
                    }
                    if (newSource) {
                        highlightNewSources(dribbbleSearch, designerNewsSearch);
                    }
                }
                break;
            case RC_NEW_DESIGNER_NEWS_STORY:
                switch (resultCode) {
                    case Activities.DesignerNews.PostStory.RESULT_DRAG_DISMISSED:
                        // need to reshow the FAB as there's no shared element transition
                        showFab();
                        unregisterPostStoryResultListener();
                        break;
                    case Activities.DesignerNews.PostStory.RESULT_POSTING:
                        showPostingProgress();
                        break;
                    default:
                        unregisterPostStoryResultListener();
                        break;
                }
                break;
            case RC_NEW_DESIGNER_NEWS_LOGIN:
                if (resultCode == RESULT_OK) {
                    showFab();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dataManager.cancelLoading();
        super.onDestroy();
    }

    // listener for notifying adapter when data sources are deactivated
    private FilterAdapter.FiltersChangedCallbacks filtersChangedCallbacks =
            new FilterAdapter.FiltersChangedCallbacks() {
        @Override
        public void onFiltersChanged(Source changedFilter) {
            if (!changedFilter.active) {
                adapter.removeDataSource(changedFilter.key);
            }
            checkEmptyState();
        }

        @Override
        public void onFilterRemoved(Source removed) {
            adapter.removeDataSource(removed.key);
            checkEmptyState();
        }
    };

    private RecyclerView.OnScrollListener toolbarElevation = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // we want the grid to scroll over the top of the toolbar but for the toolbar items
            // to be clickable when visible. To achieve this we play games with elevation. The
            // toolbar is laid out in front of the grid but when we scroll, we lower it's elevation
            // to allow the content to pass in front (and reset when scrolled to top of the grid)
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && layoutManager.findFirstVisibleItemPosition() == 0
                    && layoutManager.findViewByPosition(0).getTop() == grid.getPaddingTop()
                    && toolbar.getTranslationZ() != 0) {
                // at top, reset elevation
                toolbar.setTranslationZ(0f);
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING
                    && toolbar.getTranslationZ() != -1f) {
                // grid scrolled, lower toolbar to allow content to pass in front
                toolbar.setTranslationZ(-1f);
            }
        }
    };

    protected void fabClick() {
        if (loginRepository.isLoggedIn()) {
            Intent intent = ActivityHelper.intentTo(Activities.DesignerNews.PostStory.INSTANCE);
            FabTransform.addExtras(intent,
                    ContextCompat.getColor(this, R.color.accent), R.drawable.ic_add_dark);
            intent.putExtra(PostStoryService.EXTRA_BROADCAST_RESULT, true);
            registerPostStoryResultListener();
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, fab,
                    getString(R.string.transition_new_designer_news_post));
            startActivityForResult(intent, RC_NEW_DESIGNER_NEWS_STORY, options.toBundle());
        } else {
            Intent intent = ActivityHelper.intentTo(Activities.DesignerNews.Login.INSTANCE);
            FabTransform.addExtras(intent,
                    ContextCompat.getColor(this, R.color.accent), R.drawable.ic_add_dark);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, fab,
                    getString(R.string.transition_designer_news_login));
            startActivityForResult(intent, RC_NEW_DESIGNER_NEWS_LOGIN, options.toBundle());
        }
    }

    BroadcastReceiver postStoryResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ensurePostingProgressInflated();
            switch (intent.getAction()) {
                case PostStoryService.BROADCAST_ACTION_SUCCESS:
                    // success animation
                    AnimatedVectorDrawable complete =
                            (AnimatedVectorDrawable) getDrawable(R.drawable.avd_upload_complete);
                    if (complete != null) {
                        fabPosting.setImageDrawable(complete);
                        complete.start();
                        fabPosting.postDelayed(() -> fabPosting.setVisibility(View.GONE), 2100); // length of R.drawable.avd_upload_complete
                    }

                    // actually add the story to the grid
                    Story newStory = intent.getParcelableExtra(PostStoryService.EXTRA_NEW_STORY);
                    adapter.addAndResort(Collections.singletonList(newStory));
                    break;
                case PostStoryService.BROADCAST_ACTION_FAILURE:
                    // failure animation
                    AnimatedVectorDrawable failed =
                            (AnimatedVectorDrawable) getDrawable(R.drawable.avd_upload_error);
                    if (failed != null) {
                        fabPosting.setImageDrawable(failed);
                        failed.start();
                    }
                    // remove the upload progress 'fab' and reshow the regular one
                    fabPosting.animate()
                            .alpha(0f)
                            .rotation(90f)
                            .setStartDelay(2000L) // leave error on screen briefly
                            .setDuration(300L)
                            .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(HomeActivity
                                    .this))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    fabPosting.setVisibility(View.GONE);
                                    fabPosting.setAlpha(1f);
                                    fabPosting.setRotation(0f);
                                }
                            });
                    break;
            }
            unregisterPostStoryResultListener();
        }
    };

    void registerPostStoryResultListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PostStoryService.BROADCAST_ACTION_SUCCESS);
        intentFilter.addAction(PostStoryService.BROADCAST_ACTION_FAILURE);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(postStoryResultReceiver, intentFilter);
    }

    void unregisterPostStoryResultListener() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(postStoryResultReceiver);
    }

    void revealPostingProgress() {
        Animator reveal = ViewAnimationUtils.createCircularReveal(fabPosting,
                (int) fabPosting.getPivotX(),
                (int) fabPosting.getPivotY(),
                0f,
                fabPosting.getWidth() / 2)
                .setDuration(600L);
        reveal.setInterpolator(AnimUtils.getFastOutLinearInInterpolator(this));
        reveal.start();
        AnimatedVectorDrawable uploading =
                (AnimatedVectorDrawable) getDrawable(R.drawable.avd_uploading);
        if (uploading != null) {
            fabPosting.setImageDrawable(uploading);
            uploading.start();
        }
    }

    void ensurePostingProgressInflated() {
        if (fabPosting != null) return;
        fabPosting = (ImageButton) ((ViewStub) findViewById(R.id.stub_posting_progress)).inflate();
    }

    void checkEmptyState() {
        if (adapter.getDataItemCount() == 0) {
            // if grid is empty check whether we're loading or if no filters are selected
            if (filtersAdapter.getEnabledSourcesCount() > 0) {
                if (connected) {
                    loading.setVisibility(View.VISIBLE);
                    setNoFiltersEmptyTextVisibility(View.GONE);
                }
            } else {
                loading.setVisibility(View.GONE);
                setNoFiltersEmptyTextVisibility(View.VISIBLE);
            }
            toolbar.setTranslationZ(0f);
        } else {
            loading.setVisibility(View.GONE);
            setNoFiltersEmptyTextVisibility(View.GONE);
        }
    }

    private void showPostingProgress() {
        ensurePostingProgressInflated();
        fabPosting.setVisibility(View.VISIBLE);
        // if stub has just been inflated then it will not have been laid out yet
        if (fabPosting.isLaidOut()) {
            revealPostingProgress();
        } else {
            fabPosting.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b,
                                           int oldL, int oldT, int oldR, int oldB) {
                    fabPosting.removeOnLayoutChangeListener(this);
                    revealPostingProgress();
                }
            });
        }
    }

    private void setNoFiltersEmptyTextVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noFiltersEmptyText == null) {
                // create the no filters empty text
                ViewStub stub = findViewById(R.id.stub_no_filters);
                noFiltersEmptyText = (TextView) stub.inflate();
                SpannedString emptyText = (SpannedString) getText(R.string.no_filters_selected);
                SpannableStringBuilder ssb = new SpannableStringBuilder(emptyText);
                final Annotation[] annotations =
                        emptyText.getSpans(0, emptyText.length(), Annotation.class);
                if (annotations != null && annotations.length > 0) {
                    for (int i = 0; i < annotations.length; i++) {
                        final Annotation annotation = annotations[i];
                        if (annotation.getKey().equals("src")) {
                            // image span markup
                            String name = annotation.getValue();
                            int id = getResources().getIdentifier(name, null, getPackageName());
                            if (id == 0) continue;
                            ssb.setSpan(new ImageSpan(this, id,
                                            ImageSpan.ALIGN_BASELINE),
                                    emptyText.getSpanStart(annotation),
                                    emptyText.getSpanEnd(annotation),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (annotation.getKey().equals("foregroundColor")) {
                            // foreground color span markup
                            String name = annotation.getValue();
                            int id = getResources().getIdentifier(name, null, getPackageName());
                            if (id == 0) continue;
                            ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, id)),
                                    emptyText.getSpanStart(annotation),
                                    emptyText.getSpanEnd(annotation),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                noFiltersEmptyText.setText(ssb);
                noFiltersEmptyText.setOnClickListener(v -> drawer.openDrawer(GravityCompat.END));
            }
            noFiltersEmptyText.setVisibility(visibility);
        } else if (noFiltersEmptyText != null) {
            noFiltersEmptyText.setVisibility(visibility);
        }

    }

    private void setupTaskDescription() {
        Bitmap overviewIcon = DrawableUtils.drawableToBitmap(this, getApplicationInfo().icon);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),
                overviewIcon,
                ContextCompat.getColor(this, R.color.primary)));
        overviewIcon.recycle();
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
                    .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this));
        }
    }

    private void showFab() {
        fab.setAlpha(0f);
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setTranslationY(fab.getHeight() / 2);
        fab.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(300L)
                .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(this))
                .start();
    }

    /**
     * Highlight the new source(s) by:
     *      1. opening the drawer
     *      2. scrolling new source(s) into view
     *      3. flashing new source(s) background
     *      4. closing the drawer (if user hasn't interacted with it)
     */
    private void highlightNewSources(final Source... sources) {
        final Runnable closeDrawerRunnable = () -> drawer.closeDrawer(GravityCompat.END);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            // if the user interacts with the filters while it's open then don't auto-close
            private final View.OnTouchListener filtersTouch = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    drawer.removeCallbacks(closeDrawerRunnable);
                    return false;
                }
            };

            @Override
            public void onDrawerOpened(View drawerView) {
                // scroll to the new item(s) and highlight them
                List<Integer> filterPositions = new ArrayList<>(sources.length);
                for (Source source : sources) {
                    if (source != null) {
                        filterPositions.add(filtersAdapter.getFilterPosition(source));
                    }
                }
                int scrollTo = Collections.max(filterPositions);
                filtersList.smoothScrollToPosition(scrollTo);
                for (int position : filterPositions) {
                    filtersAdapter.highlightFilter(position);
                }
                filtersList.setOnTouchListener(filtersTouch);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // reset
                filtersList.setOnTouchListener(null);
                drawer.removeDrawerListener(this);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // if the user interacts with the drawer manually then don't auto-close
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    drawer.removeCallbacks(closeDrawerRunnable);
                }
            }
        });
        drawer.openDrawer(GravityCompat.END);
        drawer.postDelayed(closeDrawerRunnable, 2000L);
    }

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!connected) {
            loading.setVisibility(View.GONE);
            if (noConnection == null) {
                final ViewStub stub = findViewById(R.id.stub_no_connection);
                noConnection = (ImageView) stub.inflate();
            }
            final AnimatedVectorDrawable avd =
                    (AnimatedVectorDrawable) getDrawable(R.drawable.avd_no_connection);
            if (noConnection != null && avd != null) {
                noConnection.setImageDrawable(avd);
                avd.start();
            }

            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                    connectivityCallback);
            monitoringConnectivity = true;
        }
    }

    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            connected = true;
            if (adapter.getDataItemCount() != 0) return;
            runOnUiThread(() -> {
                TransitionManager.beginDelayedTransition(drawer);
                noConnection.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                dataManager.loadAllDataSources();
            });
        }

        @Override
        public void onLost(Network network) {
            connected = false;
        }
    };
}
