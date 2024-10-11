package com.ryanl.chapp.persist

import androidx.room.Dao
import androidx.room.Insert
import com.ryanl.chapp.persist.models.History

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: History)
}