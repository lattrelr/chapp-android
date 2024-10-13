package com.ryanl.chapp.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ryanl.chapp.persist.models.History

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: History)
    @Query("SELECT * FROM History")
    suspend fun getHistories(): List<History>
    @Query("SELECT * FROM History WHERE :userId==id")
    suspend fun getHistory(userId: String): History?
    @Update
    suspend fun update(history: History)
}