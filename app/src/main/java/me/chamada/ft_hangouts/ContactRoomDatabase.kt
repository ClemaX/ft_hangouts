package me.chamada.ft_hangouts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactRoomDatabase : RoomDatabase() {
    private class ContactDatabaseCallback(private val scope: CoroutineScope)
        : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database -> scope.launch { populateDatabase(database.dao()) } }
        }

        suspend fun populateDatabase(dao: ContactDAO) {
            dao.deleteAll()

            val testContact = Contact(id = 0, name = "Full Name", phoneNumber = "+33 7 77 77 77")
            dao.insert(testContact)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ContactRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ContactRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactRoomDatabase::class.java,
                    "contacts"
                )
                    .addCallback(ContactDatabaseCallback(scope))
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }

    abstract fun dao(): ContactDAO
}