package io.xxlabs.messenger.backup.data.restore

/**
 * Necessary data to restore an account to a new session on a device.
 */
data class RestoreEnvironment(
    val ndf: String,
    val appDirectory: String,
    val sessionPassword: ByteArray,
    val backupPassword: ByteArray,
)