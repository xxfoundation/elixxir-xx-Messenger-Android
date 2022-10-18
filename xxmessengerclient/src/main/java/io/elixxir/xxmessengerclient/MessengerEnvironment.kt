package io.elixxir.xxmessengerclient

import android.app.Application
import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.bindings.BindingsAdapter
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.password.PasswordStorage
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.environment.*
import io.elixxir.xxmessengerclient.utils.AuthCallbacksRegistry
import io.elixxir.xxmessengerclient.utils.BackupCallbacksRegistry
import io.elixxir.xxmessengerclient.utils.ListenersRegistry
import io.elixxir.xxmessengerclient.utils.MessengerFileManager


abstract class MessengerEnvironment(
    app: Application,
) {
    abstract val passwordStorage: PasswordStorage
    abstract val storageDir: String
    abstract val udCert: ByteArray
    abstract val udContact: ByteArray
    open val udAddress: String = "46.101.98.49:18001"

    var backup: Backup? = null
    var cMix: CMix? = null
    var e2e: E2e? = null
    var ud: UserDiscovery? = null

    val bindings: Bindings = BindingsAdapter()
    val authCallbacks: AuthCallbacksRegistry = AuthCallbacksRegistry()
    val backupCallbacks: BackupCallbacksRegistry = BackupCallbacksRegistry()
    val downloadNDF: DownloadAndVerifySignedNdf = DownloadAndVerifySignedNdf(bindings)
    val fileManager: MessengerFileManager = MessengerFileManager(app)
    val generateSecret: GenerateSecret = GenerateSecret(bindings)
    val getCMixParams: GetCMixParams = GetCMixParams(bindings)
    val getE2EParams: GetE2EParams = GetE2EParams(bindings)
    val getSingleUseParams: GetSingleUseParams = GetSingleUseParams(bindings)
    val initializeBackup: InitializeBackup = InitializeBackup(bindings)
    val isListeningForMessages: Boolean = false
    val isRegisteredWithUD: IsRegisteredWithUD = IsRegisteredWithUD(bindings)
    val loadCMix: LoadCMix = LoadCMix(bindings)
    val login: Login = Login(bindings)
    val lookupUD: LookupUD = LookupUD(bindings)
    val messageListeners: ListenersRegistry = ListenersRegistry()
    val multiLookupUD: MultiLookupUD = MultiLookupUD(bindings)
    val ndfEnvironment: NDFEnvironment = NDFEnvironment()
    val newCMix: NewCMix = NewCMix(bindings)
    val newCMixFromBackup: NewCMixFromBackup = NewCMixFromBackup(bindings)
    val newOrLoadUd: NewOrLoadUd = NewOrLoadUd(bindings)
    val newUdManagerFromBackup: NewUdManagerFromBackup = NewUdManagerFromBackup(bindings)
    val registerForNotifications: RegisterForNotifications = RegisterForNotifications()
    val resumeBackup: ResumeBackup = ResumeBackup()
    val searchUD: SearchUD = SearchUD(bindings)
    val sleep: (ms: Long) -> Unit = { Thread.sleep(it) }
}