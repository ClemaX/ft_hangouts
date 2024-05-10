package me.chamada.ft_hangouts.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.chamada.ft_hangouts.data.local.contact.ContactDAO
import me.chamada.ft_hangouts.data.local.conversation.ConversationDAO
import me.chamada.ft_hangouts.data.model.contact.Contact
import me.chamada.ft_hangouts.data.model.conversation.Conversation

@Database(entities = [Contact::class, Conversation::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HangoutsRoomDatabase : RoomDatabase() {
    private class ContactDatabaseCallback(private val scope: CoroutineScope) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.contactDAO(), database.conversationDAO())
                }
            }
        }

        suspend fun populateDatabase(contactDAO: ContactDAO, conversationDAO: ConversationDAO) {
            contactDAO.deleteAll()
            conversationDAO.deleteAll()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HangoutsRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HangoutsRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HangoutsRoomDatabase::class.java,
                    "hangouts"
                )
                    .addCallback(ContactDatabaseCallback(scope))
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }

    abstract fun contactDAO(): ContactDAO
    abstract fun conversationDAO(): ConversationDAO
}