package io.elixxir.feature.splash.model

import io.elixxir.data.session.model.SessionState
import io.elixxir.data.version.model.VersionState
import io.elixxir.feature.splash.ui.VersionAlertUi

data class AppState(
    val userState: SessionState,
    val versionState: VersionState?,
    val alert: VersionAlertUi?,
)