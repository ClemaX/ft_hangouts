package me.chamada.ft_hangouts.ui.conversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.chamada.ft_hangouts.data.local.conversation.ConversationRepository
import me.chamada.ft_hangouts.data.model.conversation.Conversation
import me.chamada.ft_hangouts.data.model.conversation.ConversationPreview
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithContact
import me.chamada.ft_hangouts.data.model.conversation.Interlocutor

class ConversationViewModel(private val repository: ConversationRepository) : ViewModel() {
    private val _currentId = MutableLiveData(0L)

    private val initialConversation = MutableLiveData(
        ConversationWithContact()
    )

    val all: LiveData<List<ConversationPreview>> = repository.all.asLiveData()

    private val currentId: LiveData<Long> get() = _currentId

    val current: LiveData<ConversationWithContact> = currentId.switchMap { id ->
        when(id) {
            0L -> initialConversation
            else -> repository.getById(id).asLiveData()
        }
    }

    class Factory(private val repository: ConversationRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConversationViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun select(id: Long) {
        _currentId.postValue(id)
    }

    fun delete(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
    }

    /*fun findByPhoneNumber(phoneNumber: String) = viewModelScope.launch {
        repository.findByPhoneNumber(phoneNumber)
    }*/

    private fun createAndSelect(phoneNumber: String) = viewModelScope.launch {
            val conversation = Conversation()
            var interlocutor = repository.findInterlocutorByPhoneNumber(phoneNumber)

            val conversationId = repository.insert(conversation)

            if (interlocutor == null) {
                interlocutor = Interlocutor( conversationId = conversationId, phoneNumber = phoneNumber)

                repository.insertInterlocutor(interlocutor)
            }

            select(conversationId)
        }

    fun selectOrCreate(phoneNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        val existingConversation = repository.findByPhoneNumber(phoneNumber)

        if (existingConversation != null) {
            select(existingConversation.id)
        }
        else {
            createAndSelect(phoneNumber)
        }
    }
}