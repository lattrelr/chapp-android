package com.ryanl.chapp.persist

import androidx.room.Dao
import androidx.room.Insert
import com.ryanl.chapp.persist.models.Message

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(msg: Message)
}