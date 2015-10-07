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

import android.support.annotation.DrawableRes;

import java.util.Comparator;

import io.plaidapp.R;

/**
 * Representation of a data source
 */
public class Source {

    public final String key;
    public final int sortOrder;
    public final String name;
    public final @DrawableRes int iconRes;
    public boolean active;

    public Source(String key,
                  int sortOrder,
                  String name,
                  @DrawableRes int iconResId,
                  boolean active) {
        this.key = key;
        this.sortOrder = sortOrder;
        this.name = name;
        this.iconRes = iconResId;
        this.active = active;
    }

    public boolean isSwipeDismissable() {
        return false;
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

        public static final String DRIBBBLE_QUERY_PREFIX = "DRIBBBLE_QUERY_";
        private static final int SEARCH_SORT_ORDER = 400;

        public final String query;

        public DribbbleSearchSource(String query,
                                    boolean active) {
            super(DRIBBBLE_QUERY_PREFIX + query, SEARCH_SORT_ORDER, "“" + query + "”", active);
            this.query = query;
        }

        @Override
        public boolean isSwipeDismissable() {
            return true;
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

    public static class DesignerNewsSearchSource extends DesignerNewsSource {

        public static final String DESIGNER_NEWS_QUERY_PREFIX = "DESIGNER_NEWS_QUERY_";
        private static final int SEARCH_SORT_ORDER = 200;

        public final String query;

        public DesignerNewsSearchSource(String query,
                                        boolean active) {
            super(DESIGNER_NEWS_QUERY_PREFIX + query, SEARCH_SORT_ORDER, "“" + query + "”", active);
            this.query = query;
        }

        @Override
        public boolean isSwipeDismissable() {
            return true;
        }
    }

    public static class SourceComparator implements Comparator<Source> {

        @Override
        public int compare(Source lhs, Source rhs) {
            return lhs.sortOrder - rhs.sortOrder;
        }
    }
}


