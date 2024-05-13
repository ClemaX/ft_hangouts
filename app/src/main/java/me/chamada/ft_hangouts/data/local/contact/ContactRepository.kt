package me.chamada.ft_hangouts.data.local.contact

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.contact.Contact

class ContactRepository(private val dao: ContactDAO) {
    val all: Flow<List<Contact>> = dao.getAll()

    @WorkerThread
    fun getById(id: Long): Flow<Contact> {
        return dao.getById(id)
    }

    @WorkerThread
    suspend fun insert(contact: Contact) {
        dao.insert(contact)
    }

    @WorkerThread
    suspend fun update(contact: Contact) {
        dao.update(contact)
    }

    @WorkerThread
    suspend fun deleteAll() {
        dao.deleteAll()
    }

    @WorkerThread
    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}