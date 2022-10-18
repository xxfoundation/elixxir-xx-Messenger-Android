package io.elixxir.xxmessengerclient

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.bindings.BindingsAdapter
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.environment.*
import io.elixxir.xxmessengerclient.utils.*


abstract class MessengerEnvironment {
    abstract val passwordStorage: PasswordStorage
    abstract val storageDir: String
    abstract val udCert: ByteArray
    abstract val udContact: ByteArray

    open val udAddress: String = "46.101.98.49:18001"
    open val sleep: (ms: Long) -> Unit = { Thread.sleep(it) }

    private val bindings: Bindings = BindingsAdapter()

    var backup: Backup? = null
    var cMix: CMix? = null
    var e2e: E2e? = null
    var ud: UserDiscovery? = null

    open val authCallbacks: AuthCallbacksRegistry = AuthCallbacksRegistry()
    open val backupCallbacks: BackupCallbacksRegistry = BackupCallbacksRegistry()
    open val downloadNDF: DownloadAndVerifySignedNdf = DownloadAndVerifySignedNdf(bindings)
    open val fileManager: MessengerFileManager get() = AndroidFileManager(storageDir)
    open val generateSecret: GenerateSecret = GenerateSecret(bindings)
    open val getCMixParams: GetCMixParams = GetCMixParams(bindings)
    open val getE2EParams: GetE2EParams = GetE2EParams(bindings)
    open val getSingleUseParams: GetSingleUseParams = GetSingleUseParams(bindings)
    open val initializeBackup: InitializeBackup = InitializeBackup(bindings)
    open var isListeningForMessages: Boolean = false
    open val isRegisteredWithUD: IsRegisteredWithUD = IsRegisteredWithUD(bindings)
    open val loadCMix: LoadCMix = LoadCMix(bindings)
    open val login: Login = Login(bindings)
    open val lookupUD: LookupUD = LookupUD(bindings)
    open val messageListeners: ListenersRegistry = ListenersRegistry()
    open val multiLookupUD: MultiLookupUD = MultiLookupUD(bindings)
    open val ndfEnvironment: NDFEnvironment = NDFEnvironment()
    open val newCMix: NewCMix = NewCMix(bindings)
    open val newCMixFromBackup: NewCMixFromBackup = NewCMixFromBackup(bindings)
    open val newOrLoadUd: NewOrLoadUd = NewOrLoadUd(bindings)
    open val newUdManagerFromBackup: NewUdManagerFromBackup = NewUdManagerFromBackup(bindings)
    open val registerForNotifications: RegisterForNotifications = RegisterForNotifications()
    open val resumeBackup: ResumeBackup = ResumeBackup()
    open val searchUD: SearchUD = SearchUD(bindings)
}