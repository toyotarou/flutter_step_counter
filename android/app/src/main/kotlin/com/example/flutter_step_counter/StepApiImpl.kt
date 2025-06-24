package com.example.flutter_step_counter

import android.content.Context
import com.example.flutter_step_counter.db.AppDatabase
import com.example.flutter_step_counter.sensors.StepSensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.example.flutter_step_counter.db.StepRecord as DbStepRecord

class StepApiImpl(private val context: Context) : StepApi {

    override fun getAllStepRecords(): List<StepRecord> = runBlocking {
        return@runBlocking withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.stepDao()
            val dbRecords: List<DbStepRecord> = dao.getAll()

            val pigeonRecords: List<StepRecord> = dbRecords.map {
                StepRecord(


                    date = it.date,
                    segment = it.segment.toLong(),
                    time = it.time,
                    step = it.step.toLong()


                )
            }

            pigeonRecords
        }
    }

    override fun isServiceRunning(): Boolean {
        return StepSensorManager.isRunning()
    }

    override fun getCurrentStep(): Long {
        return StepSensorManager.getCurrentStep().toLong()
    }
}
