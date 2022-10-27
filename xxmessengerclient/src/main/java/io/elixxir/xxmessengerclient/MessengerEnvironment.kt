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
    abstract val ndfCert: String

    open val udCert: ByteArray get() = e2e!!.getUdCertFromNdf()
    open val udContact: ByteArray get() = e2e!!.getUdContactFromNdf().data
    open val udAddress: String get() = e2e!!.getUdAddressFromNdf()
    open val sleep: (ms: Long) -> Unit = { Thread.sleep(it) }

    val bindings: Bindings = BindingsAdapter()

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
    open val groupListeners: GroupListenersRegistry = GroupListenersRegistry()
    open val multiLookupUD: MultiLookupUD = MultiLookupUD(bindings)
    open val ndfEnvironment: NDFEnvironment = NDFEnvironment(cert = ndfCert)
    open val newCMix: NewCMix = NewCMix(bindings)
    open val newCMixFromBackup: NewCMixFromBackup = NewCMixFromBackup(bindings)
    open val newOrLoadUd: NewOrLoadUd = NewOrLoadUd(bindings)
    open val newUdManagerFromBackup: NewUdManagerFromBackup = NewUdManagerFromBackup(bindings)
    open val registerForNotifications: RegisterForNotifications = RegisterForNotifications(bindings)
    open val unregisterForNotifications: UnregisterForNotifications = UnregisterForNotifications(bindings)
    open val resumeBackup: ResumeBackup = ResumeBackup()
    open val searchUD: SearchUD = SearchUD(bindings)
}