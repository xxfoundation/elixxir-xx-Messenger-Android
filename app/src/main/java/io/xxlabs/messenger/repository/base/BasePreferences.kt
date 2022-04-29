package io.xxlabs.messenger.repository.base

import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.data.data.ContactRoundRequest
import io.xxlabs.messenger.ui.main.requests.RequestsFilter

abstract class BasePreferences : BackupPreferencesRepository {
    abstract fun addContactRequest(contactRoundRequest: ContactRoundRequest)
    abstract fun addContactRequest(contactId: ByteArray, contactUsername: String, roundId: Long, isSent: Boolean)
    abstract fun removeContactRequest(contactRoundRequest: ContactRoundRequest)
    abstract fun updateContactRequest(contactRoundRequest: ContactRoundRequest)
    abstract fun getContactRequest(contactId: ByteArray, roundId: Long): ContactRoundRequest?
    abstract fun getContactRequest(contactId: ByteArray): ContactRoundRequest?
    abstract fun getContactRequest(filter: RequestsFilter): List<ContactRoundRequest>
    abstract fun getUserId(): ByteArray
    abstract fun setUserId(userId: ByteArray)
    abstract fun clearAll()
    abstract fun removeContactRequests(contactId: ByteArray): Int

    //User
    abstract var isFirstLaunch: Boolean
    abstract var isFirstTimeNotifications: Boolean
    abstract var isFirstTimeCoverMessages: Boolean
    abstract var preImages: String
    abstract var userData: String
    abstract var userPicture: String
    abstract var userSecret: String
    abstract var shouldShareEmailQr: Boolean
    abstract var shouldSharePhoneQr: Boolean
    abstract var registrationStep: Int

    //General
    abstract var contactsCount: Int
    abstract var areDebugLogsOn: Boolean
    abstract var lastAppVersion: Int
    abstract var currentNotificationsTokenId: String
    abstract var notificationsTokenId: String
    abstract var userId: String
    abstract var name: String

    //Settings
    abstract var areNotificationsOn: Boolean
    abstract var areInAppNotificationsOn: Boolean
    abstract var isCoverTrafficOn: Boolean
    abstract var showBiometricDialog: Boolean
    abstract var userBiometricKey: String
    abstract var isCrashReportEnabled: Boolean
    abstract var isFingerprintEnabled: Boolean
    abstract var isEnterToSendEnabled: Boolean
    abstract var isHideAppEnabled: Boolean
    abstract var isIncognitoKeyboardEnabled: Boolean

    //Other
    abstract var contactRoundRequests: MutableSet<String>
}