package me.chamada.ft_hangouts.data.model.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    @ColumnInfo(name = "phone_number") val phoneNumber: String = ""
)