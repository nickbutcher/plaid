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

package com.example.android.plaid.data.api.dribbble;

import android.support.annotation.StringDef;
import android.support.annotation.WorkerThread;

import com.example.android.plaid.data.api.dribbble.model.Images;
import com.example.android.plaid.data.api.dribbble.model.Shot;
import com.example.android.plaid.data.api.dribbble.model.User;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nickbutcher on 7/23/14.
 */
public class DribbbleSearch {

    public static final String SORT_POPULAR = "s=";
    public static final String SORT_RECENT = "s=latest";
    private static final String SEARCH_ENDPOINT = "/search?q=";
    private static final String HOST = "https://dribbble.com";
    private static final Pattern PATTERN_RESULT = Pattern.compile("<li\\sid=\"screenshot-(\\d+)(" +
            ".*?)</h2>\\s*?</li>", Pattern.DOTALL);
    private static final Pattern PATTERN_TITLE_URL = Pattern.compile("<a\\s+href=\"([^\"]+)" +
                    "\"\\s+class=\"dribbble-link\"><div\\s+data-picture\\s+data-alt=\"([^\"]+)\">",
            Pattern.DOTALL);
    private static final Pattern PATTERN_URL_TITLE = Pattern.compile
            ("<a\\s+class=\"dribbble-link\"\\s+href=\"([^\"]+)" +
                    "\"><div\\s+data-picture\\s+data-alt=\"([^\"]+)\">", Pattern.DOTALL);
    private static final Pattern PATTERN_IMAGE_URL = Pattern.compile("<img\\s.*?src=\"([^\"]+?)" +
            "\"", Pattern.DOTALL);
    private static final Pattern PATTERN_COMMENT = Pattern.compile("<span\\sclass=\"comment\">" +
            "([^<]+?)</span>", Pattern.DOTALL);
    private static final Pattern PATTERN_CREATED_AT = Pattern.compile("<em\\sclass=\"timestamp\">" +
            "([^<]+?)</em>", Pattern.DOTALL);
    private static final Pattern PATTERN_FAVS = Pattern.compile("<li\\sclass=\"fav\">.*?>(\\d+?)" +
            "</a>", Pattern.DOTALL);
    private static final Pattern PATTERN_COMMENTS = Pattern.compile("<li\\sclass=\"cmnt\">.*?>" +
            "(\\d+?)</a>", Pattern.DOTALL);
    private static final Pattern PATTERN_VIEWS = Pattern.compile("<li\\sclass=\"views\">([^<]+?)" +
            "</li>", Pattern.DOTALL);
    private static final Pattern PATTERN_PLAYER = Pattern.compile
            ("<span\\sclass=\"attribution-user\">(.*?)</span>", Pattern.DOTALL);
    private static final Pattern PATTERN_PLAYER_USERNAME = Pattern.compile("<a\\shref=\"/" +
            "([^\"]+?)\"", Pattern.DOTALL);
    private static final Pattern PATTERN_PLAYER_NAME = Pattern.compile("title=\"([^\"]+?)\"",
            Pattern.DOTALL);
    private static final Pattern PATTERN_PLAYER_AVATAR_URL = Pattern.compile
            ("class=\"photo\"\\ssrc=\"([^\"]+?)\"", Pattern.DOTALL);
    private static final Pattern PATTERN_PLAYER_ID = Pattern.compile("users/(\\d+?)/", Pattern
            .DOTALL);

    @WorkerThread
    public static List<Shot> search(String query, @SortOrder String sort, int page) {

        // e.g https://dribbble.com/search?q=material+design&page=7&per_page=12
        String html = downloadPage(HOST + SEARCH_ENDPOINT + URLEncoder.encode(query) + "&" + sort
                + "&page=" + page + "&per_page=12");
        if (html == null) return null;
        Matcher matcher = PATTERN_RESULT.matcher(html);
        List<Shot> shots = new ArrayList<Shot>();

        while (matcher.find()) {
            Shot shot = parseShot(matcher);
            if (shot != null) {
                shots.add(shot);
            }
        }
        return shots;
    }

    private static Shot parseShot(Matcher matcherShot) {
        Long id = Long.parseLong(matcherShot.group(1));
        String url = null;
        String title = null;
        String imgUrl = null;
        String description = null;
        Date createdAt = null;
        User player = null;
        String searchResult = matcherShot.group(2);
        Matcher matchUrlTitle = PATTERN_URL_TITLE.matcher(searchResult);
        if (matchUrlTitle.find()) {
            url = HOST + matchUrlTitle.group(1);
            title = matchUrlTitle.group(2);
        } else {
            matchUrlTitle = PATTERN_TITLE_URL.matcher(searchResult);
            if (matchUrlTitle.find()) {
                url = HOST + matchUrlTitle.group(1);
                title = matchUrlTitle.group(2);
            } else {
                return null;
            }
        }
        Matcher matchImgTitle = PATTERN_IMAGE_URL.matcher(searchResult);
        if (matchImgTitle.find()) {
            imgUrl = matchImgTitle.group(1);
            if (imgUrl.contains("_teaser.")) {
                imgUrl = imgUrl.replace("_teaser.", ".");
            }
        } else {
            return null;
        }
        Matcher matchComment = PATTERN_COMMENT.matcher(searchResult);
        if (matchComment.find()) {
            // API responses wrap description in a <p> tag. Do the same for consistent display.
            description = "<p>" + matchComment.group(1).trim() + "</p>";
        }
        Matcher matchCreatedAt = PATTERN_CREATED_AT.matcher(searchResult);
        if (matchCreatedAt.find()) {
            createdAt = new Date(Long.parseLong(matchCreatedAt.group(1)));
        }
        int favs = 0;
        Matcher matchFavs = PATTERN_FAVS.matcher(searchResult);
        if (matchFavs.find()) {
            favs = Integer.parseInt(matchFavs.group(1).trim().replaceAll(",", ""));
        }
        int comments = 0;
        Matcher matchComments = PATTERN_COMMENTS.matcher(searchResult);
        if (matchComments.find()) {
            comments = Integer.parseInt(matchComments.group(1).trim().replaceAll(",", ""));
        }
        int views = 0;
        Matcher matchViews = PATTERN_VIEWS.matcher(searchResult);
        if (matchViews.find()) {
            views = Integer.parseInt(matchViews.group(1).trim().replaceAll(",", ""));
        }

        // Player
        Matcher matchPlayer = PATTERN_PLAYER.matcher(searchResult);
        if (matchPlayer.find()) {
            player = parsePlayer(matchPlayer.group(1));
        }

        return new Shot(id,
                title,
                description,
                0l, // width
                0l, // height
                new Images(null, imgUrl, null), // TODO currently assuming normal quality, look
                // into this
                views,
                favs,
                comments,
                0l, // attachments_count
                0l, // rebounds_count
                0l, // buckets_count
                createdAt,
                null, // updated_at
                url,
                null, // attachments_url
                null, // buckets_url
                null, // comments_url
                null, // likes_url
                null, // projects_url
                null, // rebounds_url
                null, // tags
                player,
                null // team
        );
    }

    private static User parsePlayer(String player) {
        String username = null;
        String name = null;
        String avatarUrl = null;
        Long id = -1l;
        Matcher matchUsername = PATTERN_PLAYER_USERNAME.matcher(player);
        if (matchUsername.find()) {
            username = matchUsername.group(1);
        }
        Matcher matchName = PATTERN_PLAYER_NAME.matcher(player);
        if (matchName.find()) {
            name = matchName.group(1);
        }
        Matcher matchAvatar = PATTERN_PLAYER_AVATAR_URL.matcher(player);
        if (matchAvatar.find()) {
            avatarUrl = matchAvatar.group(1);
            if (avatarUrl.contains("/mini/")) {
                avatarUrl = avatarUrl.replace("/mini/", "/normal/");
            }
            Matcher matchId = PATTERN_PLAYER_ID.matcher(player);
            if (matchId.find()) {
                id = Long.parseLong(matchId.group(1));
            }
        }
        return new User(id,
                name,
                username,
                HOST + "/" + username, // url
                avatarUrl,
                null, // bio
                null, // location
                null, // links
                -1, // buckets_count
                -1, // followers_count
                -1, // followings_count
                -1, // likes_count
                -1, // projects_count
                -1, // shots_count
                -1, // teams_count
                null, // type
                null, // pro
                null, // buckets_url
                null, // followers_url
                null, // following_url
                null, // likes_url
                null, // projects_url
                null, // shots_url
                null, // teams_url
                null, // created_at
                null // updated_at
        );
    }

    private static String downloadPage(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException ioe) {
            // TODO
            boolean rohrow = false;
            return null;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SORT_POPULAR,
            SORT_RECENT
    })
    public @interface SortOrder {}

}
