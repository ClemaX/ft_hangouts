package me.chamada.ft_hangouts

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.chamada.ft_hangouts.data.local.contact.ContactRepository
import me.chamada.ft_hangouts.data.local.HangoutsRoomDatabase
import me.chamada.ft_hangouts.data.local.conversation.ConversationRepository

class HangoutsApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { HangoutsRoomDatabase.getDatabase(this, applicationScope) }

    val contactRepository by lazy { ContactRepository(database.contactDAO()) }
    val conversationRepository by lazy { ConversationRepository(database.conversationDAO()) }

    override fun onCreate() {
        super.onCreate()

        val lifecycleObserver = LifecycleObserver()

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private inner class LifecycleObserver : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            val lifecyclePreferences = getSharedPreferences("lifecycle", MODE_PRIVATE)

            lifecyclePreferences.edit {
                putLong("lastStop", System.currentTimeMillis())
            }

            super.onStop(owner)
        }
    }
}