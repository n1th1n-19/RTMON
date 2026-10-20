package com.nothing.systemwidget

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.widget.RemoteViews
import java.io.File
import kotlin.math.roundToInt

class SystemStatsWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        refresh(context)
    }

    override fun onDisabled(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(tickIntent(context))
        clearBitmapCache()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) refresh(context)
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.nothing.systemwidget.UPDATE_WIDGET"
        private const val PREFS_NAME = "SystemStatsWidget"
        private const val MAX_DATA_POINTS = 20
        private const val REFRESH_MS = 3000L

        // Counter baselines older than this (screen was off, process restarted
        // long ago) would yield a long-window average, so we re-baseline instead.
        private const val STALE_MS = 15_000L

        // Network graph scales to the busiest point in the visible window, but never
        // below this, so idle background chatter doesn't fill the graph.
        private const val NET_SCALE_FLOOR = 64f * 1024f

        private const val KEY_SAMPLE_AT = "sample_at"
        private const val KEY_CPU_TOTAL = "cpu_total"
        private const val KEY_CPU_IDLE = "cpu_idle"
        private const val KEY_NET_BYTES = "net_bytes"

        private fun linePaint(hex: String) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(hex)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        private fun fillPaint(hex: String) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(hex)
            style = Paint.Style.FILL
        }

        private val redLinePaint = linePaint("#FF0000")
        private val redFillPaint = fillPaint("#22FF0000")
        private val greenLinePaint = linePaint("#00FF00")
        private val greenFillPaint = fillPaint("#2200FF00")
        private val blueLinePaint = linePaint("#0099FF")
        private val blueFillPaint = fillPaint("#220099FF")
        private val amberLinePaint = linePaint("#FFAA00")
        private val amberFillPaint = fillPaint("#22FFAA00")

        private val gridPaint = Paint().apply {
            strokeWidth = 1f
        }

        private var cachedGraphBgColor: Int? = null
        private var cachedGridColor: Int? = null
        private var lastThemeHash: Int = 0

        private val bitmapCache = mutableMapOf<String, Bitmap>()

        fun clearBitmapCache() {
            bitmapCache.values.forEach { it.recycle() }
            bitmapCache.clear()
            cachedGraphBgColor = null
            cachedGridColor = null
        }

        private fun tickIntent(context: Context): PendingIntent {
            val intent = Intent(context, SystemStatsWidget::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * One-shot alarm chained from each tick. setRepeating() is clamped to >= 60s
         * by the OS, so it can't deliver a 3s refresh. Exact alarms need the user's
         * grant on Android 13+; without it we fall back to an inexact alarm (~5-10s).
         * ELAPSED_REALTIME (non-wakeup) never wakes a sleeping device.
         */
        private fun scheduleNext(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val triggerAt = SystemClock.elapsedRealtime() + REFRESH_MS
            val pendingIntent = tickIntent(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, triggerAt, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAt, pendingIntent)
            }
        }

        private fun refresh(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SystemStatsWidget::class.java)
            )
            if (appWidgetIds.isEmpty()) return

            // Sample once per tick and render every widget instance from that one
            // sample, so multiple widgets don't double-append history.
            val powerManager = context.getSystemService(PowerManager::class.java)
            if (powerManager.isInteractive) {
                appWidgetManager.updateAppWidget(appWidgetIds, buildViews(context))
            }
            // Re-arm even while the screen is off, or the chain would die.
            scheduleNext(context)
        }

        private fun buildViews(context: Context): RemoteViews {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            // Baselines live in prefs (not statics) so they survive process death.
            val now = SystemClock.elapsedRealtime()
            val lastAt = prefs.getLong(KEY_SAMPLE_AT, 0L)
            val elapsed = now - lastAt
            val fresh = lastAt > 0L && elapsed in 1..STALE_MS
            editor.putLong(KEY_SAMPLE_AT, now)

            val cpuUsage = getCpuUsage(prefs, editor, fresh)
            val memoryUsage = getMemoryUsage(context)
            val batteryLevel = getBatteryLevel(context)
            val networkSpeed = getNetworkSpeed(prefs, editor, fresh, elapsed)

            val views = RemoteViews(context.packageName, R.layout.widget_system_stats)
            views.setTextViewText(R.id.cpu_usage_text, formatPercent(cpuUsage))
            views.setTextViewText(R.id.memory_usage_text, formatPercent(memoryUsage))
            views.setTextViewText(R.id.battery_level_text, formatPercent(batteryLevel))
            views.setTextViewText(R.id.network_speed_text, networkSpeed?.let { formatSpeed(it) } ?: "--")

            val cpuHistory = pushHistory(prefs, editor, "cpu_history", cpuUsage?.toFloat())
            views.setImageViewBitmap(R.id.cpu_graph, drawGraph("cpu", cpuHistory, 100f, redLinePaint, redFillPaint, context))

            val memHistory = pushHistory(prefs, editor, "mem_history", memoryUsage.toFloat())
            views.setImageViewBitmap(R.id.memory_graph, drawGraph("mem", memHistory, 100f, greenLinePaint, greenFillPaint, context))

            val batHistory = pushHistory(prefs, editor, "bat_history", batteryLevel?.toFloat())
            views.setImageViewBitmap(R.id.battery_graph, drawGraph("battery", batHistory, 100f, blueLinePaint, blueFillPaint, context))

            // Stores raw bytes/s; scale is derived from the visible window at draw time.
            val netHistory = pushHistory(prefs, editor, "net_history", networkSpeed)
            val netScale = (netHistory.maxOrNull() ?: 0f).coerceAtLeast(NET_SCALE_FLOOR)
            views.setImageViewBitmap(R.id.network_graph, drawGraph("net", netHistory, netScale, amberLinePaint, amberFillPaint, context))

            editor.apply()
            return views
        }

        /** Appends [value] (when known) to the stored history and returns the window. */
        private fun pushHistory(
            prefs: SharedPreferences,
            editor: SharedPreferences.Editor,
            key: String,
            value: Float?
        ): List<Float> {
            val history = getHistoryList(prefs, key)
            if (value != null) {
                history.add(value)
                while (history.size > MAX_DATA_POINTS) history.removeAt(0)
                editor.putString(key, history.joinToString(","))
            }
            return history
        }

        private fun drawGraph(id: String, dataPoints: List<Float>, scale: Float, linePaint: Paint, fillPaint: Paint, context: Context): Bitmap {
            val width = 240
            val height = 96
            val bitmap = bitmapCache.getOrPut(id) {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)

            val currentThemeHash = context.resources.configuration.uiMode
            if (currentThemeHash != lastThemeHash || cachedGraphBgColor == null) {
                cachedGraphBgColor = context.resources.getColor(R.color.widget_graph_background, context.theme)
                cachedGridColor = context.resources.getColor(R.color.widget_grid, context.theme)
                lastThemeHash = currentThemeHash
            }

            canvas.drawColor(cachedGraphBgColor!!)

            if (dataPoints.isEmpty()) return bitmap

            gridPaint.color = cachedGridColor!!
            for (i in 0..4) {
                val y = height * i / 4f
                canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            }

            val path = Path()
            val fillPath = Path()
            val stepX = width.toFloat() / (MAX_DATA_POINTS - 1).coerceAtLeast(1)

            dataPoints.forEachIndexed { index, value ->
                val x = stepX * index
                val y = height - (height * (value / scale).coerceIn(0f, 1f))

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height.toFloat())
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(stepX * (dataPoints.size - 1), height.toFloat())
            fillPath.close()

            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(path, linePaint)

            return bitmap
        }

        private fun getHistoryList(prefs: SharedPreferences, key: String): MutableList<Float> {
            val historyString = prefs.getString(key, "") ?: ""
            return if (historyString.isEmpty()) {
                mutableListOf()
            } else {
                historyString.split(",").mapNotNull { it.toFloatOrNull() }.toMutableList()
            }
        }

        private fun formatPercent(value: Int?): String =
            value?.let { String.format("%02d%%", it) } ?: "--"

        private fun formatSpeed(speedBytesPerSec: Float): String {
            return when {
                speedBytesPerSec < 1024f -> String.format("%.0fB/s", speedBytesPerSec)
                speedBytesPerSec < 1024f * 1024f -> String.format("%.0fKB/s", speedBytesPerSec / 1024f)
                else -> String.format("%.1fMB/s", speedBytesPerSec / 1024f / 1024f)
            }
        }

        /** Device-wide rx+tx bytes/s, or null when unsupported or no fresh baseline. */
        private fun getNetworkSpeed(
            prefs: SharedPreferences,
            editor: SharedPreferences.Editor,
            fresh: Boolean,
            elapsedMs: Long
        ): Float? {
            val rxBytes = TrafficStats.getTotalRxBytes()
            val txBytes = TrafficStats.getTotalTxBytes()
            if (rxBytes == TrafficStats.UNSUPPORTED.toLong() || txBytes == TrafficStats.UNSUPPORTED.toLong()) {
                return null
            }

            val total = rxBytes + txBytes
            val previous = prefs.getLong(KEY_NET_BYTES, -1L)
            editor.putLong(KEY_NET_BYTES, total)

            val delta = total - previous
            // Negative delta = counters reset (reboot); skip that sample.
            if (!fresh || previous < 0L || delta < 0L) return null
            return delta * 1000f / elapsedMs
        }

        /**
         * True utilisation from /proc/stat where readable (Android 7 and below, some
         * ROMs). Android 8+ blocks /proc/stat for apps, so fall back to frequency
         * load: current clock vs max clock across all online cores.
         */
        private fun getCpuUsage(
            prefs: SharedPreferences,
            editor: SharedPreferences.Editor,
            fresh: Boolean
        ): Int? {
            val stat = readProcStat() ?: return getCpuFrequencyLoad()
            val (total, idle) = stat

            val lastTotal = prefs.getLong(KEY_CPU_TOTAL, -1L)
            val lastIdle = prefs.getLong(KEY_CPU_IDLE, -1L)
            editor.putLong(KEY_CPU_TOTAL, total)
            editor.putLong(KEY_CPU_IDLE, idle)

            val totalDelta = total - lastTotal
            val idleDelta = idle - lastIdle
            if (!fresh || lastTotal < 0L || totalDelta <= 0L || idleDelta !in 0..totalDelta) return null
            return ((totalDelta - idleDelta) * 100f / totalDelta).roundToInt()
        }

        /** Returns (total, idle) jiffies from the aggregate cpu line, or null if unreadable. */
        private fun readProcStat(): Pair<Long, Long>? = try {
            val fields = File("/proc/stat").bufferedReader().use { it.readLine() }
                .trim().split(Regex("\\s+"))
            // cpu user nice system idle iowait irq softirq steal
            val values = fields.drop(1).take(8).map { it.toLong() }
            if (fields[0] != "cpu" || values.size < 8) null
            else Pair(values.sum(), values[3] + values[4])
        } catch (e: Exception) {
            null
        }

        private fun getCpuFrequencyLoad(): Int? {
            val cpuDirs = File("/sys/devices/system/cpu")
                .listFiles { file -> file.name.matches(Regex("cpu\\d+")) } ?: return null
            var current = 0L
            var max = 0L
            for (dir in cpuDirs) {
                val coreMax = readLong(File(dir, "cpufreq/cpuinfo_max_freq")) ?: continue
                val coreCurrent = readLong(File(dir, "cpufreq/scaling_cur_freq")) ?: continue
                max += coreMax
                current += coreCurrent
            }
            return if (max > 0L) (current * 100f / max).roundToInt().coerceIn(0, 100) else null
        }

        private fun readLong(file: File): Long? = try {
            file.readText().trim().toLong()
        } catch (e: Exception) {
            null
        }

        private fun getMemoryUsage(context: Context): Int {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
            return (usedMemory * 100.0 / memoryInfo.totalMem).roundToInt()
        }

        private fun getBatteryLevel(context: Context): Int? {
            val batteryManager = context.getSystemService(BatteryManager::class.java)
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                .takeIf { it in 0..100 }
        }
    }
}
