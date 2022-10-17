package io.elixxir.xxmessengerclient

import io.elixxir.xxclient.backup.Backup
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.commands.*

interface Messenger {
    val cMix: () -> CMix?
    val e2e: () -> E2e?
    val ud: () -> UserDiscovery?
    val backup: () -> Backup?
    val isCreated: MessengerIsCreated
    val create: MessengerCreate
    val restoreBackup: MessengerRestoreBackup
    val isLoaded: MessengerIsLoaded
    val load: MessengerLoad
    val registerAuthCallbacks: MessengerRegisterAuthCallbacks
    val registerMessageListener: MessengerRegisterMessageListener
    val start: MessengerStart
    val stop: MessengerStop
    val isConnected: MessengerIsConnected
    val connect: MessengerConnect
    val isListeningForMessages: MessengerIsListeningForMessages
    val listenForMessages: MessengerListenForMessages
    val isRegistered: MessengerIsRegistered
    val register: MessengerRegister
    val isLoggedIn: MessengerIsLoggedIn
    val logIn: MessengerLogIn
    val myContact: MessengerMyContact
    val waitForNetwork: MessengerWaitForNetwork
    val waitForNodes: MessengerWaitForNodes
    val destroy: MessengerDestroy
    val searchContacts: MessengerSearchContacts
    val lookupContact: MessengerLookupContact
    val lookupContacts: MessengerLookupContacts
    val registerForNotifications: MessengerRegisterForNotifications
    val verifyContact: MessengerVerifyContact
    val sendMessage: MessengerSendMessage
    val registerBackupCallback: MessengerRegisterBackupCallback
    val isBackupRunning: MessengerIsBackupRunning
    val startBackup: MessengerStartBackup
    val resumeBackup: MessengerResumeBackup
    val backupParams: MessengerBackupParams
    val stopBackup: MessengerStopBackup
}