package com.example.flutter_step_counter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.db.StepRecord
import com.example.flutter_step_counter.service.StepServiceManager
import com.example.flutter_step_counter.sensors.StepDataManager
import com.example.flutter_step_counter.sensors.StepSensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HelloStepActivity : ComponentActivity() {

    private lateinit var stepSensorManager: StepSensorManager

    companion object {
        val steps = mutableStateOf(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(applicationContext)
        val stepDao = db.stepDao()

        stepSensorManager = StepSensorManager(this) { stepCount ->
            Log.d("HelloStepActivity", "👣 歩数: $stepCount")
            steps.value = stepCount.toInt()
        }

        checkPermissionAndStartSensor()

        setContent {
            val stepListState = remember { mutableStateOf(listOf<StepRecord>()) }
            val scope = rememberCoroutineScope()
            val stepValue by steps
            val countdown = remember { mutableStateOf(60) }

            // カウントダウンを1秒ごとに更新
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000)
                    countdown.value = (countdown.value - 1).coerceAtLeast(0)
                    if (countdown.value == 0) {
                        countdown.value = 60
                    }
                }
            }

            // Roomのデータを5秒ごとに再取得
            LaunchedEffect(Unit) {
                while (true) {
                    delay(5000)
                    val list = withContext(Dispatchers.IO) {
                        stepDao.getAll()
                    }
                    stepListState.value = list
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("👣 現在の歩数: $stepValue")
                    Text(
                        "⏳ 次の保存まで: ${countdown.value} 秒",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val today = getToday()
                            val nowTime = StepDataManager.getCurrentTime()
                            val stepCount = steps.value
                            val existing = stepDao.getByDate(today)

                            if (existing == null) {
                                stepDao.insert(
                                    StepRecord(
                                        date = today,
                                        time = nowTime,
                                        step = stepCount
                                    )
                                )
                            } else {
                                stepDao.update(
                                    existing.copy(
                                        step = stepCount,
                                        time = nowTime
                                    )
                                )
                            }

                            val list = stepDao.getAll()
                            withContext(Dispatchers.Main) {
                                stepListState.value = list
                            }
                        }
                    }) {
                        Text("🦶 手動で保存/更新")
                    }

                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            stepDao.deleteAll()
                            withContext(Dispatchers.Main) {
                                stepListState.value = listOf()
                            }
                        }
                    }) {
                        Text("🗑 データを全削除")
                    }

                    Button(onClick = {
                        StepServiceManager.start(this@HelloStepActivity)
                        countdown.value = 60
                    }) {
                        Text("▶️ サービス開始")
                    }

                    Button(onClick = {
                        StepServiceManager.stop(this@HelloStepActivity)
                    }) {
                        Text("⏹ サービス停止")
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("📋 保存されたステップ一覧", style = MaterialTheme.typography.titleMedium)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(stepListState.value) { record ->
                            Text("📅 ${record.date} ：👣 ${record.step} 歩（🕒 ${record.time}）")
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissionAndStartSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            stepSensorManager.register()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Log.d("HelloStepActivity", "🔑 ACTIVITY_RECOGNITION granted: $isGranted")
            if (isGranted) stepSensorManager.register()
        }

    override fun onDestroy() {
        super.onDestroy()
        stepSensorManager.unregister()
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
