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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.plaidapp.core.data.prefs.SourceManager;
import io.plaidapp.core.designernews.Injection;
import io.plaidapp.core.designernews.data.stories.StoriesRepository;
import io.plaidapp.core.dribbble.DribbbleInjection;
import io.plaidapp.core.dribbble.data.DribbbleRepository;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import io.plaidapp.core.producthunt.data.api.ProductHuntInjection;
import io.plaidapp.core.producthunt.data.api.ProductHuntRepository;
import io.plaidapp.core.ui.FilterAdapter;
import kotlin.Unit;
import retrofit2.Call;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class DataManager extends BaseDataManager<List<? extends PlaidItem>>
        implements LoadSourceCallback {

    private final DribbbleRepository dribbbleRepository;
    final StoriesRepository storiesRepository;
    private final ProductHuntRepository productHuntRepository;
    private final FilterAdapter filterAdapter;
    Map<String, Integer> pageIndexes;
    Map<String, Call> inflightCalls;

    public DataManager(Context context, FilterAdapter filterAdapter) {
        super();
        dribbbleRepository = DribbbleInjection.provideDribbbleRepository();
        storiesRepository = Injection.provideStoriesRepository(context);
        productHuntRepository = ProductHuntInjection.provideProductHuntRepository();

        this.filterAdapter = filterAdapter;
        filterAdapter.registerFilterChangedCallback(filterListener);
        setupPageIndexes();
        inflightCalls = new HashMap<>();
    }

    public void loadAllDataSources() {
        for (Source filter : filterAdapter.getFilters()) {
            loadSource(filter);
        }
    }

    @Override
    public void cancelLoading() {
        if (inflightCalls.size() > 0) {
            for (Call call : inflightCalls.values()) {
                call.cancel();
            }
            inflightCalls.clear();
        }
        dribbbleRepository.cancelAllSearches();
        storiesRepository.cancelAllRequests();
        productHuntRepository.cancelAllRequests();
    }

    private final FilterAdapter.FiltersChangedCallbacks filterListener =
            new FilterAdapter.FiltersChangedCallbacks() {
                @Override
                public void onFiltersChanged(Source changedFilter) {
                    if (changedFilter.active) {
                        loadSource(changedFilter);
                    } else { // filter deactivated
                        final String key = changedFilter.key;
                        if (inflightCalls.containsKey(key)) {
                            final Call call = inflightCalls.get(key);
                            if (call != null) call.cancel();
                            inflightCalls.remove(key);
                        }
                        storiesRepository.cancelRequestOfSource(key);
                        // clear the page index for the source
                        pageIndexes.put(key, 0);
                    }
                }
            };

    void loadSource(Source source) {
        if (source.active) {
            loadStarted();
            final int page = getNextPageIndex(source.key);
            switch (source.key) {
                case SourceManager.SOURCE_DESIGNER_NEWS_POPULAR:
                    loadDesignerNewsTopStories(page);
                    break;
                case SourceManager.SOURCE_DESIGNER_NEWS_RECENT:
                    loadDesignerNewsRecent(page);
                    break;
                case SourceManager.SOURCE_PRODUCT_HUNT:
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
        final List<Source> dateSources = filterAdapter.getFilters();
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

    private void loadDesignerNewsTopStories(final int page) {
        storiesRepository.loadTopStories(page, this);
    }

    private void loadDesignerNewsRecent(final int page) {
        storiesRepository.loadRecent(page, this);
    }

    private void loadDesignerNewsSearch(final Source.DesignerNewsSearchSource source,
                                        final int page) {
        storiesRepository.search(source.key, page, this);
    }

    private void loadDribbbleSearch(final Source.DribbbleSearchSource source, final int page) {
        dribbbleRepository.search(source.query, page, result -> {
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
                    sourceLoaded(it, page, SourceManager.SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                },
                error -> {
                    loadFailed(SourceManager.SOURCE_PRODUCT_HUNT);
                    return Unit.INSTANCE;
                });
    }
}
