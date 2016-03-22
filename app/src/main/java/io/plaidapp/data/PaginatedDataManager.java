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

/**
 * Load a paginated data source. Instantiating classes are responsible for providing implementations
 * of {@link #loadData(int)} to actually load the data, and {@link #onDataLoaded} to do something
 * with it.
 */
public abstract class PaginatedDataManager<T> extends BaseDataManager<T> {

    // state
    private int page = 0;
    protected boolean moreDataAvailable = true;

    public PaginatedDataManager(Context context) {
        super(context);
    }

    public void loadData() {
        if (!moreDataAvailable) return;
        page++;
        loadStarted();
        loadData(page);
    }

    /**
     * Extending classes must provide this method to actually load data. They <bold>must</bold> call
     * {@link #loadFinished()} when finished.
     */
    protected abstract void loadData(int page);

}
