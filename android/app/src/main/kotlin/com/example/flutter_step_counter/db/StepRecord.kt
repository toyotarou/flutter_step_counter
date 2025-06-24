package com.example.flutter_step_counter.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_record")
data class StepRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,      // yyyy-MM-dd
    val time: String,      // HH:mm:ss
    val step: Int          // センサーから取得した歩数
)
