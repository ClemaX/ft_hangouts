package me.chamada.ft_hangouts.data.local.conversation

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationPreview
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor
import me.chamada.ft_hangouts.data.model.conversation.Message

@Dao
interface ConversationDAO {
    @Transaction
    @Query(
        "SELECT "
        + "m.id AS last_message_id, m.content AS last_message_content, "
        + "m.sender_id AS last_message_sender_id, "
        + "ic.name AS contact_name, "
        + "c.* "
        + "FROM messages m "
        + "JOIN conversations AS c ON c.id = m.conversation_id "
        + "LEFT JOIN interlocutors AS i ON i.conversation_id = c.id "
        + "LEFT JOIN contacts AS ic ON ic.phone_number = i.phone_number "
        + "ORDER BY m.conversation_id, m.id DESC "
        + "LIMIT 1"
    )
    @RewriteQueriesToDropUnusedColumns
    fun getAll(): Flow<List<ConversationPreview>>

    @Transaction
    @Query(
        "SELECT conversation.*, "
        + "interlocutor.phone_number AS interlocutor_phone_number, "
        + "contact.name AS contact_name, contact.id AS contact_id "
        + "FROM conversations conversation "
        + "JOIN interlocutors interlocutor ON interlocutor.conversation_id = conversation.id "
        + "JOIN contacts contact ON contact.phone_number = interlocutor.phone_number "
        + "WHERE conversation.id = :id"
    )
    fun getById(id: Long): Flow<ConversationWithContact>

    @Query(
        "SELECT message.* FROM messages message "
        + "WHERE message.conversation_id = :id "
        + "ORDER BY message.id ASC"
    )
    fun getMessages(id: Long): Flow<List<Message>>


    @Query(
        "SELECT * FROM conversations "
        + "JOIN interlocutors i ON i.conversation_id "
        + "WHERE i.phone_number = :phoneNumber"
    )
    @RewriteQueriesToDropUnusedColumns
    fun findByPhoneNumber(phoneNumber: String): Conversation?

    @Query(
        "SELECT * FROM interlocutors i "
        + "WHERE i.phone_number = :phoneNumber"
    )
    suspend fun findInterlocutorByPhoneNumber(phoneNumber: String): Interlocutor?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(conversation: Conversation): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInterlocutor(interlocutor: Interlocutor): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun update(conversation: Conversation)
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)
}