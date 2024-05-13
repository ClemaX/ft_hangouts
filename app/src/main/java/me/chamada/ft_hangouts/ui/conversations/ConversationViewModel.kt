package me.chamada.ft_hangouts.ui.conversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.chamada.ft_hangouts.data.local.contact.ContactRepository
import me.chamada.ft_hangouts.data.local.conversation.ConversationRepository
import me.chamada.ft_hangouts.data.model.conversation.ConversationWithInterlocutor
import me.chamada.ft_hangouts.data.model.conversation.DetailedConversation
import me.chamada.ft_hangouts.ui.contacts.ContactViewModel

class ConversationViewModel(private val repository: ConversationRepository) : ViewModel() {
    private val _currentId = MutableLiveData(0)
    private val initialConversation = MutableLiveData(
        ConversationWithInterlocutor()
    )

    val all: LiveData<List<DetailedConversation>> = repository.all.asLiveData()

    val currentId: LiveData<Int> get() = _currentId

    val current: LiveData<ConversationWithInterlocutor> = currentId.switchMap { id ->
        when(id) {
            0 -> initialConversation
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

    fun select(id: Int) {
        _currentId.postValue(id)
    }

    fun delete(id: Int) = viewModelScope.launch {
        repository.deleteById(id)
    }
}