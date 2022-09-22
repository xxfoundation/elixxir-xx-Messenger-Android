package io.xxlabs.messenger.repository.base

import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.notifications.NotificationPreferences

abstract class BasePreferences : BackupPreferencesRepository, NotificationPreferences {
    abstract fun addContactRequest(contactId: ByteArray, contactUsername: String, roundId: Long, isSent: Boolean)
    abstract fun getUserId(): ByteArray
    abstract fun setUserId(userId: ByteArray)

    //User
    abstract var isFirstLaunch: Boolean
    abstract var isFirstTimeNotifications: Boolean
    abstract var isFirstTimeCoverMessages: Boolean
    abstract var preImages: String
    abstract var userData: String
    abstract var userPicture: String
    abstract var userSecret: String
    abstract var shareEmailWhenRequesting: Boolean
    abstract var sharePhoneWhenRequesting: Boolean
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