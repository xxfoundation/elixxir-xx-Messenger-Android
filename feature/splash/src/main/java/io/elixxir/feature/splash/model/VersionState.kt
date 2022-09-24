package io.elixxir.feature.splash.model

import io.elixxir.feature.splash.ui.VersionAlertUi

sealed class VersionState

class Checking: VersionState()
class VersionOk : VersionState()
class UpdateRecommended(val alertUi: VersionAlertUi) : VersionState()
class UpdateRequired(val alertUi: VersionAlertUi) : VersionState()