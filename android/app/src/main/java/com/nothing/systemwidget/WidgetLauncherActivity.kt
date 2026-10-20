package com.nothing.systemwidget

import android.app.Activity
import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView

/**
 * Minimal launcher screen: add the widget to the home screen, and on Android 12+
 * grant the exact-alarm permission the 3s refresh needs.
 */
class WidgetLauncherActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var allowRefreshButton: Button

    private val widgetProvider by lazy { ComponentName(this, SystemStatsWidget::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        statusText = findViewById(R.id.status_text)
        allowRefreshButton = findViewById(R.id.allow_refresh_button)

        findViewById<Button>(R.id.add_widget_button).setOnClickListener { addWidget() }
        allowRefreshButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        }
    }

    // Re-check on every return: the pin dialog and permission screen both pause us.
    override fun onResume() {
        super.onResume()
        val widgetCount = AppWidgetManager.getInstance(this).getAppWidgetIds(widgetProvider).size
        statusText.setText(if (widgetCount > 0) R.string.status_added else R.string.status_not_added)
        allowRefreshButton.visibility = if (needsExactAlarmPermission()) View.VISIBLE else View.GONE
    }

    private fun addWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
            // Launcher shows its own "Add to home screen" dialog.
            appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
        } else {
            statusText.setText(R.string.pin_unsupported)
        }
    }

    private fun needsExactAlarmPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}
