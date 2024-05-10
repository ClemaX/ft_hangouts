package me.chamada.ft_hangouts.data.local.conversation

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation

class ConversationRepository(private val dao: ConversationDAO) {
    val all: Flow<List<Conversation>> = dao.getAll()

    @WorkerThread
    fun getById(id: Int): Flow<Conversation> {
        return dao.getById(id)
    }

    @WorkerThread
    suspend fun insert(conversation: Conversation) {
        dao.insert(conversation)
    }

    @WorkerThread
    suspend fun update(conversation: Conversation) {
        dao.update(conversation)
    }

    @WorkerThread
    suspend fun deleteAll() {
        dao.deleteAll()
    }

    @WorkerThread
    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }
}