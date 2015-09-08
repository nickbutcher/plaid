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

package com.example.android.plaid.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.example.android.plaid.R;
import com.example.android.plaid.data.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manage saving and retrieving data sources from disk.
 */
public class SourceManager {

    public static final String DRIBBBLE_QUERY_PREFIX = "DRIBBBLE_QUERY_";
    public static final String SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR";
    public static final String SOURCE_DESIGNER_NEWS_RECENT = "SOURCE_DESIGNER_NEWS_RECENT";
    public static final String SOURCE_DRIBBBLE_POPULAR = "SOURCE_DRIBBBLE_POPULAR";
    public static final String SOURCE_DRIBBBLE_FOLLOWING = "SOURCE_DRIBBBLE_FOLLOWING";
    public static final String SOURCE_DRIBBBLE_USER_LIKES = "SOURCE_DRIBBBLE_USER_LIKES";
    public static final String SOURCE_DRIBBBLE_USER_SHOTS = "SOURCE_DRIBBBLE_USER_SHOTS";
    public static final String SOURCE_DRIBBBLE_RECENT = "SOURCE_DRIBBBLE_RECENT";
    public static final String SOURCE_DRIBBBLE_DEBUTS = "SOURCE_DRIBBBLE_DEBUTS";
    public static final String SOURCE_DRIBBBLE_ANIMATED = "SOURCE_DRIBBBLE_ANIMATED";
    public static final String SOURCE_DRIBBBLE_MD_SEARCH =
            DRIBBBLE_QUERY_PREFIX + "Material Design";
    public static final String SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT";
    private static final String SOURCES_PREF = "SOURCES_PREF";
    private static final String KEY_SOURCES = "KEY_SOURCES";

    public static List<Source> getSources(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE);
        Set<String> sourceKeys = prefs.getStringSet(KEY_SOURCES, null);
        if (sourceKeys == null) {
            setupDefaultSources(context, prefs.edit());
            return Arrays.asList(getDefaultSources(context));
        }

        List<Source> sources = new ArrayList<>(sourceKeys.size());
        for (String sourceKey : sourceKeys) {
            if (sourceKey.startsWith(DRIBBBLE_QUERY_PREFIX)) {
                sources.add(new Source.DribbbleSearchSource(
                        sourceKey,
                        sourceKey.replace(DRIBBBLE_QUERY_PREFIX, ""),
                        prefs.getBoolean(sourceKey, false)));
            } else {
                // TODO improve this O(n2) search
                sources.add(getSource(context, sourceKey, prefs.getBoolean(sourceKey, false)));
            }
        }
        Collections.sort(sources, new Source.SourceComparator());
        return sources;
    }

    public static void updateSource(Source source, Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE).edit();
        editor.putBoolean(source.key, source.active);
        editor.apply();
    }

    private static void setupDefaultSources(Context context, SharedPreferences.Editor editor) {
        Source[] defaultSources = getDefaultSources(context);
        Set<String> keys = new HashSet<>(defaultSources.length);
        for (Source source : defaultSources) {
            keys.add(source.key);
            editor.putBoolean(source.key, source.active);
        }
        editor.putStringSet(KEY_SOURCES, keys);
        editor.commit();
    }

    private static @Nullable Source getSource(Context context, String key, boolean active) {
        for (Source source : getDefaultSources(context)) {
            if (source.key.equals(key)) {
                source.active = active;
                return source;
            }
        }
        return null;
    }

    private static Source[] getDefaultSources(Context context) {
        return new Source[]{
                new Source.DesignerNewsSource(SOURCE_DESIGNER_NEWS_POPULAR, 100,
                        context.getString(R.string.source_designer_news_popular), true),
                new Source.DesignerNewsSource(SOURCE_DESIGNER_NEWS_RECENT, 101,
                        context.getString(R.string.source_designer_news_recent), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_POPULAR, 200,
                        context.getString(R.string.source_dribbble_popular), true),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_FOLLOWING, 201,
                        context.getString(R.string.source_dribbble_following), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_USER_SHOTS, 202,
                        context.getString(R.string.source_dribbble_user_shots), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_USER_LIKES, 203,
                        context.getString(R.string.source_dribbble_user_likes), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_RECENT, 204,
                        context.getString(R.string.source_dribbble_recent), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_DEBUTS, 205,
                        context.getString(R.string.source_dribbble_debuts), false),
                new Source.DribbbleSource(SOURCE_DRIBBBLE_ANIMATED, 206,
                        context.getString(R.string.source_dribbble_animated), false),
                new Source.DribbbleSearchSource(SOURCE_DRIBBBLE_MD_SEARCH,
                        context.getString(R.string.source_dribbble_search_material_design), true),
                new Source(SOURCE_PRODUCT_HUNT, 400,
                        context.getString(R.string.source_product_hunt),
                        R.drawable.ic_product_hunt, false)
        };
    }

}
