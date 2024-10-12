package com.ryanl.chapp.persist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ryanl.chapp.persist.models.Message

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(msg: Message)
    @Query(
        "SELECT * FROM messages WHERE " +
        "([from]==:user1 AND [to] == :user2) OR " +
        "([from]==:user2 AND [to] == :user1) " +
        "ORDER BY date"
    )
    suspend fun getConversation(user1: String, user2: String): List<Message>
}