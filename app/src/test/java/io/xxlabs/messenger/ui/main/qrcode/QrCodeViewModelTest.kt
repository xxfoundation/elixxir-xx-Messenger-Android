package io.xxlabs.messenger.ui.main.qrcode

//import android.content.Context
//import com.google.common.truth.Truth
//import io.mockk.junit5.MockKExtension
//import io.mockk.mockk
//import io.xxlabs.messenger.InstantExecutorExtension
//import io.xxlabs.messenger.repository.DaoRepository
//import io.xxlabs.messenger.repository.PreferencesRepository
//import io.xxlabs.messenger.repository.base.BaseRepository
//import io.xxlabs.messenger.test.utils.TestSchedulersProvider
//import org.junit.Test
//
//@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//internal class QrCodeViewModelTest {
//    lateinit var qrCodeViewModel: QrCodeViewModel
//    lateinit var baseRepo: BaseRepository
//    lateinit var mockedContext: Context
//    lateinit var preferencesRepo: PreferencesRepository
//    lateinit var daoRepo: DaoRepository
//
//    @BeforeEach
//    fun setup() {
//        baseRepo = mockk(relaxed = true)
//        daoRepo = mockk(relaxed = true)
//        mockedContext = mockk(relaxed = true)
//        preferencesRepo = mockk(relaxUnitFun = true)
//
//        val schedulers = TestSchedulersProvider()
//        qrCodeViewModel = QrCodeViewModel(baseRepo, daoRepo, preferencesRepo, schedulers)
//    }
//
//    @Test
//    fun `change background`() {
//        qrCodeViewModel.background.observeForever {}
//        qrCodeViewModel.setWindowBackgroundColor(android.R.color.black)
//        Truth.assertThat(qrCodeViewModel.background.value).isEqualTo(android.R.color.black)
//    }
//}