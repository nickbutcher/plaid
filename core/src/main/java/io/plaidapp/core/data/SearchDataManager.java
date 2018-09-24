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
import io.plaidapp.core.designernews.Injection;
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase;
import io.plaidapp.core.dribbble.data.ShotsRepository;
import io.plaidapp.core.dribbble.data.api.model.Shot;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;

/**
 * Responsible for loading search results from dribbble and designer news. Instantiating classes are
 * responsible for providing the {code onDataLoaded} method to do something with the data.
 */
public class SearchDataManager extends BaseDataManager<List<? extends PlaidItem>>
        implements LoadSourceCallback {

    private final SearchStoriesUseCase searchStoriesUseCase;
    @Inject ShotsRepository shotsRepository;

    // state
    private String query = "";
    private int page = 1;

    @Inject public SearchDataManager(Context context,
                                     OnDataLoadedCallback<List<? extends PlaidItem>> onDataLoadedCallback) {
        super();
        setOnDataLoadedCallback(onDataLoadedCallback);
        searchStoriesUseCase = Injection.provideSearchStoriesUseCase(context);
    }

    public void searchFor(String query) {
        if (!this.query.equals(query)) {
            clear();
            this.query = query;
        } else {
            page++;
        }
        searchDribbble(query, page);
        searchDesignerNews(query, page);
    }

    public void loadMore() {
        searchFor(query);
    }

    public void clear() {
        cancelLoading();
        query = "";
        page = 1;
        resetLoadingCount();
    }

    @Override
    public void cancelLoading() {
        searchStoriesUseCase.cancelAllRequests();
        shotsRepository.cancelAllSearches();
    }

    public String getQuery() {
        return query;
    }

    private void searchDesignerNews(final String query, final int resultsPage) {
        loadStarted();
        String source = Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX + query;
        searchStoriesUseCase.invoke(source, resultsPage, this);
    }

    private void searchDribbble(final String query, final int resultsPage) {
        loadStarted();
        shotsRepository.search(query, page, result -> {
            loadFinished();
            if (result instanceof Result.Success) {
                List<Shot> shots = ((Result.Success<? extends List<Shot>>) result).getData();
                setPage(shots, resultsPage);
                setDataSource(shots,
                        Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX + query);
                onDataLoaded(shots);
            }
            return Unit.INSTANCE;
        });

    }

    @Override
    public void sourceLoaded(@Nullable List<? extends PlaidItem> result, int page,
                             @NotNull String source) {
        loadFinished();
        if (result != null) {
            setPage(result, page);
            setDataSource(result,
                    Source.DesignerNewsSearchSource.DESIGNER_NEWS_QUERY_PREFIX + query);
            onDataLoaded(result);
        }
    }

    @Override
    public void loadFailed(@NotNull String source) {
        loadFinished();
    }

}
