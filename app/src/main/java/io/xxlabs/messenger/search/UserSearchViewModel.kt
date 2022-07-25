package io.xxlabs.messenger.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.xxlabs.messenger.R
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.ui.list.adapter.RequestItem
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.value
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.createTwoButtonDialogUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserSearchViewModel @Inject constructor(
    private val repo: BaseRepository,
    private val preferences: PreferencesRepository
): ViewModel() {

    val dialogUi: LiveData<TwoButtonInfoDialogUI?> by ::_dialogUi
    private val _dialogUi = MutableLiveData<TwoButtonInfoDialogUI?>(null)

    val toastUi: LiveData<ToastUI?> by ::_toastUi
    private val _toastUi = MutableLiveData<ToastUI?>(null)

    val usernameSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData(null)
            override val searchHint: String = "Search by username"
            override fun onCountryClicked() {}
        }
    }
    val emailSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData(null)
            override val searchHint: String = "Search by email address"
            override fun onCountryClicked() {}
        }
    }
    val phoneSearchUi: FactSearchUi by lazy {
        object : FactSearchUi {
            override val countryCode: LiveData<String?> = MutableLiveData("🇺🇸 +1")
            override val searchHint: String = "Search by phone number"
            override fun onCountryClicked() {
                onCountryCodeClicked()
            }
        }
    }

    private fun onCountryCodeClicked() {
        // Launch country code picker
    }

    val usernameResults: Flow<List<RequestItem>> by ::_usernameResults
    private val _usernameResults = MutableStateFlow<List<RequestItem>>(listOf())

    val emailResults: Flow<List<RequestItem>> by ::_emailResults
    private val _emailResults = MutableStateFlow<List<RequestItem>>(listOf())

    val phoneResults: Flow<List<RequestItem>> by ::_phoneResults
    private val _phoneResults = MutableStateFlow<List<RequestItem>>(listOf())

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

    fun onUsernameSearch(username: String?) {
        username?.let {
            showToast(username)
        }
    }

    fun onEmailSearch(email: String?) {
        email?.let {
            showToast(email)
        }
    }

    fun onPhoneSearch(phone: String?) {
        phone?.let {
            // Get cached country code from country picker dialog
            showToast("$phone")
        }
    }

    private fun showToast(error: String) {
        _toastUi.postValue(
            ToastUI.create(body = error)
        )
    }

    fun onToastShown() {
        _toastUi.value = null
    }

    fun onDialogShown() {
        _dialogUi.value = null
    }
}