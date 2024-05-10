package me.chamada.ft_hangouts.data.model.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,

    @ColumnInfo(
        name = "name",
        defaultValue = ""
    )
    val name: String = "",

    @ColumnInfo(
        name = "recipient_phone_numbers",
    )
    val recipientPhoneNumbers: List<String> = emptyList()
)