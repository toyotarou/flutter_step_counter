package com.example.flutter_step_counter.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    // 通常の一覧取得（ワンショット）
    @Query("SELECT * FROM step_record ORDER BY date DESC, time DESC")
    fun getAll(): List<StepRecord>

    // Flowでの監視用（リアクティブに更新）
    @Query("SELECT * FROM step_record ORDER BY date DESC, time DESC")
    fun watchAll(): Flow<List<StepRecord>>

    // 特定日付のレコード取得
    @Query("SELECT * FROM step_record WHERE date = :date LIMIT 1")
    fun getByDate(date: String): StepRecord?

    // 挿入（重複時は置換）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(record: StepRecord)

    // 更新
    @Update
    fun update(record: StepRecord)

    // 全削除
    @Query("DELETE FROM step_record")
    fun deleteAll()
}
