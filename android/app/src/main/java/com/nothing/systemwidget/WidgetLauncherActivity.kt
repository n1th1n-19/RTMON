package com.nothing.systemwidget

import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings

/**
 * Minimal launcher activity. On Android 12+ it first asks for the exact-alarm
 * permission the 3s refresh needs; otherwise it redirects users to add the widget.
 */
class WidgetLauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmManager = getSystemService(AlarmManager::class.java)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        // Close this activity immediately
        finish()
    }
}
