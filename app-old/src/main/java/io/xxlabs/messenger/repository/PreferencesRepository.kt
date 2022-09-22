package io.xxlabs.messenger.repository

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.xxlabs.messenger.data.data.ContactRoundRequest
import io.xxlabs.messenger.repository.base.BasePreferences
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import javax.inject.Inject

class PreferencesRepository @Inject constructor(
    context: Context
) : BasePreferences() {
    private val masterKeyAlias = "xx_preferences_key"
    private val preferencesAlias = "xx_preferences"
    private val masterKeySpec = KeyGenParameterSpec.Builder(
        masterKeyAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setDigests(KeyProperties.DIGEST_SHA256)
        .setRandomizedEncryptionRequired(true)
        .setKeySize(256)
        .build()

    private val masterKey =
        MasterKey.Builder(context, masterKeyAlias).setKeyGenParameterSpec(masterKeySpec).build()

    var preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        preferencesAlias,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    @Deprecated(
        "Use RequestDataSource",
        replaceWith = ReplaceWith(
            "ContactRequestsRepository.save()",
            "io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository"
        )
    )
    override fun addContactRequest(
        contactId: ByteArray,
        contactUsername: String,
        roundId: Long,
        isSent: Boolean
    ) {
        val contactRoundRequest = ContactRoundRequest(
            contactId,
            contactUsername,
            roundId,
            Utils.getCurrentTimeStamp(),
            isSent
        )

        val currRequestsList = ContactRoundRequest.toRoundRequestsSet(contactRoundRequests)
        val exists = currRequestsList.firstOrNull { roundRequest ->
            roundRequest == contactRoundRequest
        }

        if (exists != null) {
            Timber.v("Failed adding round request for ${contactRoundRequest.contactId.toBase64String()}: Already exists")
        } else {
            currRequestsList.add(contactRoundRequest)
            Timber.v("Added ${contactRoundRequest.contactId.toBase64String()} request = $contactRoundRequest")
        }

        contactRoundRequests = ContactRoundRequest.toJsonSet(currRequestsList)
    }

    /**
     * Remove all requests with the matching [contactId].
     * Returns the number of removed requests.
     */
    @Deprecated(
        "Use RequestDataSource",
        replaceWith = ReplaceWith(
            "ContactRequestsRepository.save()",
            "io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository"
        )
    )
    fun removeContactRequests(contactId: ByteArray): Int {
        val currRequestsList = ContactRoundRequest.toRoundRequestsSet(contactRoundRequests)
        val matchingRequests = currRequestsList.filter { roundRequest ->
            roundRequest.contactId.contentEquals(contactId)
        }

        for (request in matchingRequests) {
            currRequestsList.remove(request)
        }
        return matchingRequests.size
    }

    override fun getUserId(): ByteArray {
        return this.userId.fromBase64toByteArray()
    }

    override fun setUserId(userId: ByteArray) {
        this.userId = userId.toBase64String()
    }

    fun nextRegistrationPhase() {
        registrationStep += 1
    }

    override var isFirstLaunch: Boolean
        get() = preferences.getBoolean("is_first_launch", true)
        set(value) {
            preferences.edit().putBoolean("is_first_launch", value).apply()
        }

    override var isFirstTimeNotifications: Boolean
        get() = preferences.getBoolean("is_first_time_notifications", true)
        set(value) {
            preferences.edit().putBoolean("is_first_time_notifications", value).apply()
        }

    override var isFirstTimeCoverMessages: Boolean
        get() = preferences.getBoolean("is_first_time_cover_messages", true)
        set(value) {
            preferences.edit().putBoolean("is_first_time_cover_messages", value).apply()
        }

    override var preImages: String
        get() = preferences.getString("preimages", "")!!
        set(value) {
            preferences.edit().putString("preimages", value).apply()
        }

    override var userData: String = preferences.getString("user_data", "")!!
        get() = preferences.getString("user_data", "")!!
        set(value) {
            if (field != value) {
                isUserProfileBackedUp = false
                field = value
            }
            preferences.edit().putString("user_data", value).apply()
        }

    override var userPicture: String
        get() = preferences.getString("user_picture", "")!!
        set(value) {
            preferences.edit().putString("user_picture", value).apply()
        }

    override var userSecret: String
        get() = preferences.getString("secret_k", "")!!
        set(value) {
            preferences.edit().putString("secret_k", value).apply()
        }

    override var registrationStep: Int
        get() = preferences.getInt("registration_step", 0)
        set(value) {
            preferences.edit().putInt("registration_step", value).apply()
        }

    override var shareEmailWhenRequesting: Boolean
        get() = preferences.getBoolean("should_share_email_qr", false)
        set(value) {
            preferences.edit().putBoolean("should_share_email_qr", value).apply()
        }

    override var sharePhoneWhenRequesting: Boolean
        get() = preferences.getBoolean("should_share_phone_qr", false)
        set(value) {
            preferences.edit().putBoolean("should_share_phone_qr", value).apply()
        }

    override var contactsCount: Int
        get() = preferences.getInt("contacts_count", 0)
        set(value) {
            preferences.edit().putInt("contacts_count", value).apply()
        }

    override var lastAppVersion: Int
        get() = preferences.getInt("app_build_version", -1)
        set(value) {
            preferences.edit().putInt("app_build_version", value).apply()
        }

    override var currentNotificationsTokenId: String =
        preferences.getString("curr_notifications_id", "")!!
        get() = preferences.getString("curr_notifications_id", "")!!
        set(value) {
            field = value
            preferences.edit().putString("curr_notifications_id", value).apply()
        }

    override var notificationsTokenId: String = preferences.getString("notifications_id", "")!!
        get() = preferences.getString("notifications_id", "")!!
        set(value) {
            field = value
            preferences.edit().putString("notifications_id", value).apply()
        }

    override var userId: String = preferences.getString("user_id", "")!!
        get() = preferences.getString("user_id", "")!!
        set(value) {
            field = value
            preferences.edit().putString("user_id", value).apply()
        }

    override var contactRoundRequests: MutableSet<String> =
        preferences.getStringSet("timeoutChats", mutableSetOf())!!
        get() = preferences.getStringSet("timeoutChats", mutableSetOf())!!
        set(stringSet) {
            field = stringSet
            preferences.edit().putStringSet("timeoutChats", stringSet).apply()
        }

    override var isBackupEnabled: Boolean = preferences.getBoolean("backup_enabled", false)
        get() = preferences.getBoolean("backup_enabled", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("backup_enabled", value).apply()
        }

    override var isGoogleDriveEnabled: Boolean = preferences.getBoolean("drive_enabled", false)
        get() = preferences.getBoolean("drive_enabled", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("drive_enabled", value).apply()
        }

    override var isDropboxEnabled: Boolean = preferences.getBoolean("dropbox_enabled", false)
        get() = preferences.getBoolean("dropbox_enabled", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("dropbox_enabled", value).apply()
        }
    override var isSftpEnabled: Boolean = preferences.getBoolean("sftp_enabled", false)
        get() = preferences.getBoolean("sftp_enabled", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("sftp_enabled", value).apply()
        }

    override var backupPassword: String? = preferences.getString("backup_pw", null)
        get() = preferences.getString("backup_pw", null)
        set(value) {
            field = value
            preferences.edit().putString("backup_pw", value).apply()
        }

    override var autoBackup: Boolean = preferences.getBoolean("auto_backup", false)
        get() = preferences.getBoolean("auto_backup", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("auto_backup", value).apply()

        }
    override var wiFiOnlyBackup: Boolean = preferences.getBoolean("wifi_only_backup", false)
        get() = preferences.getBoolean("wifi_only_backup", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("wifi_only_backup", value).apply()
        }

    override var backupLocation: String? = preferences.getString("backup_location", null)
        get() = preferences.getString("backup_location", null)
        set(value) {
            field = value
            preferences.edit().putString("backup_location", value).apply()
        }

    override var dbxCredential: String? = preferences.getString("dbx_credential", null)
        get() = preferences.getString("dbx_credential", null)
        set(value) {
            field = value
            preferences.edit().putString("dbx_credential", value).apply()
        }
    override var sftpCredential: String? = preferences.getString("sftp_credential", null)
        get() = preferences.getString("sftp_credential", null)
        set(value) {
            field = value
            preferences.edit().putString("sftp_credential", value).apply()
        }

    override var isUserProfileBackedUp: Boolean = preferences.getBoolean("user_backed_up", false)
        get() = preferences.getBoolean("user_backed_up", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("user_backed_up", value).apply()
        }

    override var showContactNames: Boolean = preferences.getBoolean("notifications_show_contact_names", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("notifications_show_contact_names", value).apply()
        }

    override var showGroupNames: Boolean = preferences.getBoolean("notifications_show_group_names", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("notifications_show_group_names", value).apply()
        }

    override var areDebugLogsOn: Boolean = preferences.getBoolean("show_debug_logs", true)
        get() = preferences.getBoolean("show_debug_logs", true)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("show_debug_logs", isChecked).apply()
        }

    override var isCrashReportEnabled: Boolean = preferences.getBoolean("enable_crash_report", true)
        get() = preferences.getBoolean("enable_crash_report", true)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("enable_crash_report", isChecked).apply()
        }

    override var name = preferences.getString("username", "")!!
        get() = preferences.getString("username", "")!!
        set(isChecked) {
            field = isChecked
            preferences.edit().putString("username", isChecked).apply()
        }

    override var areNotificationsOn = preferences.getBoolean("show_notifications", false)
        get() = preferences.getBoolean("show_notifications", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("show_notifications", isChecked).apply()
        }

    override var areInAppNotificationsOn = preferences.getBoolean("show_in_app_notifications", true)
        get() = preferences.getBoolean("show_in_app_notifications", true)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("show_in_app_notifications", isChecked).apply()
        }

    override var isCoverTrafficOn = preferences.getBoolean("enable_cover_traffic", false)
        get() = preferences.getBoolean("enable_cover_traffic", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("enable_cover_traffic", isChecked).apply()
        }

    override var showBiometricDialog = preferences.getBoolean("show_biometric_dialog", false)
        get() = preferences.getBoolean("show_biometric_dialog", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("show_biometric_dialog", isChecked).apply()
        }

    override var isFingerprintEnabled = preferences.getBoolean("is_fingerprint_enabled", false)
        get() = preferences.getBoolean("is_fingerprint_enabled", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("is_fingerprint_enabled", isChecked).apply()
        }

    override var isEnterToSendEnabled = preferences.getBoolean("use_enter_to_send", false)
        get() = preferences.getBoolean("use_enter_to_send", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("use_enter_to_send", isChecked).apply()
        }

    override var isHideAppEnabled = preferences.getBoolean("should_hide_app", false)
        get() = preferences.getBoolean("should_hide_app", false)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("should_hide_app", isChecked).apply()
        }

    override var isIncognitoKeyboardEnabled = preferences.getBoolean("is_incognito_enabled", true)
        get() = preferences.getBoolean("is_incognito_enabled", true)
        set(isChecked) {
            field = isChecked
            preferences.edit().putBoolean("is_incognito_enabled", isChecked).apply()
        }

    override var userBiometricKey = preferences.getString("biometric_key", "")!!
        get() = preferences.getString("biometric_key", "")!!
        set(isChecked) {
            field = isChecked
            preferences.edit().putString("biometric_key", isChecked).apply()
        }

    companion object {
        @Volatile
        private var instance: PreferencesRepository? = null

        fun getInstance(
            context: Context
        ): PreferencesRepository {
            return instance ?: synchronized(this) {
                val client = PreferencesRepository(
                    context
                )
                instance = client
                return client
            }
        }
    }
}