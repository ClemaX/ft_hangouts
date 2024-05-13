package me.chamada.ft_hangouts.data.model.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interlocutors")
data class Interlocutor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "conversation_id") val conversationId: Int = 0,
    @ColumnInfo(name = "phone_number") val phoneNumber: String = ""
)