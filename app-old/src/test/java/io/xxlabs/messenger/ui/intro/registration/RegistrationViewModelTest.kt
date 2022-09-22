package io.xxlabs.messenger.ui.intro.registration

//import android.app.Application
//import android.content.Context
//import android.widget.EditText
//import com.google.android.material.textfield.TextInputLayout
//import com.google.common.truth.Truth.assertThat
//import io.reactivex.Single
//import io.xxlabs.messenger.InstantExecutorExtension
//import io.xxlabs.messenger.data.data.Country
//import io.xxlabs.messenger.data.data.SimpleRequestState
//import io.xxlabs.messenger.data.datatype.input.RegistrationInputState
//import io.xxlabs.messenger.repository.DaoRepository
//import io.xxlabs.messenger.repository.PreferencesRepository
//import io.xxlabs.messenger.repository.base.BaseRepository
//import io.xxlabs.messenger.test.utils.MockEditable
//import io.xxlabs.messenger.test.utils.MockedPreferencesProvider
//import io.xxlabs.messenger.test.utils.TestSchedulersProvider
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.ArgumentMatchers.anyBoolean
//import org.mockito.ArgumentMatchers.anyString
//import org.mockito.Mockito
//
//@ExtendWith(InstantExecutorExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//internal class RegistrationViewModelTest {
//    lateinit var registrationViewModel: RegistrationViewModel
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
//        registrationViewModel =
//            RegistrationViewModel(baseRepo, daoRepo, preferencesRepo, schedulers)
//        MockedPreferencesProvider(preferencesRepo)
//    }
//
//    @Test
//    fun `register username success`() {
//        val name = "username"
//        Mockito.`when`(baseRepo.registerUdUsername(anyString())).thenReturn(Single.create { emitter ->
//            emitter.onSuccess(name)
//        })
//        registrationViewModel.usernameRequestState.observeForever {}
//
//        registrationViewModel.registerUsername(name)
//        assertThat(preferencesRepo.name).isEqualTo(name)
//        assertThat(registrationViewModel.usernameRequestState.value).isEqualTo(SimpleRequestState.Success(true))
//    }
//
//    @Test
//    fun `register username error`() {
//        val name = "username"
//        val error: Throwable = Exception("Error")
//
//        Mockito.`when`(baseRepo.registerUdUsername(anyString())).thenReturn(
//            Single.create { emitter ->
//                emitter.onError(error)
//            }
//        )
//
//        registrationViewModel.usernameRequestState.observeForever {}
//
//        registrationViewModel.registerUsername(name)
//        assertThat(preferencesRepo.name).isEqualTo("")
//        assertThat(registrationViewModel.usernameRequestState.value).isEqualTo(SimpleRequestState.Error<Throwable>(error))
//    }
//
//    @Test
//    fun `set registration state`() {
//        registrationViewModel.registrationState.observeForever {}
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NONE)
//        registrationViewModel.setRegistrationState(RegistrationInputState.NAME_MESSAGE)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NAME_MESSAGE)
//    }
//
//    @Test
//    fun `username validation - empty input`() {
//        val inputLayout = generateUsernameInputLayout("")
//        println(inputLayout.editText?.text?.length)
//        registrationViewModel.isNameValid(inputLayout)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.EMPTY)
//    }
//
//    @Test
//    fun `username validation - invalid length`() {
//        val inputLayout = generateUsernameInputLayout("abc")
//        registrationViewModel.isNameValid(inputLayout)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NAME_INVALID_LENGTH)
//    }
//
//    @Test
//    fun `username validation - invalid characters`() {
//        val inputLayout = generateUsernameInputLayout("abc $")
//        registrationViewModel.isNameValid(inputLayout)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NAME_INVALID_CHARACTERS)
//    }
//
//    @Test
//    fun `username validation - max characters username`() {
//        val inputLayout = generateUsernameInputLayout("usernameusernameusernameusernameusernameusernameusernameusername")
//        registrationViewModel.isNameValid(inputLayout)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NAME_MAX_CHARACTERS)
//    }
//
//    @Test
//    fun `username validation - valid username`() {
//        val inputLayout = generateUsernameInputLayout("username")
//        registrationViewModel.isNameValid(inputLayout)
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NAME_VALID)
//    }
//
//    @Test
//    fun `clear fields`() {
//        registrationViewModel.isUsernameValid = true
//        registrationViewModel.registrationState.value = RegistrationInputState.NAME_VALID
//        registrationViewModel.clearFields()
//
//        assertThat(registrationViewModel.isUsernameValid).isFalse()
//        assertThat(registrationViewModel.registrationState.value).isEqualTo(RegistrationInputState.NONE)
//    }
//
//    private fun generateUsernameInputLayout(text: String): TextInputLayout {
//        val editable = MockEditable(text)
//        val editText = Mockito.mock(EditText::class.java)
//        val inputLayout = Mockito.mock(TextInputLayout::class.java)
//        Mockito.`when`(inputLayout.editText).thenReturn(editText)
//        Mockito.`when`(editText.text).thenReturn(editable)
//        Mockito.`when`(editText.length()).thenReturn(editable.length)
//        return inputLayout
//    }
//
//    @Test
//    fun `get stored email`() {
//        val email = "email@email.com"
//        Mockito.`when`(baseRepo.getStoredEmail()).thenReturn(email)
//        assertThat(registrationViewModel.getStoredMail()).isEqualTo(email)
//    }
//
//    //TODO: Fix mock
//    fun `get stored phone`() {
//        val phone = "+1312123123"
//        Mockito.`when`(baseRepo.getStoredPhone()).thenReturn(phone)
//        Mockito.`when`(Country.countriesList).thenReturn(listOf())
//        Mockito.`when`(Country.toFormattedNumber(anyString(), anyBoolean())).thenReturn(phone)
//        assertThat(registrationViewModel.getStoredPhone()).isEqualTo(phone)
//    }
//
////    private fun generatePasswordInputLayout(pwd: String): TextInputLayout {
////        val editable = MockEditable(pwd)
////        val editText = Mockito.mock(EditText::class.java)
////        val inputLayout = Mockito.mock(TextInputLayout::class.java)
////        Mockito.`when`(inputLayout.editText).thenReturn(editText)
////        Mockito.`when`(inputLayout.editText?.inputType)
////            .thenReturn(InputType.TYPE_TEXT_VARIATION_PASSWORD + 1)
////        Mockito.`when`(editText.text).thenReturn(editable)
////        Mockito.`when`(editText.length()).thenReturn(editable.length)
////        return inputLayout
////    }
//}