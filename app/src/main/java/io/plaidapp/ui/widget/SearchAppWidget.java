package io.plaidapp.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import io.plaidapp.R;
import io.plaidapp.ui.SearchActivity;

public class SearchAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PendingIntent pendingIntent = createPendingIntent(context);
        for (int i = 0, l = appWidgetIds.length; i < l; i++) {
            int appWidgetId = appWidgetIds[i];
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_widget);
            views.setOnClickPendingIntent(R.id.search_view, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private PendingIntent createPendingIntent(Context context) {
        final Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_DISABLE_SAVE, true);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
