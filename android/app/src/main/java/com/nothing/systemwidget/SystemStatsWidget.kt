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
import android.util.Log
import android.widget.RemoteViews
import java.io.RandomAccessFile
import kotlin.math.roundToInt

class SystemStatsWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        startRealtimeUpdates(context)
    }

    override fun onEnabled(context: Context) {
        startRealtimeUpdates(context)
    }

    override fun onDisabled(context: Context) {
        stopRealtimeUpdates(context)
        clearBitmapCache()
    }

    private fun startRealtimeUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SystemStatsWidget::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            3000L,
            pendingIntent
        )
    }

    private fun stopRealtimeUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SystemStatsWidget::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, SystemStatsWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.nothing.systemwidget.UPDATE_WIDGET"
        private const val PREFS_NAME = "SystemStatsWidget"
        private const val MAX_DATA_POINTS = 20

        private val redLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF0000")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        private val redFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22FF0000")
            style = Paint.Style.FILL
        }

        private val greenLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FF00")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        private val greenFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2200FF00")
            style = Paint.Style.FILL
        }

        private val blueLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0099FF")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        private val blueFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#220099FF")
            style = Paint.Style.FILL
        }

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

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isInteractive) {
                return
            }

            val views = RemoteViews(context.packageName, R.layout.widget_system_stats)

            val cpuUsage = getCpuUsage()
            val memoryUsage = getMemoryUsage(context)
            val batteryLevel = getBatteryLevel(context)
            val networkSpeed = getNetworkSpeed()

            views.setTextViewText(R.id.cpu_usage_text, String.format("%02d%%", cpuUsage))
            views.setTextViewText(R.id.memory_usage_text, String.format("%02d%%", memoryUsage))
            views.setTextViewText(R.id.battery_level_text, String.format("%02d%%", batteryLevel))
            views.setTextViewText(R.id.network_speed_text, formatSpeed(networkSpeed))

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            val cpuHistory = getHistoryList(prefs, "cpu_history")
            cpuHistory.add(cpuUsage.toFloat())
            if (cpuHistory.size > MAX_DATA_POINTS) cpuHistory.removeAt(0)
            editor.putString("cpu_history", cpuHistory.joinToString(","))
            views.setImageViewBitmap(R.id.cpu_graph, drawGraph("cpu", cpuHistory, 240, 96, redLinePaint, redFillPaint, context))

            val memHistory = getHistoryList(prefs, "mem_history")
            memHistory.add(memoryUsage.toFloat())
            if (memHistory.size > MAX_DATA_POINTS) memHistory.removeAt(0)
            editor.putString("mem_history", memHistory.joinToString(","))
            views.setImageViewBitmap(R.id.memory_graph, drawGraph("mem", memHistory, 240, 96, greenLinePaint, greenFillPaint, context))

            val batHistory = getHistoryList(prefs, "bat_history")
            batHistory.add(batteryLevel.toFloat())
            if (batHistory.size > MAX_DATA_POINTS) batHistory.removeAt(0)
            editor.putString("bat_history", batHistory.joinToString(","))
            views.setImageViewBitmap(R.id.battery_graph, drawGraph("battery", batHistory, 240, 96, blueLinePaint, blueFillPaint, context))

            val netHistory = getHistoryList(prefs, "net_history")
            val maxSpeed = prefs.getFloat("max_speed", 1048576f)
            val normalizedSpeed = ((networkSpeed / maxSpeed) * 100f).coerceAtMost(100f)
            if (networkSpeed > maxSpeed) {
                editor.putFloat("max_speed", networkSpeed)
            }
            netHistory.add(normalizedSpeed)
            if (netHistory.size > MAX_DATA_POINTS) netHistory.removeAt(0)
            editor.putString("net_history", netHistory.joinToString(","))
            views.setImageViewBitmap(R.id.network_graph, drawGraph("net", netHistory, 240, 96, blueLinePaint, blueFillPaint, context))

            editor.apply()
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun drawGraph(id: String, dataPoints: List<Float>, width: Int, height: Int, linePaint: Paint, fillPaint: Paint, context: Context): Bitmap {
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
                val y = height - (height * value / 100f)

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

        private var lastCpuTotal = 0L
        private var lastCpuIdle = 0L
        private var lastCpuUsage = 0
        private var lastRxBytes = 0L
        private var lastTxBytes = 0L
        private var lastNetworkTime = 0L

        private fun formatSpeed(speedBytesPerSec: Float): String {
            return when {
                speedBytesPerSec < 1024f -> String.format("%.0fB/s", speedBytesPerSec)
                speedBytesPerSec < 1024f * 1024f -> String.format("%.0fKB/s", speedBytesPerSec / 1024f)
                else -> String.format("%.1fMB/s", speedBytesPerSec / 1024f / 1024f)
            }
        }

        private fun getNetworkSpeed(): Float {
            val currentTime = System.currentTimeMillis()
            val rxBytes = TrafficStats.getTotalRxBytes()
            val txBytes = TrafficStats.getTotalTxBytes()

            if (lastNetworkTime == 0L || rxBytes == TrafficStats.UNSUPPORTED.toLong()) {
                lastRxBytes = rxBytes
                lastTxBytes = txBytes
                lastNetworkTime = currentTime
                return 0f
            }

            val timeDelta = currentTime - lastNetworkTime
            if (timeDelta <= 0) return 0f

            val rxDelta = rxBytes - lastRxBytes
            val txDelta = txBytes - lastTxBytes
            val totalBytes = rxDelta + txDelta

            lastRxBytes = rxBytes
            lastTxBytes = txBytes
            lastNetworkTime = currentTime

            val speedBytesPerSec = (totalBytes.toFloat() / (timeDelta / 1000f))
            return speedBytesPerSec.coerceAtLeast(0f)
        }

        private fun getCpuUsage(): Int {
            return try {
                val reader = RandomAccessFile("/proc/stat", "r")
                val load = reader.readLine()
                reader.close()

                val toks = load.split(" +".toRegex())

                if (toks.size < 9) {
                    Log.w("SystemStatsWidget", "Unexpected /proc/stat format, reporting last known CPU usage")
                    return lastCpuUsage
                }

                val idle = toks[4].toLongOrNull() ?: return lastCpuUsage
                val cpu = (toks[1].toLongOrNull() ?: 0L) +
                         (toks[2].toLongOrNull() ?: 0L) +
                         (toks[3].toLongOrNull() ?: 0L) +
                         (toks[5].toLongOrNull() ?: 0L) +
                         (toks[6].toLongOrNull() ?: 0L) +
                         (toks[7].toLongOrNull() ?: 0L) +
                         (toks[8].toLongOrNull() ?: 0L)

                val usage = if (lastCpuTotal != 0L && lastCpuIdle != 0L) {
                    val cpuDelta = cpu - lastCpuTotal
                    val idleDelta = idle - lastCpuIdle
                    val totalDelta = cpuDelta + idleDelta
                    if (totalDelta > 0) {
                        ((cpuDelta.toFloat() / totalDelta.toFloat()) * 100f).roundToInt()
                    } else {
                        val total = cpu + idle
                        if (total > 0) {
                            ((cpu.toFloat() / total.toFloat()) * 100f).roundToInt()
                        } else {
                            0
                        }
                    }
                } else {
                    val total = cpu + idle
                    if (total > 0) {
                        ((cpu.toFloat() / total.toFloat()) * 100f).roundToInt()
                    } else {
                        0
                    }
                }

                lastCpuTotal = cpu
                lastCpuIdle = idle
                lastCpuUsage = usage

                usage
            } catch (e: Exception) {
                Log.w("SystemStatsWidget", "Failed to read /proc/stat, reporting last known CPU usage", e)
                lastCpuUsage
            }
        }

        private fun getMemoryUsage(context: Context): Int {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            val totalMemory = memoryInfo.totalMem.toDouble()
            val availableMemory = memoryInfo.availMem.toDouble()
            val usedMemory = totalMemory - availableMemory

            return ((usedMemory / totalMemory) * 100).roundToInt()
        }

        private fun getBatteryLevel(context: Context): Int {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            } else {
                val batteryIntent = context.registerReceiver(
                    null,
                    android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100f).roundToInt()
                } else {
                    0
                }
            }
        }
    }
}
