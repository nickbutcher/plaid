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

package io.plaidapp.ui.search;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.TransitionRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import io.plaidapp.core.data.SearchDataManager;
import io.plaidapp.core.data.pocket.PocketUtils;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.ui.FeedAdapter;
import io.plaidapp.core.ui.recyclerview.InfiniteScrollListener;
import io.plaidapp.core.ui.recyclerview.SlideInItemAnimator;
import io.plaidapp.core.util.Activities;
import io.plaidapp.core.util.ImeUtils;
import io.plaidapp.core.util.ShortcutHelper;
import io.plaidapp.core.util.TransitionUtils;
import io.plaidapp.search.R;
import io.plaidapp.ui.search.transitions.CircularReveal;

import java.util.List;

public class SearchActivity extends Activity {

    private ImageButton searchBack;
    private ViewGroup searchBackContainer;
    private SearchView searchView;
    private View searchBackground;
    private ProgressBar progress;
    private RecyclerView results;
    private ViewGroup container;
    private ViewGroup searchToolbar;
    private ViewGroup resultsContainer;
    private ImageButton fab;
    private ViewGroup confirmSaveContainer;
    private CheckedTextView saveDribbble;
    private CheckedTextView saveDesignerNews;
    private Button saveConfirmed;
    private View scrim;
    private View resultsScrim;
    private int columns;
    private float appBarElevation;
    SearchDataManager dataManager;
    FeedAdapter adapter;
    private TextView noResults;
    private SparseArray<Transition> transitions = new SparseArray<>();
    private boolean focusQuery = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bindResources();
        setupSearchView();

        dataManager = new SearchDataManager(this, data -> {
            if (data != null && data.size() > 0) {
                if (results.getVisibility() != View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(container,
                            getTransition(R.transition.search_show_results));
                    progress.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                }
                adapter.addAndResort(data);
            } else {
                TransitionManager.beginDelayedTransition(
                        container, getTransition(io.plaidapp.core.R.transition.auto));
                progress.setVisibility(View.GONE);
                setNoResultsVisibility(View.VISIBLE);
            }
        });
        ViewPreloadSizeProvider<Shot> shotPreloadSizeProvider = new ViewPreloadSizeProvider<>();
        adapter = new FeedAdapter(this, dataManager, columns, PocketUtils.isPocketInstalled(this),
                shotPreloadSizeProvider);
        setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this));
        results.setAdapter(adapter);
        results.setItemAnimator(new SlideInItemAnimator());
        GridLayoutManager layoutManager = new GridLayoutManager(this, columns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemColumnSpan(position);
            }
        });
        results.setLayoutManager(layoutManager);
        results.addOnScrollListener(new InfiniteScrollListener(layoutManager, dataManager) {
            @Override
            public void onLoadMore() {
                dataManager.loadMore();
            }
        });
        results.setHasFixedSize(true);
        RecyclerViewPreloader<Shot> shotPreloader =
                new RecyclerViewPreloader<>(this, adapter, shotPreloadSizeProvider, 4);
        results.addOnScrollListener(shotPreloader);

        setupTransitions();
        onNewIntent(getIntent());
        ShortcutHelper.reportSearchUsed(this);
    }

    private void bindResources() {
        searchBack = findViewById(R.id.searchback);
        searchBack.setOnClickListener(view -> dismiss());
        searchBackContainer = findViewById(R.id.searchback_container);
        searchView = findViewById(R.id.search_view);
        searchBackground = findViewById(R.id.search_background);
        progress = findViewById(android.R.id.empty);
        results = findViewById(R.id.search_results);
        container = findViewById(R.id.container);
        searchToolbar = findViewById(R.id.search_toolbar);
        resultsContainer = findViewById(R.id.results_container);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> save());
        confirmSaveContainer = findViewById(R.id.confirm_save_container);
        View.OnClickListener toggleSave = view -> toggleSaveCheck((CheckedTextView) view);
        saveDribbble = findViewById(R.id.save_dribbble);
        saveDribbble.setOnClickListener(toggleSave);
        saveDesignerNews = findViewById(R.id.save_designer_news);
        saveDesignerNews.setOnClickListener(toggleSave);
        saveConfirmed = findViewById(R.id.save_confirmed);
        saveConfirmed.setOnClickListener(view -> doSave());
        scrim = findViewById(R.id.scrim);
        scrim.setOnClickListener(view -> dismiss());
        resultsScrim = findViewById(R.id.results_scrim);
        resultsScrim.setOnClickListener(view -> hideSaveConfirmation());
        Resources res = getResources();
        columns = res.getInteger(io.plaidapp.core.R.integer.num_columns);
        appBarElevation = res.getDimensionPixelSize(io.plaidapp.core.R.dimen.z_app_bar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchView.setQuery(query, false);
                searchFor(query);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (confirmSaveContainer.getVisibility() == View.VISIBLE) {
            hideSaveConfirmation();
        } else {
            dismiss();
        }
    }

    @Override
    protected void onPause() {
        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        dataManager.cancelLoading();
        super.onDestroy();
    }

    @Override
    public void onEnterAnimationComplete() {
        if (focusQuery) {
            // focus the search view once the enter transition finishes
            searchView.requestFocus();
            ImeUtils.showIme(searchView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FeedAdapter.REQUEST_CODE_VIEW_SHOT:
                // by default we focus the search filed when entering this screen. Don't do that
                // when returning from viewing a search result.
                focusQuery = false;
                break;
        }
    }

    protected void dismiss() {
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.setBackground(null);
        finishAfterTransition();
    }

    protected void save() {
        // show the save confirmation bubble
        TransitionManager.beginDelayedTransition(
                resultsContainer, getTransition(R.transition.search_show_confirm));
        fab.setVisibility(View.INVISIBLE);
        confirmSaveContainer.setVisibility(View.VISIBLE);
        resultsScrim.setVisibility(View.VISIBLE);
    }

    protected void doSave() {
        Intent saveData = new Intent();
        saveData.putExtra(Activities.Search.EXTRA_QUERY, dataManager.getQuery());
        saveData.putExtra(Activities.Search.EXTRA_SAVE_DRIBBBLE, saveDribbble.isChecked());
        saveData.putExtra(Activities.Search.EXTRA_SAVE_DESIGNER_NEWS, saveDesignerNews.isChecked());
        setResult(Activities.Search.RESULT_CODE_SAVE, saveData);
        dismiss();
    }

    protected void hideSaveConfirmation() {
        if (confirmSaveContainer.getVisibility() == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                    resultsContainer, getTransition(R.transition.search_hide_confirm));
            confirmSaveContainer.setVisibility(View.GONE);
            resultsScrim.setVisibility(View.GONE);
            fab.setVisibility(results.getVisibility());
        }
    }

    protected void toggleSaveCheck(CheckedTextView ctv) {
        ctv.toggle();
    }

    void clearResults() {
        TransitionManager.beginDelayedTransition(container,
                getTransition(io.plaidapp.core.R.transition.auto));
        adapter.clear();
        dataManager.clear();
        results.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        confirmSaveContainer.setVisibility(View.GONE);
        resultsScrim.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }

    void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (TextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                noResults.setOnClickListener(v -> {
                    searchView.setQuery("", false);
                    searchView.requestFocus();
                    ImeUtils.showIme(searchView);
                });
            }
            String message = String.format(
                    getString(R.string.no_search_results), searchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noResults.setText(ssb);
        }
        if (noResults != null) {
            noResults.setVisibility(visibility);
        }
    }

    void searchFor(String query) {
        clearResults();
        progress.setVisibility(View.VISIBLE);
        ImeUtils.hideIme(searchView);
        searchView.clearFocus();
        dataManager.searchFor(query);
    }

    Transition getTransition(@TransitionRes int transitionId) {
        Transition transition = transitions.get(transitionId);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId);
            transitions.put(transitionId, transition);
        }
        return transition;
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // hint, inputType & ime options seem to be ignored from XML! Set in code
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (TextUtils.isEmpty(query)) {
                    clearResults();
                }
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && confirmSaveContainer.getVisibility() == View.VISIBLE) {
                hideSaveConfirmation();
            }
        });
    }

    private void setupTransitions() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    View searchIcon = sharedElements.get(0);
                    if (searchIcon.getId() != R.id.searchback) return;
                    int centerX = (searchIcon.getLeft() + searchIcon.getRight()) / 2;
                    CircularReveal hideResults = (CircularReveal) TransitionUtils.findTransition(
                            (TransitionSet) getWindow().getReturnTransition(),
                            CircularReveal.class, R.id.results_container);
                    if (hideResults != null) {
                        hideResults.setCenter(new Point(centerX, 0));
                    }
                }
            }
        });
    }

}
