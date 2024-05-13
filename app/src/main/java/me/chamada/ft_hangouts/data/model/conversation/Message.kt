package me.chamada.ft_hangouts.data.model.conversation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

// Many messages have one conversation
// Many messages have one interlocutor (sender)
@Entity(tableName = "messages", indices = [Index(value = ["created_at"])])
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "conversation_id") val conversationId: Long,
    @ColumnInfo(name = "sender_id") val senderId: Long,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    val content: String
)

data class DetailedMessage(
    @Embedded val message: Message,

    @Relation(
        parentColumn = "id",
        entityColumn = "sender_id",
    )
    val sender: Interlocutor,
)