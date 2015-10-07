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

package io.plaidapp.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.plaidapp.R;
import io.plaidapp.data.Source;

/**
 * Manage saving and retrieving data sources from disk.
 */
public class SourceManager {

    public static final String SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR";
    public static final String SOURCE_DESIGNER_NEWS_RECENT = "SOURCE_DESIGNER_NEWS_RECENT";
    public static final String SOURCE_DRIBBBLE_POPULAR = "SOURCE_DRIBBBLE_POPULAR";
    public static final String SOURCE_DRIBBBLE_FOLLOWING = "SOURCE_DRIBBBLE_FOLLOWING";
    public static final String SOURCE_DRIBBBLE_USER_LIKES = "SOURCE_DRIBBBLE_USER_LIKES";
    public static final String SOURCE_DRIBBBLE_USER_SHOTS = "SOURCE_DRIBBBLE_USER_SHOTS";
    public static final String SOURCE_DRIBBBLE_RECENT = "SOURCE_DRIBBBLE_RECENT";
    public static final String SOURCE_DRIBBBLE_DEBUTS = "SOURCE_DRIBBBLE_DEBUTS";
    public static final String SOURCE_DRIBBBLE_ANIMATED = "SOURCE_DRIBBBLE_ANIMATED";
    public static final String SOURCE_PRODUCT_HUNT = "SOURCE_PRODUCT_HUNT";
    private static final String SOURCES_PREF = "SOURCES_PREF";
    private static final String KEY_SOURCES = "KEY_SOURCES";

    public static List<Source> getSources(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE);
        Set<String> sourceKeys = prefs.getStringSet(KEY_SOURCES, null);
        if (sourceKeys == null) {
            setupDefaultSources(context, prefs.edit());
            return getDefaultSources(context);
        }

        List<Source> sources = new ArrayList<>(sourceKeys.size());
        for (String sourceKey : sourceKeys) {
            if (sourceKey.startsWith(Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX)) {
                sources.add(new Source.DribbbleSearchSource(
                        sourceKey.replace(Source.DribbbleSearchSource.DRIBBBLE_QUERY_PREFIX, ""),
                        prefs.getBoolean(sourceKey, false)));
            } else if (sourceKey.startsWith(Source.DesignerNewsSearchSource
                    .DESIGNER_NEWS_QUERY_PREFIX)) {
                sources.add(new Source.DesignerNewsSearchSource(
                        sourceKey.replace(Source.DesignerNewsSearchSource
                                .DESIGNER_NEWS_QUERY_PREFIX, ""),
                        prefs.getBoolean(sourceKey, false)));
            } else {
                // TODO improve this O(n2) search
                sources.add(getSource(context, sourceKey, prefs.getBoolean(sourceKey, false)));
            }
        }
        Collections.sort(sources, new Source.SourceComparator());
        return sources;
    }

    public static void addSource(Source toAdd, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> sourceKeys = prefs.getStringSet(KEY_SOURCES, null);
        sourceKeys.add(toAdd.key);
        editor.putStringSet(KEY_SOURCES, sourceKeys);
        editor.putBoolean(toAdd.key, toAdd.active);
        editor.apply();
    }

    public static void updateSource(Source source, Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE).edit();
        editor.putBoolean(source.key, source.active);
        editor.apply();
    }

    public static void removeSource(Source source, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> sourceKeys = prefs.getStringSet(KEY_SOURCES, null);
        sourceKeys.remove(source.key);
        editor.putStringSet(KEY_SOURCES, sourceKeys);
        editor.remove(source.key);
        editor.apply();
    }

    private static void setupDefaultSources(Context context, SharedPreferences.Editor editor) {
        ArrayList<Source> defaultSources = getDefaultSources(context);
        Set<String> keys = new HashSet<>(defaultSources.size());
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

    private static ArrayList<Source> getDefaultSources(Context context) {
        ArrayList<Source> defaultSources = new ArrayList<>(11);
        defaultSources.add(new Source.DesignerNewsSource(SOURCE_DESIGNER_NEWS_POPULAR, 100,
                context.getString(R.string.source_designer_news_popular), true));
        defaultSources.add(new Source.DesignerNewsSource(SOURCE_DESIGNER_NEWS_RECENT, 101,
                context.getString(R.string.source_designer_news_recent), false));
        // 200 sort order range left for DN searches
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_POPULAR, 300,
                context.getString(R.string.source_dribbble_popular), true));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_FOLLOWING, 301,
                context.getString(R.string.source_dribbble_following), false));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_USER_SHOTS, 302,
                context.getString(R.string.source_dribbble_user_shots), false));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_USER_LIKES, 303,
                context.getString(R.string.source_dribbble_user_likes), false));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_RECENT, 304,
                context.getString(R.string.source_dribbble_recent), false));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_DEBUTS, 305,
                context.getString(R.string.source_dribbble_debuts), false));
        defaultSources.add(new Source.DribbbleSource(SOURCE_DRIBBBLE_ANIMATED, 306,
                context.getString(R.string.source_dribbble_animated), false));
        defaultSources.add(new Source.DribbbleSearchSource(context.getString(R.string
                .source_dribbble_search_material_design), true));
        // 400 sort order range left for dribbble searches
        defaultSources.add(new Source(SOURCE_PRODUCT_HUNT, 500,
                context.getString(R.string.source_product_hunt),
                R.drawable.ic_product_hunt, false));
        return defaultSources;
    }

}
