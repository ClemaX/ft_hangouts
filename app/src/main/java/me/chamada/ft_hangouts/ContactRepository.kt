package me.chamada.ft_hangouts

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val dao: ContactDAO) {
    val all: Flow<List<Contact>> = dao.getAll()

/*
    @WorkerThread
    suspend fun getOne(id: Int) = dao.getOne(id)
*/
    
    @WorkerThread
    suspend fun insert(contact: Contact) {
        dao.insert(contact)
    }

    @WorkerThread
    suspend fun update(contact: Contact) {
        dao.update(contact)
    }
}