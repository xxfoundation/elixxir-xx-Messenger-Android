package io.elixxir.xxmessengerclient

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.commands.*

class Messenger(val env: MessengerEnvironment) {
    val cMix: CMix? get() = env.cMix
    val e2e: E2e? get() = env.e2e
    val ud: UserDiscovery? get() = env.ud
    val backup: Backup? get() = env.backup

    val isCreated: MessengerIsCreated = MessengerIsCreated(env)
    val create: MessengerCreate = MessengerCreate(env)
    val restoreBackup: MessengerRestoreBackup = MessengerRestoreBackup()
    val isLoaded: MessengerIsLoaded = MessengerIsLoaded(env)
    val load: MessengerLoad = MessengerLoad(env)
    val registerAuthCallbacks: MessengerRegisterAuthCallbacks = MessengerRegisterAuthCallbacks(env)
    val registerMessageListener: MessengerRegisterMessageListener = MessengerRegisterMessageListener(env)
    val start: MessengerStart = MessengerStart(env)
    val stop: MessengerStop = MessengerStop(env)
    val isConnected: MessengerIsConnected = MessengerIsConnected(env)
    val connect: MessengerConnect = MessengerConnect(env)
    val isListeningForMessages: MessengerIsListeningForMessages = MessengerIsListeningForMessages(env)
    val listenForMessages: MessengerListenForMessages = MessengerListenForMessages(env)
    val isRegistered: MessengerIsRegistered = MessengerIsRegistered(env)
    val register: MessengerRegister = MessengerRegister(env)
    val isLoggedIn: MessengerIsLoggedIn = MessengerIsLoggedIn(env)
    val logIn: MessengerLogIn = MessengerLogIn(env)
    val myContact: MessengerMyContact = MessengerMyContact(env)
    val waitForNetwork: MessengerWaitForNetwork = MessengerWaitForNetwork(env)
    val waitForNodes: MessengerWaitForNodes = MessengerWaitForNodes(env)
    val destroy: MessengerDestroy = MessengerDestroy(env)
    val searchContacts: MessengerSearchContacts = MessengerSearchContacts(env)
    val lookupContact: MessengerLookupContact = MessengerLookupContact(env)
    val lookupContacts: MessengerLookupContacts = MessengerLookupContacts(env)
    val registerForNotifications: MessengerRegisterForNotifications = TODO()
    val verifyContact: MessengerVerifyContact = MessengerVerifyContact(env)
    val sendMessage: MessengerSendMessage = MessengerSendMessage(env)
    val registerBackupCallback: MessengerRegisterBackupCallback = TODO()
    val isBackupRunning: MessengerIsBackupRunning = TODO()
    val startBackup: MessengerStartBackup = TODO()
    val resumeBackup: MessengerResumeBackup = TODO()
    val backupParams: MessengerBackupParams = TODO()
    val stopBackup: MessengerStopBackup = TODO()
}