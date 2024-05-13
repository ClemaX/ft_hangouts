package me.chamada.ft_hangouts.data.local.conversation

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithInterlocutor
import me.chamada.ft_hangouts.data.model.conversation.DetailedConversation
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor

class ConversationRepository(private val dao: ConversationDAO) {
    val all: Flow<List<DetailedConversation>> = dao.getAll()

    @WorkerThread
    fun getById(id: Int): Flow<ConversationWithInterlocutor> {
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