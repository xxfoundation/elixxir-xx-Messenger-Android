package io.xxlabs.messenger.ui.main.settings
//
//import android.content.Context
//import com.google.common.truth.Truth.assertThat
//import io.reactivex.Single
//import io.xxlabs.messenger.InstantExecutorExtension
//import io.xxlabs.messenger.data.data.SimpleRequestState
//import io.xxlabs.messenger.repository.DaoRepository
//import io.xxlabs.messenger.repository.PreferencesRepository
//import io.xxlabs.messenger.repository.base.BaseRepository
//import io.xxlabs.messenger.support.misc.DebugLogger
//import io.xxlabs.messenger.test.utils.MockedPreferencesProvider
//import io.xxlabs.messenger.test.utils.TestSchedulersProvider
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.assertDoesNotThrow
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.Mockito
//import org.mockito.Mockito.`when`
//import org.mockito.kotlin.any
//
//@ExtendWith(InstantExecutorExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//internal class SettingsViewModelTest {
//    lateinit var settingsViewModel: SettingsViewModel
//    lateinit var baseRepo: BaseRepository
//    lateinit var mockedContext: Context
//    lateinit var preferencesRepo: PreferencesRepository
//    lateinit var daoRepo: DaoRepository
//
//    @BeforeEach
//    fun setup() {
//        baseRepo = Mockito.mock(BaseRepository::class.java)
//        daoRepo = Mockito.mock(DaoRepository::class.java)
//        preferencesRepo = Mockito.mock(PreferencesRepository::class.java)
//        mockedContext = Mockito.mock(Context::class.java)
//
//        val schedulers = TestSchedulersProvider()
//        settingsViewModel = SettingsViewModel(baseRepo, daoRepo, preferencesRepo, schedulers)
//        MockedPreferencesProvider(preferencesRepo)
//    }
//
//    @Test
//    fun `notifications status changed true`() {
//        preferencesRepo.areNotificationsOn = false
//        settingsViewModel.enableNotifications.observeForever {}
//
//        val token = "12345"
//        `when`(baseRepo.registerNotificationsToken()).thenReturn(Single.create { emitter ->
//            emitter.onSuccess(token)
//        })
//
//        settingsViewModel.enablePushNotifications(true)
//        assertThat(preferencesRepo.currentNotificationsTokenId).isEqualTo(token)
//        assertThat(preferencesRepo.notificationsTokenId).isEqualTo(token)
//        assertThat(preferencesRepo.areNotificationsOn).isEqualTo(true)
//    }
//
//    @Test
//    fun `notifications status changed false`() {
//        preferencesRepo.areNotificationsOn = false
//        settingsViewModel.enableNotifications.observeForever {}
//
//        val testException: Throwable = Exception("Test Exception")
//        `when`(baseRepo.registerNotificationsToken()).thenReturn(Single.create { emitter ->
//            emitter.onError(testException)
//        })
//
//        settingsViewModel.enablePushNotifications(true)
//        assertThat(preferencesRepo.currentNotificationsTokenId).isEqualTo("")
//        assertThat(preferencesRepo.notificationsTokenId).isEqualTo("")
//        assertThat(preferencesRepo.areNotificationsOn).isEqualTo(false)
//    }
//
//    @Test
//    fun `unregister notifications success`() {
//        val testNotificationToken = "12345"
//        preferencesRepo.areNotificationsOn = true
//        preferencesRepo.currentNotificationsTokenId = testNotificationToken
//        preferencesRepo.notificationsTokenId = testNotificationToken
//
//        settingsViewModel.enableNotifications.observeForever {}
//
//        `when`(baseRepo.unregisterForNotification()).thenReturn(Single.create { emitter ->
//            emitter.onSuccess(true)
//        })
//
//        settingsViewModel.enablePushNotifications(false)
//        assertThat(preferencesRepo.currentNotificationsTokenId).isEqualTo("")
//        assertThat(preferencesRepo.notificationsTokenId).isEqualTo("")
//        assertThat(preferencesRepo.areNotificationsOn).isEqualTo(false)
//    }
//
//    @Test
//    fun `unregister notifications fail`() {
//        val testNotificationToken = "12345"
//        preferencesRepo.areNotificationsOn = true
//        preferencesRepo.currentNotificationsTokenId = testNotificationToken
//        preferencesRepo.notificationsTokenId = testNotificationToken
//
//        settingsViewModel.enableNotifications.observeForever {}
//
//        val testException: Throwable = Exception("Test Exception")
//        `when`(baseRepo.unregisterForNotification()).thenReturn(Single.create { emitter ->
//            emitter.onError(testException)
//        })
//
//        settingsViewModel.enablePushNotifications(false)
//        assertThat(preferencesRepo.currentNotificationsTokenId).isEqualTo(testNotificationToken)
//        assertThat(preferencesRepo.notificationsTokenId).isEqualTo(testNotificationToken)
//        assertThat(preferencesRepo.areNotificationsOn).isEqualTo(true)
//    }
//
//    @Test
//    fun `enable debug isEnabled = true`() {
//        Mockito.mockStatic(DebugLogger::class.java).use { dummy ->
//            dummy.`when`<Single<Boolean>> { DebugLogger.initService(any()) }
//                .thenReturn(Single.create { emitter ->
//                    emitter.onSuccess(true)
//                })
//
//            preferencesRepo.areDebugLogsOn = true
//            settingsViewModel.enableDebug.observeForever{}
//
//            settingsViewModel.enableDebug(any(), true)
//
//            assertThat(settingsViewModel.enableDebug.value).isInstanceOf(SimpleRequestState.Success::class.java)
//            assertThat(preferencesRepo.areDebugLogsOn).isEqualTo(true)
//        }
//    }
//
//    @Test
//    fun `enable debug isEnabled = true with error`() {
//        try {
//            val error: Throwable = Exception()
//            Mockito.mockStatic(DebugLogger::class.java).use { dummy ->
//                dummy.`when`<Single<Boolean>> { DebugLogger.initService(any()) }
//                    .thenReturn(Single.create { emitter ->
//                        emitter.onError(error)
//                    })
//
//                preferencesRepo.areDebugLogsOn = false
//                settingsViewModel.enableDebug.observeForever {}
//
//                settingsViewModel.enableDebug(any(), true)
//
//                assertThat(settingsViewModel.enableDebug.value).isInstanceOf(SimpleRequestState.Error::class.java)
//                assertThat(preferencesRepo.areDebugLogsOn).isEqualTo(false)
//            }
//        } catch (err: Exception) {
//            err.printStackTrace()
//        }
//    }
//
//    @Test
//    fun `enable debug isEnabled = false`() {
//        preferencesRepo.areDebugLogsOn = true
//        settingsViewModel.enableDebug.observeForever {}
//        settingsViewModel.enableDebug(mockedContext, false)
//
//        assertThat(settingsViewModel.enableDebug.value).isEqualTo(SimpleRequestState.Success(false))
//        assertThat(preferencesRepo.areDebugLogsOn).isEqualTo(false)
//    }
//
//    @Test
//    fun `enable disable biometrics`() {
//        settingsViewModel.enableBiometrics(true)
//        assertThat(settingsViewModel.enableBiometrics.value).isEqualTo(SimpleRequestState.Success(true))
//        settingsViewModel.enableBiometrics(false)
//        assertThat(settingsViewModel.enableBiometrics.value).isEqualTo(SimpleRequestState.Success(false))
//    }
//
//    @Test
//    fun `export latest log line test`() {
//        assertDoesNotThrow("Exception") { settingsViewModel.exportLatestLog(mockedContext) }
//    }
//
//    @Test
//    fun `get log size`() {
//        assertThat(settingsViewModel.getLogSize(mockedContext)).isEqualTo("-")
//    }
//
//    @Test
//    fun `are push notifications on`() {
//        `when`(settingsViewModel.arePushNotificationsOn()).thenReturn(true)
//        assertThat(settingsViewModel.arePushNotificationsOn()).isEqualTo(true)
//    }
//
//    @Test
//    fun `are in-app notifications on`() {
//        `when`(settingsViewModel.areInAppNotificationsOn()).thenReturn(true)
//        assertThat(settingsViewModel.areInAppNotificationsOn()).isEqualTo(true)
//    }
//
//    @Test
//    fun `are debug logs on`() {
//        `when`(settingsViewModel.areDebugLogsOn()).thenReturn(true)
//        assertThat(settingsViewModel.areDebugLogsOn()).isEqualTo(true)
//    }
//
//    @Test
//    fun `enable crash report`() {
//        preferencesRepo.isCrashReportEnabled = false
//        settingsViewModel.enableCrashReport(true)
//        assertThat(settingsViewModel.isCrashReportOn()).isEqualTo(true)
//    }
//
//    @Test
//    fun `disable crash report`() {
//        preferencesRepo.isCrashReportEnabled = true
//        settingsViewModel.enableCrashReport(false)
//        assertThat(settingsViewModel.isCrashReportOn()).isEqualTo(false)
//    }
//}