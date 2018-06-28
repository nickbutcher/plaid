/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.ui.about;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.InvalidParameterException;

import in.uncod.android.bypass.Bypass;
import io.plaidapp.about.R;
import io.plaidapp.base.util.HtmlUtils;

class AboutPagerAdapter extends PagerAdapter {

    private View aboutPlaid;
    @Nullable
    private TextView plaidDescription;
    private View aboutIcon;
    @Nullable
    private TextView iconDescription;
    private View aboutLibs;
    @Nullable
    private RecyclerView libsList;

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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        View layout = getPage(position, collection);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private View getPage(int position, ViewGroup parent) {
        switch (position) {
            case 0:
                if (aboutPlaid == null) {
                    buildAppAboutPage(parent);
                }
                return aboutPlaid;
            case 1:
                if (aboutIcon == null) {
                    buildIconAboutPage(parent);
                }
                return aboutIcon;
            case 2:
                if (aboutLibs == null) {
                    buildLibsAboutPage(parent);
                }
                return aboutLibs;
        }
        throw new InvalidParameterException();
    }

    private void buildLibsAboutPage(ViewGroup parent) {
        aboutLibs = layoutInflater.inflate(R.layout.about_libs, parent, false);
        bindViews(aboutLibs);
        libsList.setAdapter(new LibraryAdapter(host));
    }

    private void buildIconAboutPage(ViewGroup parent) {
        aboutIcon = layoutInflater.inflate(R.layout.about_icon, parent, false);
        bindViews(aboutIcon);
        HtmlUtils.setTextWithNiceLinks(iconDescription, getIconAboutText());
    }

    private void buildAppAboutPage(ViewGroup parent) {
        aboutPlaid = layoutInflater.inflate(R.layout.about_plaid, parent, false);
        bindViews(aboutPlaid);
        CharSequence desc = getAppAboutText();

        HtmlUtils.setTextWithNiceLinks(plaidDescription, desc);
    }

    private CharSequence getAppAboutText() {
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
        return TextUtils.concat(about0, "\n\n", about1, "\n", about2,
                "\n\n", about3);
    }

    private CharSequence getIconAboutText() {
        CharSequence icon0 = resources.getString(R.string.about_icon_0);
        CharSequence icon1 = markdown.markdownToSpannable(resources
                .getString(R.string.about_icon_1), iconDescription, null);
        return TextUtils.concat(icon0, "\n", icon1);
    }

    private void bindViews(View parent) {
        plaidDescription = parent.findViewById(R.id.about_description);
        iconDescription = parent.findViewById(R.id.icon_description);
        libsList = parent.findViewById(R.id.libs_list);
    }
}
