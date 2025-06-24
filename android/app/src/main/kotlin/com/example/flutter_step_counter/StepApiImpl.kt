package com.example.flutter_step_counter

import android.content.Context
import com.example.flutter_step_counter.StepApi
import com.example.flutter_step_counter.StepRecord as PigeonStepRecord
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.db.StepRecord as DbStepRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class StepApiImpl(private val context: Context) : StepApi {
    override fun getCurrentStep(): Long {
        return 0L
    }

    override fun getAllStepRecords(): List<PigeonStepRecord> = runBlocking {
        val dbRecords = withContext(Dispatchers.IO) {
            val stepDao = AppDatabase.getDatabase(context).stepDao()
            stepDao.getAll()
        }

        dbRecords.map {
            PigeonStepRecord(
                date = it.date,
                segment = it.segment.toLong(),
                time = it.time,
                step = it.step.toLong()
            )
        }
    }
}
