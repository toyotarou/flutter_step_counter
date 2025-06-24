package com.example.flutter_step_counter.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.flutter_step_counter.repository.StepRepository
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.sensors.StepSensorManager
import kotlinx.coroutines.*

class StepUpdateService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var stepRepository: StepRepository
    private lateinit var stepSensorManager: StepSensorManager

    companion object {
        var isRunning: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        Log.d("StepUpdateService", "🚀 サービス起動")

        stepSensorManager = StepSensorManager(this)
        stepSensorManager.register()

        val db = AppDatabase.getDatabase(applicationContext)
        stepRepository = StepRepository(db.stepDao())

        startForegroundWithNotification()
        startRepeatingTask()
    }

    private fun startRepeatingTask() {
        runnable = object : Runnable {
            override fun run() {
                val step = StepSensorManager.getCurrentStep()

                Log.d("StepUpdateService", "💾 saveStep called from Service: $step")
                stepRepository.saveStep(step.toFloat())

                handler.postDelayed(this, 60_000)
            }
        }

        handler.post(runnable)
    }

    private fun startForegroundWithNotification() {
        val channelId = "step_service_channel"
        val channelName = "Step Update Service"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
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
        stepSensorManager.unregister()
        Log.d("StepUpdateService", "🛑 サービス停止")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
