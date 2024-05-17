package me.chamada.ft_hangouts.data.local.conversation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationPreview
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor

@Dao
interface ConversationDAO {
    @Transaction
    @Query(
        "SELECT "
        + "ic.name AS contact_name, "
        + "c.* "
        + "FROM conversations c "
        + "LEFT JOIN interlocutors AS i ON i.conversation_id = c.id "
        + "LEFT JOIN contacts AS ic ON ic.phone_number = i.phone_number "
        + "ORDER BY c.id DESC "
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

    @Update
    suspend fun update(conversation: Conversation)
    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)
}