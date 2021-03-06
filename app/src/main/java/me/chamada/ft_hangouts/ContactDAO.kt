package me.chamada.ft_hangouts

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDAO {
    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): Flow<List<Contact>>

/*
    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getOne(id: Int): Flow<Contact>
*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Update()
    suspend fun update(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAll()
}