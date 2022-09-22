package io.xxlabs.messenger.data.room.migration

import android.app.Application
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import io.xxlabs.messenger.application.XxMessengerApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import java.io.File
import java.io.IOException
import java.security.SecureRandom

private const val DB_KEY_SIZE = 32
const val DB_KEY_NAME = "dbKey"

/**
 * The detected state of the database, based on whether we can open it
 * without a passphrase.
 */
enum class State {
    DOES_NOT_EXIST, UNENCRYPTED, ENCRYPTED
}

fun getDatabaseState(
    app: Application,
    dbName: String,
): State {
    val context = app.applicationContext
    SQLiteDatabase.loadLibs(context)
    return getDatabaseState(context.getDatabasePath(dbName))
}

private fun getDatabaseState(dbPath: File) : State {
    if (!dbPath.exists()) return State.DOES_NOT_EXIST

    var db: SQLiteDatabase? = null
    try {
        // Attempt to open the database with no password.
        db = SQLiteDatabase.openDatabase(
            dbPath.absolutePath,
            "",
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    } catch (e: SQLiteException) {
        // An exception was thrown, the database is encrypted.
        return State.ENCRYPTED
    } finally {
        db?.close()
    }

    return State.UNENCRYPTED
}

/**
 * Creates an encrypted copy of the existing database then replaces the
 * plaintext DB with the new, encrypted one.
 */
@Suppress("BlockingMethodInNonBlockingContext")
@Throws(IOException::class)
suspend fun encryptDatabase(
    app: Application,
    dbName: String,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    val prefs = (app as XxMessengerApplication).preferencesRepository.preferences
            as EncryptedSharedPreferences
    // Path to current plaintext DB.
    val plainTextDb = app.getDatabasePath(dbName)
    // Path to where the encrypted database is temporarily stored.
    val newFile = File.createTempFile(
        "sqlcipherutils",
        "tmp",
        app.cacheDir
    )

    // Open the unencrypted database.
    var db = SQLiteDatabase.openDatabase(
        plainTextDb.absolutePath,
        "",
        null,
        SQLiteDatabase.OPEN_READWRITE
    )
    val version = db.version
    db.close()

    // Create a key to encrypt the database and persist it to EncryptedSharedPreferences.
    val dbKey = createKey(prefs, dispatcher)

    // Create a temporary encrypted database.
    db = SQLiteDatabase.openDatabase(
        newFile.absolutePath,
        dbKey,
        null,
        SQLiteDatabase.OPEN_READWRITE
    )

    // Attach the plaintext database to the encrypted one.
    db.rawExecSQL("ATTACH DATABASE '${plainTextDb.absolutePath}' AS plaintext KEY ''")
    // Use sqlcipher_export to copy & encrypt the plaintext database.
    db.rawExecSQL("SELECT sqlcipher_export('main', 'plaintext')")
    // Detach the plaintext database and close it.
    db.rawExecSQL("DETACH DATABASE plaintext")
    db.version = version
    db.close()

    // Delete the plaintext file and replace it with the encrypted one.
    plainTextDb.delete()
    newFile.renameTo(plainTextDb)
}

/**
 * Create a key to encrypt the database and store it to SharedPreferences.
 */
suspend fun createKey(
    prefs: EncryptedSharedPreferences,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): String {
    return KeyGenerator.create(DB_KEY_SIZE, dispatcher).also {
        prefs.edit().putString(DB_KEY_NAME, it).apply()
    }
}

private object KeyGenerator {
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    /**
     * Generates a new database key as a [CharArray] of [size] bytes long.
     */
    suspend fun create(
        size: Int,
        dispatcher: CoroutineDispatcher
    ): String = withContext(dispatcher) { generateRandomKey(size).toHex() }

    /**
     * Generates a random [ByteArray] of [size] bytes long.
     */
    private fun generateRandomKey(size: Int): ByteArray =
        ByteArray(size).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26 (Oreo) and above
                SecureRandom.getInstanceStrong().nextBytes(this)
            } else {
                SecureRandom().nextBytes(this)
            }
        }

    /**
     * Extension function that converts a [ByteArray] to a hex encoded String
     */
    private fun ByteArray.toHex(): String {
        val result = StringBuilder()
        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }
        return result.toString()
    }
}