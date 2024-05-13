package me.chamada.ft_hangouts.data.local.conversation

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.data.model.conversation.ConversationPreview
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor
import me.chamada.ft_hangouts.data.model.conversation.Message

class ConversationRepository(private val dao: ConversationDAO) {
    val all: Flow<List<ConversationPreview>> = dao.getAll()

    @WorkerThread
    fun getById(id: Long): Flow<ConversationWithContact> {
        return dao.getById(id)
    }

    @WorkerThread
    fun findByPhoneNumber(phoneNumber: String): Conversation? {
        return dao.findByPhoneNumber(phoneNumber)
    }

    @WorkerThread
    suspend fun findInterlocutorByPhoneNumber(phoneNumber: String): Interlocutor? {
        return dao.findInterlocutorByPhoneNumber(phoneNumber)
    }

    @WorkerThread
    suspend fun insert(conversation: Conversation): Long {
        return dao.insert(conversation)
    }

    @WorkerThread
    suspend fun insertInterlocutor(interlocutor: Interlocutor): Long {
        return dao.insertInterlocutor(interlocutor)
    }

    @WorkerThread
    suspend fun insertMessage(message: Message): Long {
        return dao.insertMessage(message)
    }

    @WorkerThread
    fun getMessages(id: Long): Flow<List<Message>> {
        return dao.getMessages(id)
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
    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}