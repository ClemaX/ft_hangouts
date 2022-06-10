package me.chamada.ft_hangouts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact (
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name", defaultValue = "") val name: String = "",
    @ColumnInfo(name = "phone_number", defaultValue = "") val phoneNumber: String = "") {
}