package com.example.flutter_step_counter.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    // 📌 全レコード取得（最新日付・時間順）
    @Query("SELECT * FROM step_record ORDER BY date DESC, segment DESC, time DESC")
    fun getAll(): List<StepRecord>

    // 📌 Flowでのリアルタイム監視
    @Query("SELECT * FROM step_record ORDER BY date DESC, segment DESC, time DESC")
    fun watchAll(): Flow<List<StepRecord>>

    // 📌 特定日付のすべてのレコードを取得（segment順に）
    @Query("SELECT * FROM step_record WHERE date = :date ORDER BY segment ASC")
    fun getAllByDate(date: String): List<StepRecord>

    // 📌 特定日付の最新の segment レコードを取得
    @Query("SELECT * FROM step_record WHERE date = :date ORDER BY segment DESC LIMIT 1")
    fun getLatestByDate(date: String): StepRecord?

    // 📌 挿入（segmentが異なるなら別レコード扱い）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: StepRecord)

    // 📌 更新（id一致時のみ）
    @Update
    fun update(record: StepRecord)

    // 📌 全削除
    @Query("DELETE FROM step_record")
    fun deleteAll()
}
