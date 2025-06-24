package com.example.flutter_step_counter.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensorManager(private val context: Context) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    companion object {
        private var isRunning = false
        private var currentStep: Int = 0

        fun isRunning(): Boolean = isRunning
        fun getCurrentStep(): Int = currentStep
    }

    fun register() {
        if (isRunning) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            isRunning = true
            Log.d("StepSensorManager", "✅ センサー登録")
        } ?: Log.w("StepSensorManager", "⚠️ TYPE_STEP_COUNTER 未対応")
    }

    fun unregister() {
        sensorManager?.unregisterListener(this)
        isRunning = false
        Log.d("StepSensorManager", "🛑 センサー解除")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            currentStep = event.values[0].toInt()
            Log.d("StepSensorManager", "👣 歩数: $currentStep")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 無視
    }
}
