package io.elixxir.feature.splash.model

import io.elixxir.data.version.VersionState

data class AppState(
    val userState: UserState,
    val versionState: VersionState
)