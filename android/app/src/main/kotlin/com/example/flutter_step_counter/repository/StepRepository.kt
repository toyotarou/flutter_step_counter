package com.example.flutter_step_counter.repository

import com.example.flutter_step_counter.db.StepDao
import com.example.flutter_step_counter.db.StepRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepRepository(private val stepDao: StepDao) {
    fun saveStep(currentStep: Float) {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val date = dateFormat.format(now)
        val time = timeFormat.format(now)
        val step = currentStep.toInt()

        CoroutineScope(Dispatchers.IO).launch {
            val record = StepRecord(
                date = date,
                time = time,
                step = step
            )
            stepDao.insert(record)
        }
    }
}
