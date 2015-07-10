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

package io.plaidapp.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.plaidapp.data.api.designernews.model.StoriesResponse;
import io.plaidapp.data.api.dribbble.DribbbleSearch;
import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import io.plaidapp.data.api.producthunt.model.PostsResponse;
import io.plaidapp.data.prefs.SourceManager;
import io.plaidapp.ui.FilterAdapter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class DataManager extends BaseDataManager
        implements FilterAdapter.FiltersChangedListener, DataLoadingSubject {

    private final FilterAdapter filterAdapter;
    private AtomicInteger loadingCount;
    private Map<String, Integer> pageIndexes;

    /**
     * @param filterAdapter
     */
    public DataManager(Context context,
                       FilterAdapter filterAdapter) {
        super(context);
        this.filterAdapter = filterAdapter;
        loadingCount = new AtomicInteger(0);
        setupPageIndexes();
    }

    public void loadAllDataSources() {
        for (Source filter : filterAdapter.getFilters()) {
            loadSource(filter);
        }
    }

    @Override
    public boolean isDataLoading() {
        return loadingCount.get() > 0;
    }

    @Override
    public void onFiltersChanged(Source changedFilter){
        if (changedFilter.active) {
            loadSource(changedFilter);
        } else {
            // clear the page index for the source
            pageIndexes.put(changedFilter.key, 0);
        }
    }

    @Override
    public void onFilterRemoved(Source removed) { } // no-op

    private void loadSource(Source source) {
        if (source.active) {
            loadingCount.incrementAndGet();
            int page = getNextPageIndex(source.key);
            switch (source.key) {
                case SourceManager.SOURCE_DESIGNER_NEWS_POPULAR:
                    loadDesignerNewsTopStories(page);
                    break;
                case SourceManager.SOURCE_DESIGNER_NEWS_RECENT:
                    loadDesignerNewsRecent(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_POPULAR:
                    loadDribbblePopular(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_FOLLOWING:
                    loadDribbbleFollowing(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_USER_LIKES:
                    loadDribbbleUserLikes(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_USER_SHOTS:
                    loadDribbbleUserShots(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_RECENT:
                    loadDribbbleRecent(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_DEBUTS:
                    loadDribbbleDebuts(page);
                    break;
                case SourceManager.SOURCE_DRIBBBLE_ANIMATED:
                    loadDribbbleAnimated(page);
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
        List<Source> dateSources = filterAdapter.getFilters();
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

    private void loadDesignerNewsTopStories(final int page) {
        getDesignerNewsApi().getTopStories(page, new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null
                        && sourceIsEnabled(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR)) {
                    setPage(storiesResponse.stories, page);
                    setDataSource(storiesResponse.stories,
                            SourceManager.SOURCE_DESIGNER_NEWS_POPULAR);
                    onDataLoaded(storiesResponse.stories);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDesignerNewsRecent(final int page) {
        getDesignerNewsApi().getRecentStories(page, new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null
                        && sourceIsEnabled(SourceManager.SOURCE_DESIGNER_NEWS_RECENT)) {
                    setPage(storiesResponse.stories, page);
                    setDataSource(storiesResponse.stories,
                            SourceManager.SOURCE_DESIGNER_NEWS_RECENT);
                    onDataLoaded(storiesResponse.stories);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDesignerNewsSearch(final Source.DesignerNewsSearchSource source,
                                        final int page) {
        getDesignerNewsApi().search(source.query, page, new Callback<StoriesResponse>() {
            @Override
            public void success(StoriesResponse storiesResponse, Response response) {
                if (storiesResponse != null) {
                    setPage(storiesResponse.stories, page);
                    setDataSource(storiesResponse.stories, source.key);
                    onDataLoaded(storiesResponse.stories);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDribbblePopular(final int page) {
        getDribbbleApi().getPopular(page, DribbbleService.PER_PAGE_DEFAULT, new
                Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_POPULAR)) {
                    setPage(shots, page);
                    setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_POPULAR);
                    onDataLoaded(shots);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDribbbleDebuts(final int page) {
        getDribbbleApi().getDebuts(page, DribbbleService.PER_PAGE_DEFAULT, new
                Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_DEBUTS)) {
                    setPage(shots, page);
                    setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_DEBUTS);
                    onDataLoaded(shots);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDribbbleAnimated(final int page) {
        getDribbbleApi().getAnimated(page, DribbbleService.PER_PAGE_DEFAULT, new
                Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_ANIMATED)) {
                    setPage(shots, page);
                    setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_ANIMATED);
                    onDataLoaded(shots);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDribbbleRecent(final int page) {
        getDribbbleApi().getRecent(page, DribbbleService.PER_PAGE_DEFAULT, new
                Callback<List<Shot>>() {
            @Override
            public void success(List<Shot> shots, Response response) {
                if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_RECENT)) {
                    setPage(shots, page);
                    setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_RECENT);
                    onDataLoaded(shots);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }

    private void loadDribbbleFollowing(final int page) {
        if (getDribbblePrefs().isLoggedIn()) {
            getDribbbleApi().getFollowing(page, DribbbleService.PER_PAGE_DEFAULT,
                    new Callback<List<Shot>>() {
                        @Override
                        public void success(List<Shot> shots, Response response) {
                            if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_FOLLOWING)) {
                                setPage(shots, page);
                                setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_FOLLOWING);
                                onDataLoaded(shots);
                            }
                            loadingCount.decrementAndGet();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            loadingCount.decrementAndGet();
                        }
                    });
        } else {
            loadingCount.decrementAndGet();
        }
    }

    private void loadDribbbleUserLikes(final int page) {
        if (getDribbblePrefs().isLoggedIn()) {
            getDribbbleApi().getUserLikes(page, DribbbleService.PER_PAGE_DEFAULT,
                    new Callback<List<Like>>() {
                        @Override
                        public void success(List<Like> likes, Response response) {
                            if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_USER_LIKES)) {
                                // API returns Likes but we just want the Shots
                                List<Shot> likedShots = new ArrayList<>(likes.size());
                                for (Like like : likes) {
                                    likedShots.add(like.shot);
                                }
                                // these will be sorted like any other shot (popularity per page)
                                // TODO figure out a more appropriate sorting strategy for likes
                                setPage(likedShots, page);
                                setDataSource(likedShots, SourceManager.SOURCE_DRIBBBLE_USER_LIKES);
                                onDataLoaded(likedShots);
                            }
                            loadingCount.decrementAndGet();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            loadingCount.decrementAndGet();
                        }
                    });
        } else {
            loadingCount.decrementAndGet();
        }
    }

    private void loadDribbbleUserShots(final int page) {
        if (getDribbblePrefs().isLoggedIn()) {
            getDribbbleApi().getUserShots(page, DribbbleService.PER_PAGE_DEFAULT,
                    new Callback<List<Shot>>() {
                        @Override
                        public void success(List<Shot> shots, Response response) {
                            if (sourceIsEnabled(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS)) {
                                // this api call doesn't populate the shot user field but we need it
                                User user = getDribbblePrefs().getUser();
                                for (Shot shot : shots) {
                                    shot.user = user;
                                }

                                setPage(shots, page);
                                setDataSource(shots, SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
                                onDataLoaded(shots);
                            }
                            loadingCount.decrementAndGet();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            loadingCount.decrementAndGet();
                        }
                    });
        } else {
            loadingCount.decrementAndGet();
        }
    }


    private void loadDribbbleSearch(final Source.DribbbleSearchSource source, final int page) {
        new AsyncTask<Void, Void, List<Shot>>() {
            @Override
            protected List<Shot> doInBackground(Void... params) {
                return DribbbleSearch.search(source.query, DribbbleSearch.SORT_RECENT, page);
            }

            @Override
            protected void onPostExecute(List<Shot> shots) {
                if (shots != null && shots.size() > 0 && sourceIsEnabled(source.key)) {
                    setPage(shots, page);
                    setDataSource(shots, source.key);
                    onDataLoaded(shots);
                }
                loadingCount.decrementAndGet();
            }
        }.execute();
    }

    private void loadProductHunt(final int page) {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        getProductHuntApi().getPosts(page - 1, new Callback<PostsResponse>() {
            @Override
            public void success(PostsResponse postsResponse, Response response) {
                if (postsResponse != null && sourceIsEnabled(SourceManager.SOURCE_PRODUCT_HUNT)) {
                    setPage(postsResponse.posts, page);
                    setDataSource(postsResponse.posts,
                            SourceManager.SOURCE_PRODUCT_HUNT);
                    onDataLoaded(postsResponse.posts);
                }
                loadingCount.decrementAndGet();
            }

            @Override
            public void failure(RetrofitError error) {
                loadingCount.decrementAndGet();
            }
        });
    }
}
