package me.chamada.ft_hangouts.data.local.conversation

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithInterlocutor
import me.chamada.ft_hangouts.data.model.conversation.DetailedConversation
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor
import me.chamada.ft_hangouts.data.model.conversation.Message

@Dao
interface ConversationDAO {
    @Transaction
    @Query(
        "SELECT DISTINCT (m.conversation_id), "
        + "m.id AS last_message_id, m.content AS last_message_content, "
        + "s.phone_number AS last_message_sender_phone_number, "
        + "sc.name AS last_message_sender_name, "
        + "c.* "
        + "FROM messages m "
        + "JOIN conversations AS c ON c.id = m.conversation_id "
        + "JOIN interlocutors AS s ON s.id = m.sender_id "
        + "JOIN contacts AS sc ON sc.phone_number = s.phone_number "
        + "ORDER BY m.conversation_id, m.id DESC"
    )
    fun getAll(): Flow<List<DetailedConversation>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getById(id: Int): Flow<ConversationWithInterlocutor>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation)

    @Update
    suspend fun update(conversation: Conversation)
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Int)
}