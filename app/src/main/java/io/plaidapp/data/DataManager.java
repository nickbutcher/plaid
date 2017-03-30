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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.plaidapp.data.api.designernews.model.Story;
import io.plaidapp.data.api.dribbble.DribbbleSearchService;
import io.plaidapp.data.api.dribbble.DribbbleService;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import io.plaidapp.data.api.producthunt.model.Post;
import io.plaidapp.data.prefs.SourceManager;
import io.plaidapp.ui.FilterAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
public abstract class DataManager extends BaseDataManager<List<? extends PlaidItem>> {

    private final FilterAdapter filterAdapter;
    private Map<String, Integer> pageIndexes;
    private Map<String, Call> inflight;

    public DataManager(Context context,
                       FilterAdapter filterAdapter) {
        super(context);
        this.filterAdapter = filterAdapter;
        filterAdapter.registerFilterChangedCallback(filterListener);
        setupPageIndexes();
        inflight = new HashMap<>();
    }

    public void loadAllDataSources() {
        for (Source filter : filterAdapter.getFilters()) {
            loadSource(filter);
        }
    }

    @Override
    public void cancelLoading() {
        if (inflight.size() > 0) {
            for (Call call : inflight.values()) {
                call.cancel();
            }
            inflight.clear();
        }
    }

    private final FilterAdapter.FiltersChangedCallbacks filterListener =
            new FilterAdapter.FiltersChangedCallbacks() {
        @Override
        public void onFiltersChanged(Source changedFilter) {
            if (changedFilter.active) {
                loadSource(changedFilter);
            } else { // filter deactivated
                final String key = changedFilter.key;
                if (inflight.containsKey(key)) {
                    final Call call = inflight.get(key);
                    if (call != null) call.cancel();
                    inflight.remove(key);
                }
                // clear the page index for the source
                pageIndexes.put(key, 0);
            }
        }
    };

    private void loadSource(Source source) {
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

    private void sourceLoaded(List<? extends PlaidItem> data, int page, String key) {
        loadFinished();
        if (data != null && !data.isEmpty() && sourceIsEnabled(key)) {
            setPage(data, page);
            setDataSource(data, key);
            onDataLoaded(data);
        }
        inflight.remove(key);
    }

    private void loadFailed(String key) {
        loadFinished();
        inflight.remove(key);
    }

    private void loadDesignerNewsTopStories(final int page) {
        final Call<List<Story>> topStories = getDesignerNewsApi().getTopStories(page);
        topStories.enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DESIGNER_NEWS_POPULAR);
                } else {
                    loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR);
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR);
            }
        });
        inflight.put(SourceManager.SOURCE_DESIGNER_NEWS_POPULAR, topStories);
    }

    private void loadDesignerNewsRecent(final int page) {
        final Call<List<Story>> recentStoriesCall = getDesignerNewsApi().getRecentStories(page);
        recentStoriesCall.enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DESIGNER_NEWS_RECENT);
                } else {
                    loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_RECENT);
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DESIGNER_NEWS_RECENT);
            }
        });
        inflight.put(SourceManager.SOURCE_DESIGNER_NEWS_RECENT, recentStoriesCall);
    }

    private void loadDesignerNewsSearch(final Source.DesignerNewsSearchSource source,
                                        final int page) {
        final Call<List<Story>> searchCall = getDesignerNewsApi().search(source.query, page);
        searchCall.enqueue(new Callback<List<Story>>() {
            @Override
            public void onResponse(Call<List<Story>> call, Response<List<Story>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, source.key);
                } else {
                    loadFailed(source.key);
                }
            }

            @Override
            public void onFailure(Call<List<Story>> call, Throwable t) {
                loadFailed(source.key);
            }
        });
        inflight.put(source.key, searchCall);
    }

    private void loadDribbblePopular(final int page) {
        final Call<List<Shot>> popularCall = getDribbbleApi()
                .getPopular(page, DribbbleService.PER_PAGE_DEFAULT);
        popularCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DRIBBBLE_POPULAR);
                } else {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_POPULAR);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DRIBBBLE_POPULAR);
            }
        });
        inflight.put(SourceManager.SOURCE_DRIBBBLE_POPULAR, popularCall);
    }

    private void loadDribbbleDebuts(final int page) {
        final Call<List<Shot>> debutsCall = getDribbbleApi()
                .getDebuts(page, DribbbleService.PER_PAGE_DEFAULT);
        debutsCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DRIBBBLE_DEBUTS);
                } else {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_DEBUTS);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DRIBBBLE_DEBUTS);
            }
        });
        inflight.put(SourceManager.SOURCE_DRIBBBLE_DEBUTS, debutsCall);
    }

    private void loadDribbbleAnimated(final int page) {
        final Call<List<Shot>> animatedCall = getDribbbleApi()
                .getAnimated(page, DribbbleService.PER_PAGE_DEFAULT);
        animatedCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DRIBBBLE_ANIMATED);
                } else {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_ANIMATED);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DRIBBBLE_ANIMATED);
            }
        });
        inflight.put(SourceManager.SOURCE_DRIBBBLE_ANIMATED, animatedCall);
    }

    private void loadDribbbleRecent(final int page) {
        final Call<List<Shot>> recentCall = getDribbbleApi()
                .getRecent(page, DribbbleService.PER_PAGE_DEFAULT);
        recentCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DRIBBBLE_RECENT);
                } else {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_RECENT);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DRIBBBLE_RECENT);
            }
        });
        inflight.put(SourceManager.SOURCE_DRIBBBLE_RECENT, recentCall);
    }

    private void loadDribbbleFollowing(final int page) {
        final Call<List<Shot>> followingCall = getDribbbleApi()
                .getFollowing(page, DribbbleService.PER_PAGE_DEFAULT);
        followingCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_DRIBBBLE_FOLLOWING);
                } else {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_FOLLOWING);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_DRIBBBLE_FOLLOWING);
            }
        });
        inflight.put(SourceManager.SOURCE_DRIBBBLE_FOLLOWING, followingCall);
    }

    private void loadDribbbleUserLikes(final int page) {
        if (getDribbblePrefs().isLoggedIn()) {
            final Call<List<Like>> userLikesCall = getDribbbleApi()
                    .getUserLikes(page, DribbbleService.PER_PAGE_DEFAULT);
            userLikesCall.enqueue(new Callback<List<Like>>() {
                @Override
                public void onResponse(Call<List<Like>> call, Response<List<Like>> response) {
                    if (response.isSuccessful()) {
                        // API returns Likes but we just want the Shots
                        final List<Like> likes = response.body();
                        List<Shot> likedShots = null;
                        if (likes != null && !likes.isEmpty()) {
                            likedShots = new ArrayList<>(likes.size());
                            for (Like like : likes) {
                                likedShots.add(like.shot);
                            }
                        }
                        sourceLoaded(likedShots, page, SourceManager.SOURCE_DRIBBBLE_USER_LIKES);
                    } else {
                        loadFailed(SourceManager.SOURCE_DRIBBBLE_USER_LIKES);
                    }
                }

                @Override
                public void onFailure(Call<List<Like>> call, Throwable t) {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_USER_LIKES);
                }
            });
            inflight.put(SourceManager.SOURCE_DRIBBBLE_USER_LIKES, userLikesCall);
        } else {
            loadFinished();
        }
    }

    private void loadDribbbleUserShots(final int page) {
        if (getDribbblePrefs().isLoggedIn()) {
            final Call<List<Shot>> userShotsCall = getDribbbleApi()
                    .getUserShots(page, DribbbleService.PER_PAGE_DEFAULT);
            userShotsCall.enqueue(new Callback<List<Shot>>() {
                @Override
                public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                    if (response.isSuccessful()) {
                        loadFinished();
                        final List<Shot> shots = response.body();
                        if (shots != null && !shots.isEmpty()) {
                            // this api call doesn't populate the shot user field but we need it
                            final User user = getDribbblePrefs().getUser();
                            for (Shot shot : shots) {
                                shot.user = user;
                            }
                        }
                        sourceLoaded(shots, page, SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
                    } else {
                        loadFailed(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
                    }
                }

                @Override
                public void onFailure(Call<List<Shot>> call, Throwable t) {
                    loadFailed(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS);
                }
            });
            inflight.put(SourceManager.SOURCE_DRIBBBLE_USER_SHOTS, userShotsCall);
        } else {
            loadFinished();
        }
    }


    private void loadDribbbleSearch(final Source.DribbbleSearchSource source, final int page) {
        final Call<List<Shot>> searchCall = getDribbbleSearchApi().search(source.query, page,
                DribbbleSearchService.PER_PAGE_DEFAULT, DribbbleSearchService.SORT_RECENT);
        searchCall.enqueue(new Callback<List<Shot>>() {
            @Override
            public void onResponse(Call<List<Shot>> call, Response<List<Shot>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, source.key);
                } else {
                    loadFailed(source.key);
                }
            }

            @Override
            public void onFailure(Call<List<Shot>> call, Throwable t) {
                loadFailed(source.key);
            }
        });
        inflight.put(source.key, searchCall);
    }

    private void loadProductHunt(final int page) {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        final Call<List<Post>> postsCall = getProductHuntApi().getPosts(page - 1);
        postsCall.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    sourceLoaded(response.body(), page, SourceManager.SOURCE_PRODUCT_HUNT);
                } else {
                    loadFailed(SourceManager.SOURCE_PRODUCT_HUNT);
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                loadFailed(SourceManager.SOURCE_PRODUCT_HUNT);
            }
        });
        inflight.put(SourceManager.SOURCE_PRODUCT_HUNT, postsCall);
    }
}
