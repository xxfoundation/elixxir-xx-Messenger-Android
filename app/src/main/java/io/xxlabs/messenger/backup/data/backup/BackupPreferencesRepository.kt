package io.xxlabs.messenger.backup.data.backup

interface BackupPreferencesRepository {
    var isBackupEnabled: Boolean
    var isGoogleDriveEnabled: Boolean
    var isDropboxEnabled: Boolean
    var backupPassword: String?
    var autoBackup: Boolean
    var wiFiOnlyBackup: Boolean
    var backupLocation: String?
    var dbxCredential: String?
    var isUserProfileBackedUp: Boolean
}