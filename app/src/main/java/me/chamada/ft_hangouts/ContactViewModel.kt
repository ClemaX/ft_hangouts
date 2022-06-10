package me.chamada.ft_hangouts

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    var all: LiveData<List<Contact>> = repository.all.asLiveData()
    var current: Contact? = null

/*
    fun updateCurrent() {
        current?.let { update(it) }
    }
*/
/*
    fun getOne(id: Int) = viewModelScope.launch{
        repository.getOne(id)
    }
*/
    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun update(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
    }
}

class ContactViewModelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}