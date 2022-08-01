package io.xxlabs.messenger.search

import android.graphics.Bitmap
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.*
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.data.contact.ContactRequestData
import io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.model.Request
import io.xxlabs.messenger.requests.ui.list.adapter.*
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.value
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.createInfoDialog
import io.xxlabs.messenger.ui.dialog.info.createTwoButtonDialogUi
import io.xxlabs.messenger.ui.main.countrycode.CountrySelectionListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class UserSearchViewModel @Inject constructor(
    private val repo: BaseRepository,
    private val daoRepo: DaoRepository,
    private val preferences: PreferencesRepository,
    private val requestsDataSource: ContactRequestsRepository,
): ViewModel(){

    var previousTabPosition: Int = UserSearchFragment.SEARCH_USERNAME

    private val initialState: SearchUiState by lazy {
        SearchUiState(
            callToActionText = callToActionText,
            placeholderText = placeholderText,
            placeholderVisible = true,
            placeHolderClicked = ::onPlaceholderClicked
        )
    }

    private val userInputState: SearchUiState by lazy {
        SearchUiState()
    }

    private val searchRunningState: SearchUiState by lazy {
        SearchUiState(
            isSearching = true,
            cancelClicked = ::onCancelSearchClicked
        )
    }

    private val searchCompleteState: SearchUiState by lazy {
        SearchUiState(isSearching = false)
    }

    private val callToActionText: Spanned by lazy {
        val highlight = appContext().getColor(R.color.brand_default)
        val cta = appContext().getString(R.string.search_call_to_action)

        val span1 = appContext().getString(R.string.search_call_to_action_span_1)
        val span1Start = cta.indexOf(span1, ignoreCase = true)
        val span1End = span1Start + span1.length

        val span2 = appContext().getString(R.string.search_call_to_action_span_2)
        val span2Start = cta.indexOf(span2, ignoreCase = true)
        val span2End = span2Start + span2.length

        SpannableString(cta).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                span1Start,
                span1End,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlight),
                span2Start,
                span2End,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private val placeholderText: Spanned by lazy {
        val highlight = appContext().getColor(R.color.brand_default)
        val text = appContext().getString(R.string.search_placeholder_text)
        val span = appContext().getString(R.string.search_placeholder_span)
        val startIndex = text.indexOf(span, ignoreCase = true)

        SpannableString(text).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                startIndex + span.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    val searchInfoDialog: LiveData<InfoDialogUI?> by ::_searchInfoDialog
    private val _searchInfoDialog = MutableLiveData<InfoDialogUI?>(null)

    private val searchInfoDialogUi: InfoDialogUI by lazy {
        createInfoDialog(
            title = R.string.search_info_dialog_title,
            body = R.string.search_info_dialog_body,
            linkTextToUrlMap = mapOf(
                appContext().getString(R.string.search_info_dialog_link_text)
                        to appContext().getString(R.string.search_info_dialog_link_url)
            )
        )
    }

    val udSearchUi: LiveData<UdSearchUi> by ::_udSearchUi
    private val _udSearchUi = MutableLiveData<UdSearchUi>(initialState)

    val dialogUi: LiveData<TwoButtonInfoDialogUI?> by ::_dialogUi
    private val _dialogUi = MutableLiveData<TwoButtonInfoDialogUI?>(null)

    val toastUi: LiveData<ToastUI?> by ::_toastUi
    private val _toastUi = MutableLiveData<ToastUI?>(null)

    private val _userInputEnabled = Transformations.map(udSearchUi) { state ->
        state != searchRunningState
    }

    val usernameSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData(null)
            override val searchHint: String = "Search by username"
            override val userInputEnabled: LiveData<Boolean> by ::_userInputEnabled
            override fun onCountryClicked() {}
            override fun onSearchInput(editable: Editable?) = onUserInput(editable?.toString())
        }
    }
    val emailSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData(null)
            override val searchHint: String = "Search by email address"
            override val userInputEnabled: LiveData<Boolean> by ::_userInputEnabled
            override fun onCountryClicked() {}
            override fun onSearchInput(editable: Editable?) = onUserInput(editable?.toString())
        }
    }
    val phoneSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> by ::dialCode
            override val searchHint: String = "Search by phone number"
            override val userInputEnabled: LiveData<Boolean> by ::_userInputEnabled
            override fun onCountryClicked() { onCountryCodeClicked() }
            override fun onSearchInput(editable: Editable?) = onUserInput(editable?.toString())
        }
    }
    val qrSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData(null)
            override val searchHint: String = "Search by QR code"
            override val userInputEnabled: LiveData<Boolean> = MutableLiveData(false)
            override fun onCountryClicked() { }
            override fun onSearchInput(editable: Editable?) = onUserInput(editable?.toString())
        }
    }

    private var country: Country = Country.getDefaultCountry()
        set(value) {
            dialCode.value = "${value.flag}  ${value.dialCode}"
            field = value
        }
    private val dialCode = MutableLiveData("${country.flag}  ${country.dialCode}")

    val selectCountry: LiveData<CountrySelectionListener?> by ::_selectCountry
    private val _selectCountry = MutableLiveData<CountrySelectionListener?>(null)

    private val countryListener: CountrySelectionListener by lazy {
        object : CountrySelectionListener {
            override val onDismiss = { _selectCountry.value = null }
            override fun onItemSelected(country: Country) = onCountrySelected(country)
        }
    }

    val usernameResults: Flow<List<RequestItem>> by ::_usernameResults
    private val _usernameResults = MutableStateFlow<List<RequestItem>>(listOf())

    val emailResults: Flow<List<RequestItem>> by ::_emailResults
    private val _emailResults = MutableStateFlow<List<RequestItem>>(listOf())

    val phoneResults: Flow<List<RequestItem>> by ::_phoneResults
    private val _phoneResults = MutableStateFlow<List<RequestItem>>(listOf())

    private var searchJob: Job? = null

    init {
        showNewUserPopups()
    }

    private fun showNewUserPopups() {
        if (preferences.isFirstTimeNotifications) {
            showNotificationDialog()
        }
    }

    private fun showNotificationDialog() {
        _dialogUi.value = createTwoButtonDialogUi(
            title = R.string.settings_push_notifications_dialog_title,
            body = R.string.settings_push_notifications_dialog_body,
            linkTextToUrlMap = null,
            positiveClick = ::enablePushNotifications,
            negativeClick = null,
            onDismiss = ::showCoverMessageDialog
        )
        preferences.isFirstTimeNotifications = false
    }

    private fun enablePushNotifications() {
        viewModelScope.launch {
            try {
                val notificationToken = enableNotifications()
                onNotificationsEnabled(notificationToken)
            } catch (e: Exception) {
                showToast(
                    e.message ?: "Failed to enable notifications. Please try again in Settings."
                )
            }
        }
    }

    private fun onNotificationsEnabled(token: String) {
        token.run {
            Timber.d("New token successfully sent! $this")
            preferences.areNotificationsOn = true
            preferences.currentNotificationsTokenId = this
            preferences.notificationsTokenId = this
        }
    }

    private suspend fun enableNotifications() = repo.registerNotificationsToken().value()

    private fun showCoverMessageDialog() {
        _dialogUi.value = createTwoButtonDialogUi(
            title = R.string.settings_cover_traffic_title,
            body = R.string.settings_cover_traffic_dialog_body,
            linkTextToUrlMap = mapOf(
                appContext().getString(R.string.settings_cover_traffic_link_text)
                        to appContext().getString(R.string.settings_cover_traffic_link_url)
            ),
            positiveClick = { enableDummyTraffic(true) },
            negativeClick = { enableDummyTraffic(false) },
        )
        preferences.isFirstTimeCoverMessages = false
    }

    private fun enableDummyTraffic(enabled: Boolean) {
        preferences.isCoverTrafficOn = enabled
        repo.enableDummyTraffic(enabled)
    }

    suspend fun onUsernameSearch(username: String?): Flow<List<RequestItem>> {
        _usernameResults.value = listOf()
        val factQuery = FactQuery.UsernameQuery(username)
        return search(factQuery).cancellable()
    }

    suspend fun onEmailSearch(email: String?): Flow<List<RequestItem>> {
        _emailResults.value = listOf()
        val factQuery = FactQuery.EmailQuery(email)
        return search(factQuery).cancellable()
    }

    suspend fun onPhoneSearch(phone: String?): Flow<List<RequestItem>> {
        _phoneResults.value = listOf()
        val factQuery = FactQuery.PhoneQuery(phone + country.countryCode)
        return search(factQuery).cancellable()
    }

    private suspend fun search(factQuery: FactQuery): Flow<List<RequestItem>> {
        // Cancel previous searches, save a reference to this one.
        searchJob?.cancel()
        searchJob = coroutineContext.job

        if (!isValidQuery(factQuery)) flowOf(listOf<RequestItem?>())

        _udSearchUi.value = searchRunningState

        return combine(
            searchUd(factQuery),
            allRequests(),
            allConnections()
        ) { ud, allRequests, allConnections ->
            val foundRequests = allRequests.matching(factQuery).toMutableSet()
            val foundConnections = allConnections.matching(factQuery).toMutableSet()

            // Add identical request to request results, if not already there.
            allRequests.identicalTo(ud.username)?.let {
                foundRequests.add(it)
            }

            // Add identical connection to connection results, if not already there.
            allConnections.identicalTo(ud.username)?.let {
                foundConnections.add(it)
            }

            val alreadyRequested = ud.username in foundRequests.map { request ->
                request.username
            }

            val alreadyAdded = ud.username in foundConnections.map { connection ->
                connection.username
            }

            val nonConnections =
                if (alreadyRequested || alreadyAdded) {
                    // If the UD result's userID match a request's userID, only show the request
                    foundRequests.toList().sortedBy { it.username }
                } else {
                    // Otherwise show both
                    listOf(ud) + foundRequests.sortedBy { it.username }
                }


            if (nonConnections.isEmpty()) {
                // If there's no UD result or Requests, just show the Connections with no divider.
                foundConnections.toList().sortedBy {
                    it.username
                }.ifEmpty {
                    // Show a "no results found" placeholder if there's nothing at all.
                    noResultsFor(factQuery)
                }
            } else {
                if (foundConnections.isEmpty()) {
                    // If there's no Connections, show the UD & Request results.
                    nonConnections
                } else {
                    // Or show the UD results, Requests, a divider, and finally Connections.
                    nonConnections
                        .plus(listOf(ConnectionsDividerItem()))
                        .plus(foundConnections.toList().sortedBy { it.username })
                }
            }
        }
    }

    private val RequestItem.username: String
        get() = (request as? ContactRequest)?.model?.username ?: ""

    private suspend fun allRequests(): Flow<List<RequestItem>> =
        requestsDataSource.getRequests().mapNotNull { requestsList ->
            requestsList.map {
                it.asRequestSearchResult()
            }
        }.stateIn(viewModelScope)

    private suspend fun allConnections() = flow {
        val connectionsList = savedUsers().filter {
            it.isConnection()
        }.asConnectionsSearchResult()
        emit(connectionsList)
    }.stateIn(viewModelScope)

    private fun List<RequestItem>.identicalTo(username: String): RequestItem? =
        firstOrNull { it.username == username }

    private fun List<RequestItem>.matching(factQuery: FactQuery): List<RequestItem> {
        return when (factQuery.type) {
            FactType.USERNAME -> {
                filter {
                    (it.request as? ContactRequest)?.model?.displayName?.contains(
                        factQuery.fact,
                        true
                    ) ?: false
                }
            }
            FactType.EMAIL -> {
                filter {
                    (it.request as? ContactRequest)?.model?.email?.contains(
                        factQuery.fact,
                        true
                    ) ?: false
                }
            }
            FactType.PHONE -> {
                filter {
                    (it.request as? ContactRequest)?.model?.phone?.contains(
                        factQuery.fact,
                        true
                    ) ?: false
                }
            }
            else -> listOf()
        }
    }

    private fun isValidQuery(factQuery: FactQuery): Boolean {
        return with (factQuery.fact) {
            if (isNullOrBlank()) {
                // Prevent blank text
                false
            } else {
                // Prevent users from searching (and possibly requesting) themselves.
                this != repo.getStoredUsername()
                        && this != repo.getStoredEmail()
                        && this != repo.getStoredPhone()
            }
        }
    }

    private fun noResultsFor(factQuery: FactQuery): List<RequestItem> =
        listOf(noResultPlaceholder(factQuery))

    private fun noResultPlaceholder(factQuery: FactQuery): RequestItem =
        EmptyPlaceholderItem(
            text = "There are no users with that ${factQuery.type.name.lowercase()}."
        )

    private suspend fun savedUsers(): List<ContactData> =
        daoRepo.getAllContacts().value()

    private fun ContactData.isConnection(): Boolean =
        RequestStatus.from(status) == RequestStatus.ACCEPTED

    private suspend fun searchConnections(factQuery: FactQuery) = flow {
        val results = when (factQuery.type) {
            FactType.USERNAME -> {
                savedUsers().filter {
                    it.isConnection() && it.displayName.contains(factQuery.fact, true)
                }.asConnectionsSearchResult()

            }
            FactType.EMAIL -> {
                savedUsers().filter {
                    it.isConnection() && it.email.contains(factQuery.fact, true)
                }.asConnectionsSearchResult()

            }
            FactType.PHONE -> {
                savedUsers().filter {
                    it.isConnection() && it.phone.contains(factQuery.fact, true)
                }.asConnectionsSearchResult()
            }
            else -> listOf()
        }
        emit(results)
    }.stateIn(viewModelScope)

    private suspend fun searchRequests(factQuery: FactQuery) =
        when (factQuery.type) {
            FactType.USERNAME -> {
                filterRequests {
                    it.model.displayName.contains(factQuery.fact, true)
                }
            }
            FactType.EMAIL -> {
                filterRequests {
                    it.model.email.contains(factQuery.fact, true)
                }
            }
            FactType.PHONE -> {
                filterRequests {
                    it.model.phone.contains(
                        Country.toFormattedNumber(factQuery.fact, false) ?: factQuery.fact
                    )
                }
            }
            else -> flow { listOf<RequestItem>() }
        }.stateIn(viewModelScope)

    private suspend fun filterRequests(match: (contactRequest: ContactRequest) -> Boolean) =
        requestsDataSource.getRequests().map { requestsList ->
            requestsList.filter {
                match(it)
            }.map {
                it.asRequestSearchResult()
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun List<ContactData>.asConnectionsSearchResult(): List<RequestItem> =
        map {
            val requestData = ContactRequestData(it)
            AcceptedConnectionItem(
                requestData,
                resolveBitmap(it.photo)
            )
        }

    private suspend fun ContactRequest.asRequestSearchResult(): RequestItem =
        ContactRequestSearchResultItem(
            contactRequest = this,
            photo = resolveBitmap(model.photo),
            statusText = model.statusText(),
            statusTextColor = model.statusTextColor(),
            actionVisible = model.actionVisible(),
            actionIcon = model.actionIcon(),
            actionIconColor = model.actionIconColor(),
            actionTextStyle = model.actionTextStyle(),
            actionLabel = model.actionLabel()
        )

    private fun Contact.statusText(): String {
        return when (RequestStatus.from(status)) {
            RequestStatus.SENT,
            RequestStatus.VERIFIED,
            RequestStatus.RESET_SENT,
            RequestStatus.RESENT,
            RequestStatus.VERIFYING,
            RequestStatus.HIDDEN,
            RequestStatus.SENDING -> "Request pending"

            RequestStatus.SEND_FAIL,
            RequestStatus.CONFIRM_FAIL,
            RequestStatus.VERIFICATION_FAIL,
            RequestStatus.RESET_FAIL -> "Request failed"

            else -> ""
        }
    }

    private fun Contact.statusTextColor(): Int {
        return when (RequestStatus.from(status)) {
            RequestStatus.SEND_FAIL,
            RequestStatus.CONFIRM_FAIL,
            RequestStatus.VERIFICATION_FAIL,
            RequestStatus.RESET_FAIL ->  R.color.accent_danger

            else -> R.color.neutral_weak
        }
    }

    private fun Contact.actionVisible(): Boolean {
        return when (RequestStatus.from(status)) {
            RequestStatus.VERIFIED, RequestStatus.VERIFYING, RequestStatus.HIDDEN -> false
            else -> true
        }
    }

    private fun Contact.actionIcon(): Int {
        return when (RequestStatus.from(status)) {
            RequestStatus.RESENT -> R.drawable.ic_check_green
            else -> R.drawable.ic_retry
        }
    }

    private fun Contact.actionIconColor(): Int {
        return when (RequestStatus.from(status)) {
            RequestStatus.RESENT ->  R.color.accent_success
            else -> R.color.brand_default
        }
    }

    private fun Contact.actionTextStyle(): Int {
        return when (RequestStatus.from(status)) {
            RequestStatus.RESENT -> R.drawable.ic_check_green
            else -> R.style.request_item_resent
        }
    }

    private fun Contact.actionLabel(): String {
        return when (RequestStatus.from(status)) {
            RequestStatus.RESENT -> appContext().getString(R.string.request_item_action_resent)
            else -> appContext().getString(R.string.request_item_action_retry)
        }
    }


    private suspend fun resolveBitmap(data: ByteArray?): Bitmap? = withContext(Dispatchers.IO) {
        BitmapResolver.getBitmap(data)
    }

    private suspend fun searchUd(factQuery: FactQuery) = flow {
        val result = try {
            val udResult = repo.searchUd(factQuery.fact, factQuery.type).value()
            udResult.second?.let { // Error message
                if (it.isNotEmpty()) {
                    if (!it.contains("no results found", true)) {
                        showToast(it)
                    }
                    _udSearchUi.value = searchCompleteState
                    noResultPlaceholder(factQuery)
                } else { // Search result
                    _udSearchUi.value = searchCompleteState
                    udResult.first?.asSearchResult() ?: noResultPlaceholder(factQuery)
                }
            } ?: run {
                _udSearchUi.value = searchCompleteState
                udResult.first?.asSearchResult() ?: noResultPlaceholder(factQuery)
            }
        } catch (e: Exception) {
            e.message?.let { showToast(it) }
            _udSearchUi.value = searchCompleteState
            noResultPlaceholder(factQuery)
        }
        emit(result)
    }.stateIn(viewModelScope)

    private fun ContactWrapperBase.asSearchResult(): RequestItem {
        // ContactWrapperBase -> ContactRequestData
        val requestData = ContactRequestData(
            ContactData.from(this, RequestStatus.SEARCH)
        )
        // ContactRequestData -> RequestItem
        return SearchResultItem(requestData)
    }

    private fun showToast(error: String) {
        _toastUi.postValue(
            ToastUI.create(
                body = error,
                leftIcon = R.drawable.ic_alert
            )
        )
    }

    fun onToastShown() {
        _toastUi.value = null
    }

    fun onDialogShown() {
        _dialogUi.value = null
    }

    private fun onPlaceholderClicked() {
        _searchInfoDialog.value = searchInfoDialogUi
    }

    fun onInfoDialogShown() {
        _searchInfoDialog.value = null
    }

    private fun onCancelSearchClicked() {
        searchJob?.cancel()
        _udSearchUi.value = searchCompleteState
    }

    private fun onCountryCodeClicked() {
        _selectCountry.value = countryListener
    }

    val dismissCountries: LiveData<Boolean> by ::_dismissCountries
    private val _dismissCountries = MutableLiveData(false)

    private fun onCountrySelected(selectedCountry: Country?) {
        _dismissCountries.value = true
        country = selectedCountry ?: return
    }

    fun onCountriesDismissed() {
        _dismissCountries.value = false
    }

    fun onUserInput(input: String?) {
        _udSearchUi.value = input?.let {
            userInputState
        } ?: initialState
    }
}

private sealed class FactQuery {
    abstract val fact: String
    abstract val type: FactType

    class UsernameQuery(query: String?): FactQuery() {
        override val fact: String = query ?: ""
        override val type: FactType = FactType.USERNAME
    }

    class EmailQuery(query: String?): FactQuery() {
        override val fact: String = query ?: ""
        override val type: FactType = FactType.EMAIL
    }

    class PhoneQuery(query: String?): FactQuery() {
        override val fact: String = query ?: ""
        override val type: FactType = FactType.PHONE
    }
}