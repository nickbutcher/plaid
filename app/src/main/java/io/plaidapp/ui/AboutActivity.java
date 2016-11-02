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

package io.plaidapp.ui;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;

import java.security.InvalidParameterException;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import io.plaidapp.R;
import io.plaidapp.ui.widget.ElasticDragDismissFrameLayout;
import io.plaidapp.ui.widget.InkPageIndicator;
import io.plaidapp.util.HtmlUtils;
import io.plaidapp.util.customtabs.CustomTabActivityHelper;
import io.plaidapp.util.glide.CircleTransform;

/**
 * About screen. This displays 3 pages in a ViewPager:
 *  – About Plaid
 *  – Credit Roman for the awesome icon
 *  – Credit libraries
 */
public class AboutActivity extends Activity {

    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.indicator) InkPageIndicator pageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        pager.setAdapter(new AboutPagerAdapter(AboutActivity.this));
        pager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.spacing_normal));
        pageIndicator.setViewPager(pager);

        draggableFrame.addListener(
                new ElasticDragDismissFrameLayout.SystemChromeFader(this) {
                    @Override
                    public void onDragDismissed() {
                        // if we drag dismiss downward then the default reversal of the enter
                        // transition would slide content upward which looks weird. So reverse it.
                        if (draggableFrame.getTranslationY() > 0) {
                            getWindow().setReturnTransition(
                                    TransitionInflater.from(AboutActivity.this)
                                            .inflateTransition(R.transition.about_return_downward));
                        }
                        finishAfterTransition();
                    }
                });
    }

    static class AboutPagerAdapter extends PagerAdapter {

        private View aboutPlaid;
        @Nullable @BindView(R.id.about_description) TextView plaidDescription;
        private View aboutIcon;
        @Nullable @BindView(R.id.icon_description) TextView iconDescription;
        private View aboutLibs;
        @Nullable @BindView(R.id.libs_list) RecyclerView libsList;

        private final LayoutInflater layoutInflater;
        private final Bypass markdown;
        private final Activity host;
        private final Resources resources;

        AboutPagerAdapter(@NonNull Activity host) {
            this.host = host;
            resources = host.getResources();
            layoutInflater = LayoutInflater.from(host);
            markdown = new Bypass(host, new Bypass.Options());
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View layout = getPage(position, collection);
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private View getPage(int position, ViewGroup parent) {
            switch (position) {
                case 0:
                    if (aboutPlaid == null) {
                        aboutPlaid = layoutInflater.inflate(R.layout.about_plaid, parent, false);
                        ButterKnife.bind(this, aboutPlaid);
                        // fun with spans & markdown
                        CharSequence about0 = markdown.markdownToSpannable(resources
                                .getString(R.string.about_plaid_0), plaidDescription, null);
                        SpannableString about1 = new SpannableString(
                                resources.getString(R.string.about_plaid_1));
                        about1.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                0, about1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        SpannableString about2 = new SpannableString(markdown.markdownToSpannable
                                (resources.getString(R.string.about_plaid_2),
                                        plaidDescription, null));
                        about2.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                0, about2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        SpannableString about3 = new SpannableString(markdown.markdownToSpannable
                                (resources.getString(R.string.about_plaid_3),
                                        plaidDescription, null));
                        about3.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                                0, about3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        CharSequence desc = TextUtils.concat(about0, "\n\n", about1, "\n", about2,
                                "\n\n", about3);
                        HtmlUtils.setTextWithNiceLinks(plaidDescription, desc);
                    }
                    return aboutPlaid;
                case 1:
                    if (aboutIcon == null) {
                        aboutIcon = layoutInflater.inflate(R.layout.about_icon, parent, false);
                        ButterKnife.bind(this, aboutIcon);
                        CharSequence icon0 = resources.getString(R.string.about_icon_0);
                        CharSequence icon1 = markdown.markdownToSpannable(resources
                                .getString(R.string.about_icon_1), iconDescription, null);
                        CharSequence iconDesc = TextUtils.concat(icon0, "\n", icon1);
                        HtmlUtils.setTextWithNiceLinks(iconDescription, iconDesc);
                    }
                    return aboutIcon;
                case 2:
                    if (aboutLibs == null) {
                        aboutLibs = layoutInflater.inflate(R.layout.about_libs, parent, false);
                        ButterKnife.bind(this, aboutLibs);
                        libsList.setAdapter(new LibraryAdapter(host));
                    }
                    return aboutLibs;
            }
            throw new InvalidParameterException();
        }
    }

    private static class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_INTRO = 0;
        private static final int VIEW_TYPE_LIBRARY = 1;
        static final Library[] libs = {
                new Library("Android support libraries",
                        "The Android support libraries offer a number of features that are not built into the framework.",
                        "https://developer.android.com/topic/libraries/support-library",
                        "https://developer.android.com/images/android_icon_125.png",
                        false),
                new Library("ButterKnife",
                        "Bind Android views and callbacks to fields and methods.",
                        "http://jakewharton.github.io/butterknife/",
                        "https://avatars.githubusercontent.com/u/66577",
                        true),
                new Library("Bypass",
                        "Skip the HTML, Bypass takes markdown and renders it directly.",
                        "https://github.com/Uncodin/bypass",
                        "https://avatars.githubusercontent.com/u/1072254",
                        true),
                new Library("Glide",
                        "An image loading and caching library for Android focused on smooth scrolling.",
                        "https://github.com/bumptech/glide",
                        "https://avatars.githubusercontent.com/u/423539",
                        false),
                new Library("JSoup",
                        "Java HTML Parser, with best of DOM, CSS, and jquery.",
                        "https://github.com/jhy/jsoup/",
                        "https://avatars.githubusercontent.com/u/76934",
                        true),
                new Library("OkHttp",
                        "An HTTP & HTTP/2 client for Android and Java applications.",
                        "http://square.github.io/okhttp/",
                        "https://avatars.githubusercontent.com/u/82592",
                        false),
                new Library("Retrofit",
                        "A type-safe HTTP client for Android and Java.",
                        "http://square.github.io/retrofit/",
                        "https://avatars.githubusercontent.com/u/82592",
                        false) };

        private final CircleTransform circleCrop;
        final Activity host;

        LibraryAdapter(Activity host) {
            this.host = host;
            circleCrop = new CircleTransform(host);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_INTRO:
                    return new LibraryIntroHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.about_lib_intro, parent, false));
                case VIEW_TYPE_LIBRARY:
                    return createLibraryHolder(parent);
            }
            throw new InvalidParameterException();
        }

        private @NonNull LibraryHolder createLibraryHolder(ViewGroup parent) {
            final LibraryHolder holder = new LibraryHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.library, parent, false));
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    CustomTabActivityHelper.openCustomTab(
                            host,
                            new CustomTabsIntent.Builder()
                                    .setToolbarColor(ContextCompat.getColor(host, R.color.primary))
                                    .addDefaultShareMenuItem()
                                    .build(), Uri.parse(libs[position - 1].link));

                }
            };
            holder.itemView.setOnClickListener(clickListener);
            holder.link.setOnClickListener(clickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_LIBRARY) {
                bindLibrary((LibraryHolder) holder, libs[position - 1]); // adjust for intro
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? VIEW_TYPE_INTRO : VIEW_TYPE_LIBRARY;
        }

        @Override
        public int getItemCount() {
            return libs.length + 1; // + 1 for the static intro view
        }

        private void bindLibrary(final LibraryHolder holder, final Library lib) {
            holder.name.setText(lib.name);
            holder.description.setText(lib.description);
            DrawableRequestBuilder<String> request = Glide.with(holder.image.getContext())
                    .load(lib.imageUrl)
                    .placeholder(R.drawable.avatar_placeholder);
            if (lib.circleCrop) {
                request.transform(circleCrop);
            }
            request.into(holder.image);
        }
    }

    static class LibraryHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.library_image) ImageView image;
        @BindView(R.id.library_name) TextView name;
        @BindView(R.id.library_description) TextView description;
        @BindView(R.id.library_link) Button link;

        LibraryHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class LibraryIntroHolder extends RecyclerView.ViewHolder {

        TextView intro;

        LibraryIntroHolder(View itemView) {
            super(itemView);
            intro = (TextView) itemView;
        }
    }

    /**
     * Models an open source library we want to credit
     */
    private static class Library {
        final String name;
        final String link;
        final String description;
        final String imageUrl;
        final boolean circleCrop;

        Library(String name, String description, String link, String imageUrl, boolean circleCrop) {
            this.name = name;
            this.description = description;
            this.link = link;
            this.imageUrl = imageUrl;
            this.circleCrop = circleCrop;
        }
    }

}
