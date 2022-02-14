package io.xxlabs.messenger.application

import android.app.Application
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.security.crypto.EncryptedSharedPreferences
import io.xxlabs.messenger.data.room.dao.*
import io.xxlabs.messenger.data.room.migration.*
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.data.room.model.converters.DateConverter
import io.xxlabs.messenger.data.room.model.converters.DateTimeConverter
import io.xxlabs.messenger.data.room.model.converters.SentStatusConverter
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@TypeConverters(DateConverter::class, DateTimeConverter::class, SentStatusConverter::class)
@Database(
    entities = [ContactData::class, PrivateMessageData::class, GroupMember::class,
        GroupData::class, GroupMessageData::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactsDao(): ContactsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun groupsDao(): GroupsDao
    abstract fun groupMembersDao(): GroupMembersDao
    abstract fun groupMessagesDao(): GroupMessagesDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private const val DB_NAME = "elixxirDB"
        private val scope = CoroutineScope(
            CoroutineName("RoomBuilder") +
                    Job() +
                    Dispatchers.IO
        )

        fun getInstance(app: Application): AppDatabase {
            return instance ?: synchronized(this) {
                runBlocking(scope.coroutineContext) {
                    when (getDatabaseState(app, DB_NAME)) {
                        State.DOES_NOT_EXIST -> encryptedDb(app)
                        State.UNENCRYPTED -> encryptDb(app)
                        State.ENCRYPTED -> encryptedDb(app)
                    }
                }
            }
        }

        /**
         * Encrypt the current database.
         */
        private suspend fun encryptDb(app: Application): AppDatabase {
            runCatching { encryptDatabase(app, DB_NAME) }
                .onFailure {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            appContext(),
                            "Existing database not found. Creating an encrypted database.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            return encryptedDb(app)
        }

        /**
         * Return the encrypted AppDatabase with the SQLCipher open helper.
         */
        private suspend fun encryptedDb(app: Application): AppDatabase {
            val dbKey = getOrCreateKey(app)
            val factory = SupportFactory(SQLiteDatabase.getBytes(dbKey.toCharArray()))

            val builder = Room.databaseBuilder(
                app,
                AppDatabase::class.java,
                DB_NAME
            )
                .addMigrations(*MigrationsFactory.create())
                .openHelperFactory(factory)
            val encryptedDb = builder.build()

            instance = encryptedDb
            return encryptedDb
        }

        /**
         * Check if an existing key is in SharedPreferences, or create a new one
         * and store it there.
         */
        private suspend fun getOrCreateKey(app: Application): String {
            val prefs = (app as XxMessengerApplication).preferencesRepository.preferences
                    as EncryptedSharedPreferences
            return prefs.getString(DB_KEY_NAME, null)
                ?: createKey(prefs)
        }
    }
}