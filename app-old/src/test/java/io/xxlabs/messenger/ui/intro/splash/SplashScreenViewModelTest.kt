package io.xxlabs.messenger.ui.intro.splash
//
//import android.app.Application
//import android.content.Context
//import com.google.common.truth.Truth.assertThat
//import io.xxlabs.messenger.InstantExecutorExtension
//import io.xxlabs.messenger.repository.DaoRepository
//import io.xxlabs.messenger.repository.PreferencesRepository
//import io.xxlabs.messenger.repository.base.BaseRepository
//import io.xxlabs.messenger.test.utils.MockedPreferencesProvider
//import io.xxlabs.messenger.test.utils.TestSchedulersProvider
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.Mockito
//
//@ExtendWith(InstantExecutorExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class SplashScreenViewModelTest {
//    lateinit var splashScreenViewModel: SplashScreenViewModel
//    lateinit var baseRepo: BaseRepository
//    lateinit var preferencesRepo: PreferencesRepository
//    lateinit var daoRepo: DaoRepository
//    lateinit var mockContext: Context
//    lateinit var mockAppContext: Application
//
//    @BeforeEach
//    fun setUp() {
//        baseRepo = Mockito.mock(BaseRepository::class.java)
//        daoRepo = Mockito.mock(DaoRepository::class.java)
//        preferencesRepo = Mockito.mock(PreferencesRepository::class.java)
//        mockContext = Mockito.mock(Context::class.java)
//        mockAppContext = Mockito.mock(Application::class.java)
//
//        val schedulers = TestSchedulersProvider()
//        splashScreenViewModel =
//            SplashScreenViewModel(baseRepo, preferencesRepo, schedulers)
//        MockedPreferencesProvider(preferencesRepo)
//    }
//
//    @Test
//    fun `register user with success`() {
//        val storageDir = "test"
//        val password = "123456".toByteArray()
//        Mockito.doNothing().`when`(baseRepo).newClient(storageDir, password)
//        Mockito.`when`(baseRepo.getSessionFolder(mockContext)).thenReturn(storageDir)
//        splashScreenViewModel.onRegisterSession.observeForever{}
//        splashScreenViewModel.registerUser(mockContext, byteArrayOf())
////        assertThat(splashScreenViewModel.registrationTime.value).isNotEmpty()
////        assertThat(splashScreenViewModel.onRegisterSession.value).isTrue()
//    }
//
////    @Test
////    fun `register user with error`() {
////        val storageDir = "test"
////        val password = "123456".toByteArray()
////        val exception = Exception()
////        Mockito.doThrow(exception).`when`(baseClientRepo).newClient(storageDir, password)
////        Mockito.`when`(baseClientRepo.getSessionFolder(mockContext)).thenReturn(storageDir)
////        splashScreenViewModel.registrationTime.observeForever{}
////        splashScreenViewModel.onRegisterSession.observeForever{}
////        splashScreenViewModel.registerUser(mockContext, byteArrayOf())
////    }
//
//    @Test
//    fun `check user session exists = true`() {
//        Mockito.`when`(baseRepo.doesBindingsFolderExists()).thenReturn(true)
//        val userSessionExists = splashScreenViewModel.doesUserSessionExists()
//        assertThat(userSessionExists).isTrue()
//    }
//
//    @Test
//    fun `check user session exists = false`() {
//        Mockito.`when`(baseRepo.doesBindingsFolderExists()).thenReturn(false)
//        val userSessionExists = splashScreenViewModel.doesUserSessionExists()
//        assertThat(userSessionExists).isFalse()
//    }
//}