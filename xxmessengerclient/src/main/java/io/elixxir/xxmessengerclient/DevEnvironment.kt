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

class DevEnvironment(
    app: Application,
    override var passwordStorage: PasswordStorage,
    override var storageDir: String,
    override val udCert: ByteArray,
    override val udContact: ByteArray
): MessengerEnvironment {
    override val bindings: Bindings = BindingsAdapter()

    override var authCallbacks: AuthCallbacksRegistry = AuthCallbacksRegistry()
    override var backup: Backup? = null
    override var backupCallbacks: BackupCallbacksRegistry = BackupCallbacksRegistry()
    override var cMix: CMix? = null
    override var downloadNDF: DownloadAndVerifySignedNdf = DownloadAndVerifySignedNdf(bindings)
    override var e2e: E2e? = null
    override var fileManager: MessengerFileManager = MessengerFileManager(app)
    override var generateSecret: GenerateSecret = GenerateSecret(bindings)
    override var getCMixParams: GetCMixParams = GetCMixParams(bindings)
    override var getE2EParams: GetE2EParams = GetE2EParams(bindings)
    override var getSingleUseParams: GetSingleUseParams = GetSingleUseParams(bindings)
    override var initializeBackup: InitializeBackup = InitializeBackup(bindings)
    override var isListeningForMessages: Boolean = false
    override var isRegisteredWithUD: IsRegisteredWithUD = IsRegisteredWithUD(bindings)
    override var loadCMix: LoadCMix = LoadCMix(bindings)
    override var login: Login = Login(bindings)
    override var lookupUD: LookupUD = LookupUD(bindings)
    override var messageListeners: ListenersRegistry = ListenersRegistry()
    override var multiLookupUD: MultiLookupUD = MultiLookupUD(bindings)
    override var ndfEnvironment: NDFEnvironment = NDFEnvironment()
    override var newCMix: NewCMix = NewCMix(bindings)
    override var newCMixFromBackup: NewCMixFromBackup = NewCMixFromBackup(bindings)
    override var newOrLoadUd: NewOrLoadUd = NewOrLoadUd(bindings)
    override var newUdManagerFromBackup: NewUdManagerFromBackup = NewUdManagerFromBackup(bindings)
    override var registerForNotifications: RegisterForNotifications = RegisterForNotifications()
    override var resumeBackup: ResumeBackup = ResumeBackup()
    override var searchUD: SearchUD = SearchUD(bindings)
    override var sleep: (ms: Long) -> Unit = { Thread.sleep(it) }
    override var ud: UserDiscovery? = null

    override val udAddress: String = "46.101.98.49:18001"
}