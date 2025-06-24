package com.example.flutter_step_counter.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.repository.StepRepository

class StepSensorManager(
    private val context: Context
) : SensorEventListener {

    private val TAG = "StepSensorManager"

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private lateinit var stepRepository: StepRepository

    fun register() {
        Log.d(TAG, "✅ register() called")

        val db = AppDatabase.getDatabase(context)
        stepRepository = StepRepository(db.stepDao())

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            Log.e(TAG, "❌ SensorManager is null")
            return
        }

        stepCounterSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor == null) {
            Log.e(TAG, "❌ StepCounterSensor not available")
            return
        }

        val success = sensorManager!!.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        Log.d(
            TAG,
            if (success) "✅ Step sensor registered successfully" else "❌ Failed to register step sensor"
        )
    }

    fun unregister() {
        Log.d(TAG, "🛑 unregister() called")
        sensorManager?.unregisterListener(this)
        sensorManager = null
        stepCounterSensor = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            Log.w(TAG, "⚠️ onSensorChanged called with null event")
            return
        }

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0]
            Log.d(TAG, "🚶 onSensorChanged - StepCounter value: $steps")
            try {
                stepRepository.saveStep(steps) // Repository経由で保存
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in saveStep: ${e.message}", e)
            }
        } else {
            Log.w(TAG, "⚠️ Unexpected sensor type: ${event.sensor.type}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "📡 Sensor accuracy changed: $accuracy for sensor: ${sensor?.name}")
    }
}
