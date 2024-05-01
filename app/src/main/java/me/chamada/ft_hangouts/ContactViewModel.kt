package me.chamada.ft_hangouts

import androidx.lifecycle.*
import kotlinx.coroutines.launch

val nameCharPool = ('a'..'z') + ('A'..'Z') + ' '
const val nameMinLength = 5
const val nameMaxLength = 20

val random = kotlin.random.Random(6047)

fun randomName(length: Int = kotlin.random.Random.nextInt(nameMinLength, nameMaxLength)): String {
    return (1..length)
        .map {
            if (random.nextInt(0, 11) == 10) nameCharPool.size - 1
            else random.nextInt(0, nameCharPool.size - 1)
        }
        .map(nameCharPool::get)
        .joinToString("")
        .trimStart()
}

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    var all: LiveData<List<Contact>> = repository.all.asLiveData()
    var current: Contact? = null

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun update(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
    }

    fun preSeed(count: Int = 100) = viewModelScope.launch {
        for (i in 1..count)
            repository.insert(Contact(id = 0, name = randomName(), phoneNumber = "+33 7 77 77 77"))
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

class ContactViewModelFactory(private val repository: ContactRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}