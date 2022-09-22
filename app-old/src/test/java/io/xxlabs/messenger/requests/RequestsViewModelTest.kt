package io.xxlabs.messenger.requests
//
//import android.content.Context
//import com.google.common.truth.Truth.assertThat
//import io.mockk.every
//import io.mockk.junit5.MockKExtension
//import io.mockk.mockk
//import io.mockk.mockkStatic
//import io.xxlabs.messenger.InstantExecutorExtension
//import io.xxlabs.messenger.data.data.ContactRoundRequest
//import io.xxlabs.messenger.data.datatype.ContactRequestState
//import io.xxlabs.messenger.repository.DaoRepository
//import io.xxlabs.messenger.repository.PreferencesRepository
//import io.xxlabs.messenger.repository.base.BaseRepository
//import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
//import io.xxlabs.messenger.support.extensions.toBase64String
//import io.xxlabs.messenger.support.util.Utils
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.junit.jupiter.api.extension.ExtendWith
//
//@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//internal class RequestsViewModelTest {
//    lateinit var requestsViewModel: RequestsViewModel
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
//        preferencesRepo =  mockk(relaxUnitFun = true)
//
//        requestsViewModel = RequestsViewModel(baseRepo, daoRepo, preferencesRepo)
//    }
//
//    @Test
//    fun `change and get search filter`() {
//        requestsViewModel.currentSearchFilter.observeForever {}
//
//        val filter = RequestsFilter.SENT
//        requestsViewModel.changeSearchFilter(filter)
//        assertThat(requestsViewModel.currentSearchFilter.value).isEqualTo(filter)
//        assertThat(requestsViewModel.getCurrentFilter()).isEqualTo(filter)
//    }
//
//    @Test
//    fun `GET ADD contacts rounds received sent failed`() {
//        val uid = "12345"
//        val uid2 = "123456"
//        val uid3 = "123457"
//
//        val contactRoundRequestReceived = ContactRoundRequest(
//            uid.toByteArray(),
//            "user",
//            1,
//            Utils.getCurrentTimeStamp(),
//            isSent = false,
//            ContactRequestState.RECEIVED
//        )
//
//        val contactRoundRequestSuccess = ContactRoundRequest(
//            uid2.toByteArray(),
//            "user2",
//            2,
//            Utils.getCurrentTimeStamp(),
//            true,
//            ContactRequestState.SUCCESS
//        )
//
//        val contactRoundRequestFailed = ContactRoundRequest(
//            uid3.toByteArray(),
//            "user3",
//            3,
//            Utils.getCurrentTimeStamp(),
//            true,
//            ContactRequestState.FAILED
//        )
//
//        val valid = "valid"
//        val valid2 = "valid"
//        val valid3 = "valid"
//
//        mockkStatic(String::fromBase64toByteArray)
//        every { uid.toByteArray().toBase64String() } returns valid
//        every { uid2.toByteArray().toBase64String() } returns valid2
//        every { uid3.toByteArray().toBase64String() } returns valid3
//
//
//        mockkStatic(ByteArray::toBase64String)
//        every { valid.fromBase64toByteArray() } returns uid.toByteArray()
//        every { valid2.fromBase64toByteArray() } returns uid2.toByteArray()
//        every { valid3.fromBase64toByteArray() } returns uid3.toByteArray()
//
//        preferencesRepo.addContactRequest(contactRoundRequestReceived)
//        preferencesRepo.addContactRequest(contactRoundRequestSuccess)
//        preferencesRepo.addContactRequest(contactRoundRequestFailed)
//
//        every { preferencesRepo.getContactRequest(RequestsFilter.RECEIVED) } returns listOf(contactRoundRequestReceived)
//        every { preferencesRepo.getContactRequest(RequestsFilter.SENT) } returns listOf(contactRoundRequestSuccess)
//        every { preferencesRepo.getContactRequest(RequestsFilter.FAILED) } returns listOf(contactRoundRequestFailed)
//
//        assertThat(requestsViewModel.getContactRounds(RequestsFilter.RECEIVED)).isEqualTo(
//            listOf(contactRoundRequestReceived)
//        )
//        assertThat(requestsViewModel.getContactRounds(RequestsFilter.SENT)).isEqualTo(
//            listOf(contactRoundRequestSuccess)
//        )
//        assertThat(requestsViewModel.getContactRounds(RequestsFilter.FAILED)).isEqualTo(
//            listOf(contactRoundRequestFailed)
//        )
//    }
//}