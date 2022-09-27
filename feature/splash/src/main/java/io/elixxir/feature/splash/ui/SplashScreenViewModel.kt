package io.elixxir.feature.splash.ui

import androidx.lifecycle.*

import dagger.hilt.android.lifecycle.HiltViewModel
import io.elixxir.core.logging.NotExposedYet
import io.elixxir.core.logging.log
import io.elixxir.core.ui.model.UiText
import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.model.SessionState
import io.elixxir.data.version.VersionRepository
import io.elixxir.feature.splash.model.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Responsible for minimum version enforcement and initializing core app components.
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val sessionRepo: SessionRepository,
    private val versionRepo: VersionRepository,
) : ViewModel() {

    private val _appState = MutableStateFlow(
        AppState(userState, null)
    )
    val appState = _appState.asStateFlow()

    private val _launchUrl = MutableSharedFlow<String?>()
    val launchUrl = _launchUrl.asSharedFlow()

    private val userState: SessionState
        get() = if (userExists()) SessionState.ExistingUser else SessionState.NewUser

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch(Dispatchers.IO) {
            maybeClearData()
//            fetchCommonErrors()
//            parseJson(downloadRegistrationJson())
            _appState.emit(
                AppState(
                    userState = userState,
                    versionState = VersionOk()
                )
            )
        }
    }

    fun userExists(): Boolean = preferences.doesUserExist()

    private suspend fun maybeClearData(): Boolean{
        return if (!userExists()) {
            clearAppDataAsync().await()
        } else true
    }

    private fun clearAppDataAsync() : Deferred<Boolean> {
        return viewModelScope.async {
            File("Session folder").apply {
                if (exists()) {
                    log("Bindings folder from previous installation was found.")
                    log("It contains ${listFiles()?.size ?: 0} files.")
                    log("Deleting!")
                    deleteRecursively()
                }
            }
            true
        }
    }

    private fun fetchCommonErrors() {
        NotExposedYet()
    }

    private fun updateRecommended(downloadUrl: String): VersionState {
        return UpdateRecommended(
            VersionAlert(
                title = UiText.StringResource(R.string.version_alert_update_recommended_title),
                body = UiText.StringResource(R.string.version_alert_update_recommended_subtitle),
                positiveLabel = UiText.StringResource(R.string.version_alert_update_required_positive_label),
                negativeLabel = UiText.StringResource(R.string.version_alert_update_recommended_negative_label),
                onPositiveClick = { onUpdateRequiredPositiveClick(downloadUrl) },
                onNegativeClick = ::onUpdateRecommendedNegativeClick,
                onDismissed = ::onUpdateRecommendedDismissed,
                dismissable = true,
                downloadUrl = downloadUrl
            )
        )
    }

    private fun updateRequired(message: String, downloadUrl: String): VersionState {
        return UpdateRequired(
            VersionAlert(
                title = UiText.StringResource(R.string.version_alert_update_required_title),
                body = UiText.DynamicString(message),
                positiveLabel = UiText.StringResource(R.string.version_alert_update_required_positive_label),
                negativeLabel = UiText.StringResource(R.string.version_alert_update_recommended_negative_label),
                onPositiveClick = { onUpdateRequiredPositiveClick(downloadUrl) },
                onNegativeClick = { },
                onDismissed = { },
                dismissable = false,
                downloadUrl = downloadUrl
            )
        )
    }

    private fun onUpdateRecommendedPositiveClick(url: String) {
        viewModelScope.launch {
            _launchUrl.emit(url)
        }
    }

    private fun onUpdateRecommendedNegativeClick() {
        onUpdateRecommendedDismissed()
    }

    private fun onUpdateRecommendedDismissed() {
        _appState.value = AppState(userState, VersionOk())
    }

    private fun onUpdateRequiredPositiveClick(url: String) {
        onUpdateRecommendedPositiveClick(url)
    }
}