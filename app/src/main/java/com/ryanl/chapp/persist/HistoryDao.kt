package com.ryanl.chapp.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ryanl.chapp.persist.models.History

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: History)
    @Query("SELECT * FROM History")
    suspend fun getHistories(): List<History>
}