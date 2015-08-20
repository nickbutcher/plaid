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

package com.example.android.plaid.data;

import android.support.annotation.DrawableRes;

import com.example.android.plaid.R;

import java.util.Comparator;

/**
 * Representation of a data source
 */
public class Source {

    public final String key;
    public final @DrawableRes int sortOrder;
    public final String name;
    public final int res;
    public boolean active;

    public Source(String key,
                  int sortOrder,
                  String name,
                  @DrawableRes int iconResId,
                  boolean active) {
        this.key = key;
        this.sortOrder = sortOrder;
        this.name = name;
        this.res = iconResId;
        this.active = active;
    }

    public static class DribbbleSource extends Source {

        public DribbbleSource(String key,
                              int sortOrder,
                              String name,
                              boolean active) {
            super(key, sortOrder, name, R.drawable.ic_dribbble, active);
        }
    }

    public static class DribbbleSearchSource extends DribbbleSource {

        private static final int SEARCH_SORT_ORDER = 300;

        public final String query;

        public DribbbleSearchSource(String key,
                                    String query,
                                    boolean active) {
            super(key, SEARCH_SORT_ORDER, "“" + query + "”", active);
            this.query = query;
        }
    }

    public static class DesignerNewsSource extends Source {

        public DesignerNewsSource(String key,
                                  int sortOrder,
                                  String name,
                                  boolean active) {
            super(key, sortOrder, name, R.drawable.ic_designer_news, active);
        }
    }

    public static class SourceComparator implements Comparator<Source> {

        @Override
        public int compare(Source lhs, Source rhs) {
            return lhs.sortOrder - rhs.sortOrder;
        }
    }
}


