package com.example.flutter_step_counter.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.db.StepRecord
import com.example.flutter_step_counter.sensors.StepDataManager
import com.example.flutter_step_counter.HelloStepActivity
import kotlinx.coroutines.*

class StepUpdateService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    companion object {
        var isRunning: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Log.d("StepUpdateService", "🚀 サービス起動")

        startForegroundWithNotification()
        startRepeatingTask()
    }

    private fun startRepeatingTask() {
        val context = applicationContext
        val db = AppDatabase.getDatabase(context)
        val stepDao = db.stepDao()

        runnable = object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    val today = StepDataManager.getCurrentDate()
                    val time = StepDataManager.getCurrentTime()
                    val step = HelloStepActivity.steps.value

                    val existing = stepDao.getByDate(today)
                    if (existing == null) {
                        stepDao.insert(StepRecord(date = today, time = time, step = step))
                        Log.d("StepUpdateService", "🆕 データ新規登録: $step（🕒 $time）")
                    } else {
                        stepDao.update(existing.copy(step = step, time = time))
                        Log.d("StepUpdateService", "♻️ データ更新: $step（🕒 $time）")
                    }
                }
                handler.postDelayed(this, 60_000)
            }
        }

        handler.post(runnable)
    }

    private fun startForegroundWithNotification() {
        val channelId = "step_service_channel"
        val channelName = "Step Update Service"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("歩数データを記録中")
            .setContentText("60秒ごとにステップ数を保存しています")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(runnable)
        Log.d("StepUpdateService", "🛑 サービス停止")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
