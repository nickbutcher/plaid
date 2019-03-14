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

package io.plaidapp.core.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.plaidapp.core.data.prefs.SourcesRepository;
import io.plaidapp.core.designernews.data.DesignerNewsSearchSource;
import io.plaidapp.core.designernews.data.stories.model.Story;
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase;
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase;
import io.plaidapp.core.dribbble.data.DribbbleSourceItem;
import io.plaidapp.core.dribbble.data.ShotsRepository;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.producthunt.domain.LoadPostsUseCase;
import io.plaidapp.core.ui.filter.FiltersChangedCallback;
import kotlin.Unit;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.plaidapp.core.designernews.data.DesignerNewsSearchSource.SOURCE_DESIGNER_NEWS_POPULAR;
import static io.plaidapp.core.producthunt.data.ProductHuntSourceItem.SOURCE_PRODUCT_HUNT;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public class DataManager implements DataLoadingSubject {

    private final AtomicInteger loadingCount = new AtomicInteger(0);
    private List<DataLoadingCallbacks> loadingCallbacks;
    private OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback;

    private final ShotsRepository shotsRepository;
    private final LoadStoriesUseCase loadStoriesUseCase;
    private final SearchStoriesUseCase searchStoriesUseCase;
    private final LoadPostsUseCase loadPosts;
    private final SourcesRepository sourcesRepository;
    private Map<String, Integer> pageIndexes;
    private Map<String, Call> inflightCalls = new HashMap<>();

    public DataManager(LoadStoriesUseCase loadStoriesUseCase,
                       LoadPostsUseCase loadPosts,
                       SearchStoriesUseCase searchStoriesUseCase,
                       ShotsRepository shotsRepository,
                       SourcesRepository sourcesRepository) {
        super();
        this.loadStoriesUseCase = loadStoriesUseCase;
        this.loadPosts = loadPosts;
        this.searchStoriesUseCase = searchStoriesUseCase;
        this.shotsRepository = shotsRepository;
        this.sourcesRepository = sourcesRepository;
        setOnDataLoadedCallback(onDataLoadedCallback);

        this.sourcesRepository.registerFilterChangedCallback(filterListener);
        setupPageIndexes();
    }

    public void setOnDataLoadedCallback(
            OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback) {
        this.onDataLoadedCallback = onDataLoadedCallback;
    }

    private void onDataLoaded(List<? extends PlaidItem> data) {
        if (onDataLoadedCallback != null) {
            onDataLoadedCallback.onDataLoaded(data);
        }
    }

    public void loadAllDataSources() {
        for (SourceItem filter : sourcesRepository.getSourcesSync()) {
            loadSource(filter);
        }
    }

    public void cancelLoading() {
        if (inflightCalls.size() > 0) {
            for (Call call : inflightCalls.values()) {
                call.cancel();
            }
            inflightCalls.clear();
        }
        shotsRepository.cancelAllSearches();
        loadStoriesUseCase.cancelAllRequests();
        searchStoriesUseCase.cancelAllRequests();
        loadPosts.cancelAllRequests();
    }

    private final FiltersChangedCallback filterListener = new FiltersChangedCallback() {
        @Override
        public void onFiltersChanged(SourceItem changedFilter) {
            if (changedFilter.getActive()) {
                loadSource(changedFilter);
            } else { // filter deactivated
                final String key = changedFilter.getKey();
                if (inflightCalls.containsKey(key)) {
                    final Call call = inflightCalls.get(key);
                    if (call != null) call.cancel();
                    inflightCalls.remove(key);
                }
                loadStoriesUseCase.cancelRequestOfSource(key);
                searchStoriesUseCase.cancelRequestOfSource(key);
                // clear the page index for the source
                pageIndexes.put(key, 0);
            }
        }
    };

    private void loadSource(SourceItem source) {
        if (source.getActive()) {
            loadStarted();
            final int page = getNextPageIndex(source.getKey());
            switch (source.getKey()) {
                case SOURCE_DESIGNER_NEWS_POPULAR:
                    loadDesignerNewsStories(page);
                    break;
                case SOURCE_PRODUCT_HUNT:
                    loadProductHunt(page);
                    break;
                default:
                    if (source instanceof DribbbleSourceItem) {
                        loadDribbbleSearch((DribbbleSourceItem) source, page);
                    } else if (source instanceof DesignerNewsSearchSource) {
                        loadDesignerNewsSearch((DesignerNewsSearchSource) source, page);
                    }
                    break;
            }
        }
    }

    private void setupPageIndexes() {
        final List<SourceItem> dateSources = sourcesRepository.getSourcesSync();
        pageIndexes = new HashMap<>(dateSources.size());
        for (SourceItem source : dateSources) {
            pageIndexes.put(source.getKey(), 0);
        }
    }

    private int getNextPageIndex(String dataSource) {
        int nextPage = 1; // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes.get(dataSource) + 1;
        }
        pageIndexes.put(dataSource, nextPage);
        return nextPage;
    }

    private boolean sourceIsEnabled(String key) {
        return pageIndexes.get(key) != 0;
    }

    private void sourceLoaded(@Nullable List<? extends PlaidItem> data, int page,
                              @NonNull String source) {
        loadFinished();
        if (data != null && !data.isEmpty() && sourceIsEnabled(source)) {
            setPage(data, page);
            setDataSource(data, source);
            onDataLoaded(data);
        }
        inflightCalls.remove(source);
    }

    private void loadFailed(@NonNull String source) {
        loadFinished();
        inflightCalls.remove(source);
    }

    private void loadDesignerNewsStories(final int page) {
        loadStoriesUseCase.invoke(page, (result, pageResult, source) -> {
            if (result instanceof Result.Success) {
                sourceLoaded(((Result.Success<List<Story>>) result).getData(), page, source);
            } else {
                loadFailed(source);
            }
            return Unit.INSTANCE;
        });
    }

    private void loadDesignerNewsSearch(final DesignerNewsSearchSource source,
                                        final int page) {
        searchStoriesUseCase.invoke(source.getKey(), page, (result, pageResult, sourceResult) -> {
            if (result instanceof Result.Success) {
                sourceLoaded(((Result.Success<List<Story>>) result).getData(), page, source.getKey());
            } else {
                loadFailed(source.getKey());
            }
            return Unit.INSTANCE;
        });
    }

    private void loadDribbbleSearch(final DribbbleSourceItem source, final int page) {
        shotsRepository.search(source.getQuery(), page, result -> {
            if (result instanceof Result.Success) {
                Result.Success<List<Shot>> success = (Result.Success<List<Shot>>) result;
                sourceLoaded(success.getData(), page, source.getKey());
            } else {
                loadFailed(source.getKey());
            }
            return Unit.INSTANCE;
        });
    }

    private void loadProductHunt(final int page) {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        loadPosts.invoke(
                page - 1,
                it -> {
                    sourceLoaded(it, page, SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                },
                error -> {
                    loadFailed(SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                });
    }

    @Override
    public void registerCallback(DataLoadingSubject.DataLoadingCallbacks callback) {
        if (loadingCallbacks == null) {
            loadingCallbacks = new ArrayList<>(1);
        }
        loadingCallbacks.add(callback);
    }

    private void loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            dispatchLoadingStartedCallbacks();
        }
    }

    private void loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            dispatchLoadingFinishedCallbacks();
        }
    }

    private static void setPage(List<? extends PlaidItem> items, int page) {
        for (PlaidItem item : items) {
            item.setPage(page);
        }
    }

    private static void setDataSource(List<? extends PlaidItem> items, String dataSource) {
        for (PlaidItem item : items) {
            item.setDataSource(dataSource);
        }
    }

    private void dispatchLoadingStartedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataStartedLoading();
        }
    }

    private void dispatchLoadingFinishedCallbacks() {
        if (loadingCallbacks == null || loadingCallbacks.isEmpty()) return;
        for (DataLoadingCallbacks loadingCallback : loadingCallbacks) {
            loadingCallback.dataFinishedLoading();
        }
    }
}
