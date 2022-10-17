package io.elixxir.xxmessengerclient

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.password.PasswordStorage
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.environment.*
import io.elixxir.xxmessengerclient.utils.AuthCallbacksRegistry
import io.elixxir.xxmessengerclient.utils.BackupCallbacksRegistry
import io.elixxir.xxmessengerclient.utils.ListenersRegistry
import io.elixxir.xxmessengerclient.utils.MessengerFileManager

interface MessengerEnvironment {
    val udIpAddress: String get() = "46.101.98.49:18001" // AltUD

    val bindings: Bindings
    var authCallbacks: AuthCallbacksRegistry
    var backup: Backup?
    var backupCallbacks: BackupCallbacksRegistry
    var cMix: CMix?
    var downloadNDF: DownloadAndVerifySignedNdf
    var e2e: E2e?
    var fileManager: MessengerFileManager
    var generateSecret: GenerateSecret
    var getCMixParams: GetCMixParams
    var getE2EParams: GetE2EParams
    var getSingleUseParams: GetSingleUseParams
    var initializeBackup: InitializeBackup
    var isListeningForMessages: () -> Boolean
    var isRegisteredWithUD: IsRegisteredWithUD
    var loadCMix: LoadCMix
    var login: Login
    var lookupUD: LookupUD
    var messageListeners: ListenersRegistry
    var multiLookupUD: MultiLookupUD
    var ndfEnvironment: NDFEnvironment
    var newCMix: NewCMix
    var newCMixFromBackup: NewCMixFromBackup
    var newOrLoadUd: NewOrLoadUd
    var newUdManagerFromBackup: NewUdManagerFromBackup
    var passwordStorage: PasswordStorage
    var registerForNotifications: RegisterForNotifications
    var resumeBackup: ResumeBackup
    var searchUD: SearchUD
    var sleep: (Long) -> Void
    var storageDir: String
    var ud: UserDiscovery?
    var udAddress: String?
    var udCert: ByteArray?
    var udContact: ByteArray?
}