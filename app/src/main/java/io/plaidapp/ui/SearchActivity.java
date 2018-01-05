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
import android.app.SearchManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.TransitionRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import java.util.List;

import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.plaidapp.R;
import io.plaidapp.data.PlaidItem;
import io.plaidapp.data.SearchDataManager;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.pocket.PocketUtils;
import io.plaidapp.util.ShortcutHelper;
import io.plaidapp.ui.recyclerview.InfiniteScrollListener;
import io.plaidapp.ui.recyclerview.SlideInItemAnimator;
import io.plaidapp.ui.transitions.CircularReveal;
import io.plaidapp.util.ImeUtils;
import io.plaidapp.util.TransitionUtils;

public class SearchActivity extends Activity {

    public static final String EXTRA_QUERY = "EXTRA_QUERY";
    public static final String EXTRA_SAVE_DRIBBBLE = "EXTRA_SAVE_DRIBBBLE";
    public static final String EXTRA_SAVE_DESIGNER_NEWS = "EXTRA_SAVE_DESIGNER_NEWS";
    public static final int RESULT_CODE_SAVE = 7;

    @BindView(R.id.searchback) ImageButton searchBack;
    @BindView(R.id.searchback_container) ViewGroup searchBackContainer;
    @BindView(R.id.search_view) SearchView searchView;
    @BindView(R.id.search_background) View searchBackground;
    @BindView(android.R.id.empty) ProgressBar progress;
    @BindView(R.id.search_results) RecyclerView results;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.search_toolbar) ViewGroup searchToolbar;
    @BindView(R.id.results_container) ViewGroup resultsContainer;
    @BindView(R.id.fab) ImageButton fab;
    @BindView(R.id.confirm_save_container) ViewGroup confirmSaveContainer;
    @BindView(R.id.save_dribbble) CheckedTextView saveDribbble;
    @BindView(R.id.save_designer_news) CheckedTextView saveDesignerNews;
    @BindView(R.id.scrim) View scrim;
    @BindView(R.id.results_scrim) View resultsScrim;
    @BindInt(R.integer.num_columns) int columns;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;
    SearchDataManager dataManager;
    FeedAdapter adapter;
    private TextView noResults;
    private SparseArray<Transition> transitions = new SparseArray<>();
    private boolean focusQuery = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setupSearchView();

        dataManager = new SearchDataManager(this) {
            @Override
            public void onDataLoaded(List<? extends PlaidItem> data) {
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
                            container, getTransition(R.transition.auto));
                    progress.setVisibility(View.GONE);
                    setNoResultsVisibility(View.VISIBLE);
                }
            }
        };
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

    @OnClick({ R.id.scrim, R.id.searchback })
    protected void dismiss() {
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.setBackground(null);
        finishAfterTransition();
    }

    @OnClick(R.id.fab)
    protected void save() {
        // show the save confirmation bubble
        TransitionManager.beginDelayedTransition(
                resultsContainer, getTransition(R.transition.search_show_confirm));
        fab.setVisibility(View.INVISIBLE);
        confirmSaveContainer.setVisibility(View.VISIBLE);
        resultsScrim.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.save_confirmed)
    protected void doSave() {
        Intent saveData = new Intent();
        saveData.putExtra(EXTRA_QUERY, dataManager.getQuery());
        saveData.putExtra(EXTRA_SAVE_DRIBBBLE, saveDribbble.isChecked());
        saveData.putExtra(EXTRA_SAVE_DESIGNER_NEWS, saveDesignerNews.isChecked());
        setResult(RESULT_CODE_SAVE, saveData);
        dismiss();
    }

    @OnClick(R.id.results_scrim)
    protected void hideSaveConfirmation() {
        if (confirmSaveContainer.getVisibility() == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                    resultsContainer, getTransition(R.transition.search_hide_confirm));
            confirmSaveContainer.setVisibility(View.GONE);
            resultsScrim.setVisibility(View.GONE);
            fab.setVisibility(results.getVisibility());
        }
    }

    @OnClick({ R.id.save_dribbble, R.id.save_designer_news })
    protected void toggleSaveCheck(CheckedTextView ctv) {
        ctv.toggle();
    }

    void clearResults() {
        TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
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
