package me.chamada.ft_hangouts.data.local.conversation

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation

@Dao
interface ConversationDAO {
    @Query("SELECT * FROM conversations ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getById(id: Int): Flow<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation)

    @Update
    suspend fun update(conversation: Conversation)
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Int)
}