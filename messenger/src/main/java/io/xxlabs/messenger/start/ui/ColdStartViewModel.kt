package io.xxlabs.messenger.start.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bindings.Bindings
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.xxlabs.messenger.start.model.VersionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * Responsible for minimum version enforcement and initializing core app components.
 */
class ColdStartViewModel(application: Application) : AndroidViewModel(application) {

    val navigateToRegistration: LiveData<Boolean> by ::_navigateToRegistration
    private val _navigateToRegistration = MutableLiveData(false)

    val navigateToMain: LiveData<Boolean> by ::_navigateToMain
    private val _navigateToMain = MutableLiveData(false)

    val versionAlert: LiveData<VersionAlertUi?> by ::_versionAlert
    private val _versionAlert = MutableLiveData<VersionAlertUi?>(null)

    val error: LiveData<String?> by ::_error
    private val _error = MutableLiveData<String?>(null)

    init {
        initializeApp()
    }

    private fun initializeApp() {
        maybeClearData()
        fetchCommonErrors()
        parseJson(downloadRegistrationJson())
    }

    private fun maybeClearData() {
        if (userExists()) {
            _navigateToMain.value = true
        } else {
            clearAppData()
        }
    }

    fun userExists(): Boolean = false

    private fun clearAppData() {
        viewModelScope.launch(Dispatchers.IO) {
            File("Session folder").apply {
                if (exists()) {
                    Timber.v("Bindings folder from previous installation was found.")
                    Timber.v("It contains ${listFiles()?.size ?: 0} files.")
                    Timber.v("Deleting!")
                    deleteRecursively()
                }
                _navigateToRegistration.postValue(true)
            }
        }
    }

    private fun fetchCommonErrors() {

    }

    private fun downloadRegistrationJson(): JsonObject {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val db = Bindings.downloadDAppRegistrationDB().decodeToString().replace("\n", "")
        return gson.fromJson(db, JsonObject::class.java)
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
        return VersionAlert()
    }

    private fun updateRequiredAlert(message: String, downloadUrl: String): VersionAlertUi {
        return VersionAlert()
    }

    private fun versionOk() {
        _versionAlert.value = null
    }

    fun onVersionAlertShown() {
        _versionAlert.value = null
    }
}