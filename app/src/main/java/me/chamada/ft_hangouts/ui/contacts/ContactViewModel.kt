package me.chamada.ft_hangouts.ui.contacts

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.chamada.ft_hangouts.data.local.contact.ContactRepository
import me.chamada.ft_hangouts.data.model.contact.Contact


class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    private val _currentId = MutableLiveData(0L)
    private val initialContact: LiveData<Contact> = MutableLiveData(Contact())

    val all: LiveData<List<Contact>> = repository.all.asLiveData()
    val currentId: LiveData<Long> get() = _currentId

    val current: LiveData<Contact> = currentId.switchMap { id ->
        when(id) {
            0L -> initialContact
            else -> repository.getById(id).asLiveData()
        }
    }

    companion object {
        private const val NAME_LENGTH_MIN = 5
        private const val NAME_LENGTH_MAX = 20

        private val nameCharPool = ('a'..'z') + ('A'..'Z') + ' '

        private val random = kotlin.random.Random(System.nanoTime())

        fun randomName(length: Int = kotlin.random.Random.nextInt(NAME_LENGTH_MIN, NAME_LENGTH_MAX)): String {
            return (1..length)
                .map {
                    if (random.nextInt(0, 11) == 10) nameCharPool.size - 1
                    else random.nextInt(0, nameCharPool.size - 1)
                }
                .map(nameCharPool::get)
                .joinToString("")
                .trimStart()
        }

        fun randomPhoneNumber(): String {
            val parts = intArrayOf(
                random.nextInt(10, 60),
                random.nextInt(0,10),
                *(1..3).map { random.nextInt(0, 10) * 10 + random.nextInt(0, 10) }.toIntArray())
            return "+" + parts.joinToString(" ")
        }
    }

    class Factory(private val repository: ContactRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ContactViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun select(contactId: Long) {
        _currentId.postValue(contactId)
    }

    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    fun update(contact: Contact) = viewModelScope.launch {
        repository.update(contact)
    }

    fun preSeed(count: Int = 100) = viewModelScope.launch {
        for (i in 1..count)
            repository.insert(Contact(id = 0, name = randomName(), phoneNumber = randomPhoneNumber()))
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
    }
}
