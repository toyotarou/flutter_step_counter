package com.example.flutter_step_counter.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_record")
data class StepRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,      // yyyy-MM-dd
    val segment: Int,      // 0: 通常, 1以降: 再起動後
    val time: String,      // HH:mm:ss
    val step: Int          // センサーから取得した歩数
)
