package io.xxlabs.messenger.start.ui

import androidx.lifecycle.*
import bindings.Bindings
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.xxlabs.messenger.start.model.VersionData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * Responsible for minimum version enforcement and initializing core app components.
 */
class ColdStartViewModel : ViewModel() {

    val navigateToRegistration: LiveData<Boolean> by ::_navigateToRegistration
    private val _navigateToRegistration = MutableLiveData(false)

    val navigateToMain: LiveData<Boolean> by ::_navigateToMain
    private val _navigateToMain = MutableLiveData(false)

    val versionAlert: LiveData<VersionAlertUi?> by ::_versionAlert
    private val _versionAlert = MutableLiveData<VersionAlertUi?>(null)

    val error: LiveData<String?> by ::_error
    private val _error = MutableLiveData<String?>(null)

    val navigateToUrl: LiveData<String?> by ::_navigateToUrl
    private val _navigateToUrl = MutableLiveData<String?>(null)

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch(Dispatchers.IO) {
            maybeClearData()
            fetchCommonErrors()
            parseJson(downloadRegistrationJson())
        }
    }

    private suspend fun maybeClearData(): Boolean{
        return if (!userExists()) {
            clearAppDataAsync().await()
        } else true
    }

    fun userExists(): Boolean = false

    private fun clearAppDataAsync() : Deferred<Boolean> {
        return viewModelScope.async {
            File("Session folder").apply {
                if (exists()) {
                    Timber.v("Bindings folder from previous installation was found.")
                    Timber.v("It contains ${listFiles()?.size ?: 0} files.")
                    Timber.v("Deleting!")
                    deleteRecursively()
                }
            }
            true
        }
    }

    private fun fetchCommonErrors() {
        TODO()
    }

    private fun downloadRegistrationJson(): JsonObject {
//        val gson = GsonBuilder()
//            .setLenient()
//            .create()
//
//        val db = Bindings.downloadDAppRegistrationDB().decodeToString().replace("\n", "")
//        return gson.fromJson(db, JsonObject::class.java)

        TODO()
    }

    private fun parseJson(json: JsonElement) {
        val registrationWrapper = VersionData.from(json)
        val appVersion = registrationWrapper.appVersion
        val minVersion = registrationWrapper.minVersion
        val recommendedVersion = registrationWrapper.recommendedVersion
        val downloadUrl = registrationWrapper.downloadUrl
        val popupMessage = registrationWrapper.minPopupMessage

        when {
            appVersion < minVersion -> updateRequiredAlert(popupMessage, downloadUrl)
            appVersion >= minVersion && appVersion < recommendedVersion -> {
                updateRecommendedAlert(downloadUrl)
            }
            else -> versionOk()
        }
    }

    private fun updateRecommendedAlert(downloadUrl: String): VersionAlertUi {
        return VersionAlert(

        )
    }

    private fun updateRequiredAlert(message: String, downloadUrl: String): VersionAlertUi {
        return VersionAlert()
    }

    private fun versionOk() {
        _versionAlert.value = null
        determineNavigation()
    }

    private fun determineNavigation() {
        if (userExists()) {
            _navigateToMain.value = true
            _navigateToRegistration.value = false
        } else {
            _navigateToRegistration.value = true
            _navigateToMain.value = false
        }
    }

    fun onNavigationHandled() {
        _navigateToMain.value = null
        _navigateToRegistration.value = null
    }

    fun onVersionAlertShown() {
        _versionAlert.value = null
    }

    fun onUrlHandled() {
        _navigateToUrl.value = null
    }

    private fun onUpdateRecommendedPositiveClick(url: String) {
        _navigateToUrl.value = url
    }

    private fun onUpdateRecommendedNegativeClick() {
        determineNavigation()
    }

    private fun onUpdateRecommendedDismissed() {
        determineNavigation()
    }

    private fun onUpdateRequiredPositiveClick(url: String) {
        onUpdateRecommendedPositiveClick(url)
    }
}