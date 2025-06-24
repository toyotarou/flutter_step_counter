package com.example.flutter_step_counter.repository

import com.example.flutter_step_counter.db.StepDao
import com.example.flutter_step_counter.db.StepRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepRepository(private val stepDao: StepDao) {

    private var lastStep: Int? = null  // 前回保存された歩数

    fun saveStep(currentStep: Float) {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val date = dateFormat.format(now)
        val time = timeFormat.format(now)
        val step = currentStep.toInt()

        CoroutineScope(Dispatchers.IO).launch {
            val latest = stepDao.getLatestByDate(date)

            // 再起動の判定
            if (latest == null) {
                // 初回記録（segment = 0）
                stepDao.insert(
                    StepRecord(date = date, segment = 0, time = time, step = step)
                )
                lastStep = step
            } else if (step < latest.step) {
                // センサー値が小さくなった → 再起動と判断し segment を分ける
                val nextSegment = latest.segment + 1
                stepDao.insert(
                    StepRecord(date = date, segment = nextSegment, time = time, step = step)
                )
                lastStep = step
            } else {
                // 通常更新（step値更新）
                val updated = latest.copy(step = step, time = time)
                stepDao.update(updated)
                lastStep = step
            }
        }
    }
}
