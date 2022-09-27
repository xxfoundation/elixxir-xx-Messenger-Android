package io.elixxir.feature.splash.model

import io.elixxir.data.session.model.SessionState
import io.elixxir.data.version.model.VersionState

data class AppState(
    val userState: SessionState,
    val versionState: VersionState?
)