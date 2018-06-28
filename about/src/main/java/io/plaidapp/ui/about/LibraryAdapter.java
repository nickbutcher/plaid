package io.plaidapp.ui.about;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.security.InvalidParameterException;

import io.plaidapp.about.R;
import io.plaidapp.base.util.customtabs.CustomTabActivityHelper;
import kotlin.Unit;

class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_INTRO = 0;
    private static final int VIEW_TYPE_LIBRARY = 1;

    private static final Library[] libs = {
            new Library("Android support libraries",
                    "The Android support libraries offer a number of features that are not built "
                            + "into the framework.",
                    "https://developer.android.com/topic/libraries/support-library",
                    "https://developer.android.com/images/android_icon_125.png",
                    false),
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
                    false)};

    private final Activity host;

    LibraryAdapter(Activity host) {
        this.host = host;
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

    private @NonNull
    LibraryHolder createLibraryHolder(ViewGroup parent) {
        final LibraryHolder holder = new LibraryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.library, parent, false),
                (link, position) -> {
                    if (position == RecyclerView.NO_POSITION) return Unit.INSTANCE;
                    openLink(link);
                    return Unit.INSTANCE;
                });
        return holder;
    }

    private void openLink(String link) {
        CustomTabActivityHelper.openCustomTab(
                host,
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(host,
                                io.plaidapp.R.color.primary))
                        .addDefaultShareMenuItem()
                        .build(), Uri.parse(link));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_LIBRARY) {
            ((LibraryHolder) holder).bind(libs[position - 1]); // adjust for intro
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

}
