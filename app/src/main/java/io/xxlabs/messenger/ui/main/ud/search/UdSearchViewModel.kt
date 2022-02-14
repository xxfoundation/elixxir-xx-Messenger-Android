package io.xxlabs.messenger.ui.main.ud.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.data.ContactSearchResult
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.singleThread
import timber.log.Timber
import java.nio.charset.Charset
import javax.inject.Inject

class UdSearchViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
    var subscriptions = CompositeDisposable()
    val searchState = MutableLiveData<DataRequestState<ByteArray>>()
    val contactResult = MutableLiveData<ContactSearchResult>()

    //Registration Step State
    var currentSearchFilter = MutableLiveData<UdSearchFilter>()

    init {
        currentSearchFilter.value = UdSearchFilter.USERNAME
    }

    fun changeSearchFilter(udSearchFilter: UdSearchFilter) {
        currentSearchFilter.value = udSearchFilter
    }

    fun searchUd(factToSearch: String) {
        val type = getFactFromFilter()
        val cleanFact = getFilteredFact(factToSearch)
        singleThread {
            val time = System.currentTimeMillis()
            repo.searchUd(cleanFact, type) { contact: ContactWrapperBase?, error: String? ->
                Timber.d("Searched facts: ${contact?.getStringifiedFacts()}")
                val searchResult = ContactSearchResult(contact, error ?: "")
                schedulers.main.scheduleDirect {
                    contactResult.value = searchResult
                }
                Timber.v("Search Ud ($type) - Total execution time: ${System.currentTimeMillis() - time}ms")
            }
        }
    }

    private fun getFilteredFact(factToSearch: String) = when (getCurrentFilter()) {
        UdSearchFilter.USERNAME -> {
            "U$factToSearch"
        }

        UdSearchFilter.EMAIL -> {
            "E$factToSearch"
        }

        UdSearchFilter.PHONE -> {
            "P$factToSearch"
        }
    }

    private fun getFactFromFilter() = when (getCurrentFilter()) {
        UdSearchFilter.USERNAME -> {
            FactType.USERNAME
        }

        UdSearchFilter.EMAIL -> {
            FactType.EMAIL
        }

        UdSearchFilter.PHONE -> {
            FactType.PHONE
        }
    }

    fun getCurrentFilter(): UdSearchFilter {
        return currentSearchFilter.value ?: UdSearchFilter.USERNAME
    }

    override fun onCleared() {
        contactResult.postValue(ContactSearchResult())
        super.onCleared()
    }

    fun searchTest() {
        val userId = "2iWr+pG63wl300aOnIJFHsio1AVFJ74cKEjWav5Exl8D"
        repo.userLookup(userId.fromBase64toByteArray()
        ) { contact, error ->
            Timber.v("[TEST] error: $error")
            Timber.v("[TEST] Contact obj: $contact")
            Timber.v("[TEST] Contact userId: ${contact?.getId()}")
        }
    }
}
