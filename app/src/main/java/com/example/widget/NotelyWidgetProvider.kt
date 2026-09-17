package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class NotelyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.notely_widget_layout)

        // Set pending intents for actions
        views.setOnClickPendingIntent(R.id.widget_title, createLaunchIntent(context, "HOME"))
        views.setOnClickPendingIntent(R.id.btn_widget_note, createLaunchIntent(context, "NOTE"))
        views.setOnClickPendingIntent(R.id.btn_widget_record, createLaunchIntent(context, "RECORD"))
        views.setOnClickPendingIntent(R.id.btn_widget_scan, createLaunchIntent(context, "SCAN"))
        views.setOnClickPendingIntent(R.id.btn_widget_link, createLaunchIntent(context, "LINK"))
        views.setOnClickPendingIntent(R.id.btn_widget_mic, createLaunchIntent(context, "RECORD"))
        views.setOnClickPendingIntent(R.id.widget_bottom_bar, createLaunchIntent(context, "TODAY"))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createLaunchIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("widget_action", action)
        }
        val requestCode = action.hashCode()
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
