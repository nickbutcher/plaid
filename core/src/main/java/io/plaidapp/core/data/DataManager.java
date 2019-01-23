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
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase;
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase;
import io.plaidapp.core.dribbble.data.ShotsRepository;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository;
import io.plaidapp.core.ui.filter.FiltersChangedCallback;
import kotlin.Unit;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public class DataManager implements LoadSourceCallback, DataLoadingSubject {

    private final AtomicInteger loadingCount = new AtomicInteger(0);
    private List<DataLoadingCallbacks> loadingCallbacks;
    private OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback;

    private final ShotsRepository shotsRepository;
    private final LoadStoriesUseCase loadStoriesUseCase;
    private final SearchStoriesUseCase searchStoriesUseCase;
    private final ProductHuntRepository productHuntRepository;
    private final SourcesRepository sourcesRepository;
    private Map<String, Integer> pageIndexes;
    private Map<String, Call> inflightCalls = new HashMap<>();

    public DataManager(OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback,
                       LoadStoriesUseCase loadStoriesUseCase,
                       ProductHuntRepository productHuntRepository,
                       SearchStoriesUseCase searchStoriesUseCase,
                       ShotsRepository shotsRepository,
                       SourcesRepository sourcesRepository) {
        super();
        this.loadStoriesUseCase = loadStoriesUseCase;
        this.productHuntRepository = productHuntRepository;
        this.searchStoriesUseCase = searchStoriesUseCase;
        this.shotsRepository = shotsRepository;
        this.sourcesRepository = sourcesRepository;
        setOnDataLoadedCallback(onDataLoadedCallback);

        this.sourcesRepository.registerFilterChangedCallback(filterListener);
        setupPageIndexes();
    }

    private void setOnDataLoadedCallback(
            OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback) {
        this.onDataLoadedCallback = onDataLoadedCallback;
    }

    private void onDataLoaded(List<? extends PlaidItem> data) {
        onDataLoadedCallback.onDataLoaded(data);
    }

    public void loadAllDataSources() {
        for (Source filter : sourcesRepository.getSources()) {
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
        productHuntRepository.cancelAllRequests();
    }

    private final FiltersChangedCallback filterListener = new FiltersChangedCallback() {
                @Override
                public void onFiltersChanged(Source changedFilter) {
                    if (changedFilter.getActive()) {
                        loadSource(changedFilter);
                    } else { // filter deactivated
                        final String key = changedFilter.key;
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

    private void loadSource(Source source) {
        if (source.getActive()) {
            loadStarted();
            final int page = getNextPageIndex(source.key);
            switch (source.key) {
                case SourcesRepository.SOURCE_DESIGNER_NEWS_POPULAR:
                    loadDesignerNewsStories(page);
                    break;
                case SourcesRepository.SOURCE_PRODUCT_HUNT:
                    loadProductHunt(page);
                    break;
                default:
                    if (source instanceof Source.DribbbleSearchSource) {
                        loadDribbbleSearch((Source.DribbbleSearchSource) source, page);
                    } else if (source instanceof Source.DesignerNewsSearchSource) {
                        loadDesignerNewsSearch((Source.DesignerNewsSearchSource) source, page);
                    }
                    break;
            }
        }
    }

    private void setupPageIndexes() {
        final List<Source> dateSources = sourcesRepository.getSources();
        pageIndexes = new HashMap<>(dateSources.size());
        for (Source source : dateSources) {
            pageIndexes.put(source.key, 0);
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

    @Override
    public void sourceLoaded(@Nullable List<? extends PlaidItem> data, int page,
                             @NonNull String source) {
        loadFinished();
        if (data != null && !data.isEmpty() && sourceIsEnabled(source)) {
            setPage(data, page);
            setDataSource(data, source);
            onDataLoaded(data);
        }
        inflightCalls.remove(source);
    }

    @Override
    public void loadFailed(@NonNull String source) {
        loadFinished();
        inflightCalls.remove(source);
    }

    private void loadDesignerNewsStories(final int page) {
        loadStoriesUseCase.invoke(page, this);
    }

    private void loadDesignerNewsSearch(final Source.DesignerNewsSearchSource source,
                                        final int page) {
        searchStoriesUseCase.invoke(source.key, page, this);
    }

    private void loadDribbbleSearch(final Source.DribbbleSearchSource source, final int page) {
        shotsRepository.search(source.query, page, result -> {
            if (result instanceof Result.Success) {
                Result.Success<List<Shot>> success = (Result.Success<List<Shot>>) result;
                sourceLoaded(success.getData(), page, source.key);
            } else {
                loadFailed(source.key);
            }
            return Unit.INSTANCE;
        });
    }

    private void loadProductHunt(final int page) {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        productHuntRepository.loadProductHuntData(
                page - 1,
                it -> {
                    sourceLoaded(it, page, SourcesRepository.SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                },
                error -> {
                    loadFailed(SourcesRepository.SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                });
    }

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
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
