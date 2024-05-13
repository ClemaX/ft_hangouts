package me.chamada.ft_hangouts.data.model.conversation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// One conversation has one interlocutor
// One conversation has many messages

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

data class ConversationWithContact(
    @Embedded val conversation: Conversation = Conversation(),

    /*@Relation(
        parentColumn = "id",
        entityColumn = "conversation_id",
    )
    val interlocutor: Interlocutor = Interlocutor(),*/

    @ColumnInfo(name = "interlocutor_phone_number") val interlocutorPhoneNumber: String = "",

    @ColumnInfo(name = "contact_id") val contactId: Long? = null,
    @ColumnInfo(name = "contact_name") val contactName: String? = null,
)


data class ConversationPreview(
    @Embedded val conversation: Conversation = Conversation(),
    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id",
    )
    val interlocutor: Interlocutor = Interlocutor(),

    @ColumnInfo(name = "last_message_sender_phone_number") val lastMessageSenderPhoneNumber: String? = null,
    @ColumnInfo(name = "last_message_sender_name") val lastMessageSenderName: String? = null,
    @ColumnInfo(name = "last_message_content") val lastMessageContent: String? = null
)