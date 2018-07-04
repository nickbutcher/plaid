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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.security.InvalidParameterException;

import io.plaidapp.about.R;
import io.plaidapp.core.util.HtmlUtils;

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

    private LayoutInflater layoutInflater;

    private final AboutViewModel aboutViewModel;

    AboutPagerAdapter(@NonNull AboutViewModel aboutViewModel) {
        this.aboutViewModel = aboutViewModel;
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
        assureLayoutInflaterInitialized(parent);
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

    private void assureLayoutInflaterInitialized(View view) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(view.getContext());
        }
    }

    private void buildLibsAboutPage(ViewGroup parent) {
        aboutLibs = layoutInflater.inflate(R.layout.about_libs, parent, false);
        bindViews(aboutLibs);
        libsList.setAdapter(new LibraryAdapter(aboutViewModel.getLibraries()));
    }

    private void buildIconAboutPage(ViewGroup parent) {
        aboutIcon = layoutInflater.inflate(R.layout.about_icon, parent, false);
        bindViews(aboutIcon);
        HtmlUtils.setTextWithNiceLinks(iconDescription, aboutViewModel.getIconAboutText());
    }

    private void buildAppAboutPage(ViewGroup parent) {
        aboutPlaid = layoutInflater.inflate(R.layout.about_plaid, parent, false);
        bindViews(aboutPlaid);

        HtmlUtils.setTextWithNiceLinks(plaidDescription, aboutViewModel.getAppAboutText());
    }

    private void bindViews(View parent) {
        plaidDescription = parent.findViewById(R.id.about_description);
        iconDescription = parent.findViewById(R.id.icon_description);
        libsList = parent.findViewById(R.id.libs_list);
    }
}
