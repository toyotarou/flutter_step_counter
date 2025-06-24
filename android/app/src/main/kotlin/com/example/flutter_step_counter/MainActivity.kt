package com.example.flutter_step_counter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flutter_step_counter.sensors.StepSensorManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {

    private lateinit var stepSensorManager: StepSensorManager

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔧 センサー初期化
        stepSensorManager = StepSensorManager(this)

        // 🎯 Android 10 以降の ACTIVITY_RECOGNITION パーミッション確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION
                )
            } else {
                stepSensorManager.register()
            }
        } else {
            // Android 9 以下はそのまま登録
            stepSensorManager.register()
        }
    }

    // 🔧 Pigeon API 登録
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        StepApi.setUp(flutterEngine.dartExecutor.binaryMessenger, StepApiImpl(this))
    }

    // 🎯 パーミッション許可時の処理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            stepSensorManager.register()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stepSensorManager.unregister()
    }
}
