package io.xxlabs.messenger.backup.data.backup

interface BackupPreferencesRepository {
    var name: String
    var isBackupEnabled: Boolean
    var isGoogleDriveEnabled: Boolean
    var isDropboxEnabled: Boolean
    var isSftpEnabled: Boolean
    var isCrustEnabled: Boolean
    var backupPassword: String?
    var autoBackup: Boolean
    var wiFiOnlyBackup: Boolean
    var backupLocation: String?
    var dbxCredential: String?
    var sftpCredential: String?
    var isUserProfileBackedUp: Boolean
}