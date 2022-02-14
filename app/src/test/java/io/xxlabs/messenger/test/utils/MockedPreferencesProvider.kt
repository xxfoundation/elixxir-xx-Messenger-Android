package io.xxlabs.messenger.test.utils

import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.repository.PreferencesRepository
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock

class MockedPreferencesProvider(preferencesRepo: PreferencesRepository) {
    init {
        //User

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.userData)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).userData = anyString()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.userPicture)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).userPicture = anyString()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.userSecret)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).userSecret = anyString()

        //General
        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.contactsCount)
                .thenReturn(answer.arguments[0] as Int)
        }.`when`(preferencesRepo).contactsCount = anyInt()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.areDebugLogsOn)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).areDebugLogsOn = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.lastAppVersion)
                .thenReturn(answer.arguments[0] as Int)
        }.`when`(preferencesRepo).lastAppVersion = anyInt()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.currentNotificationsTokenId)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).currentNotificationsTokenId = anyString()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.notificationsTokenId)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).notificationsTokenId = anyString()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.name)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).name = anyString()

        //Settings
        preferencesRepo.isEnterToSendEnabled = false
        preferencesRepo.isHideAppEnabled = false
        preferencesRepo.isIncognitoKeyboardEnabled = false

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.areNotificationsOn)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).areNotificationsOn = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.showBiometricDialog)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).showBiometricDialog = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.userBiometricKey)
                .thenReturn(answer.arguments[0] as String)
        }.`when`(preferencesRepo).userBiometricKey = anyString()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.isCrashReportEnabled)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).isCrashReportEnabled = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.isFingerprintEnabled)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).isFingerprintEnabled = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.isEnterToSendEnabled)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).isEnterToSendEnabled = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.isHideAppEnabled)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).isHideAppEnabled = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.isIncognitoKeyboardEnabled)
                .thenReturn(answer.arguments[0] as Boolean)
        }.`when`(preferencesRepo).isIncognitoKeyboardEnabled = anyBoolean()

        doAnswer { answer: InvocationOnMock ->
            `when`(preferencesRepo.contactRoundRequests)
                .thenReturn(convertArg(answer))
        }.`when`(preferencesRepo).contactRoundRequests = anyList<String>().toMutableSet()

        //User
        preferencesRepo.userData = ""
        preferencesRepo.userPicture = ""
        preferencesRepo.userSecret = ""

        //General
        preferencesRepo.contactsCount = 0
        preferencesRepo.areDebugLogsOn = false
        preferencesRepo.lastAppVersion = BuildConfig.VERSION_CODE
        preferencesRepo.currentNotificationsTokenId = ""
        preferencesRepo.notificationsTokenId = ""
        preferencesRepo.userId = ""
        preferencesRepo.name = ""

        //Settings
        preferencesRepo.areNotificationsOn = false
        preferencesRepo.showBiometricDialog = false
        preferencesRepo.userBiometricKey = ""
        preferencesRepo.isCrashReportEnabled = false
        preferencesRepo.isFingerprintEnabled = false
        preferencesRepo.isEnterToSendEnabled = false
        preferencesRepo.isHideAppEnabled = false
        preferencesRepo.isIncognitoKeyboardEnabled = false

        //Other
        preferencesRepo.contactRoundRequests = mutableSetOf()
    }

    private fun convertArg(answer: InvocationOnMock): MutableSet<String>{
        return (answer.arguments[0] as MutableSet<*>).filterIsInstance<String>().map {
            it
        }.toMutableSet()
    }
}