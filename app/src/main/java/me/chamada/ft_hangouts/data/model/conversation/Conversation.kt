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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)

data class ConversationWithInterlocutor(
    @Embedded val conversation: Conversation = Conversation(),

    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id",
    )
    val interlocutor: Interlocutor = Interlocutor(),
)


data class DetailedConversation(
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