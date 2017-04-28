package io.plaidapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import io.plaidapp.R;

public class SearchShortcutConfigureActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent launchIntent = new Intent(this, SearchActivity.class);
        launchIntent.putExtra(SearchActivity.EXTRA_DISABLE_SAVE, true);

        Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_shortcut_search);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.search));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);

        setResult(RESULT_OK, intent);
        finish();
    }
}