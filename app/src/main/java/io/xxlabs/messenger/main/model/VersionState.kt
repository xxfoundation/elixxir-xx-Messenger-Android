package io.xxlabs.messenger.main.model

import io.xxlabs.messenger.main.ui.VersionAlertUi

sealed class VersionState

class Checking: VersionState()
class VersionOk : VersionState()
class UpdateRecommended(val alertUi: VersionAlertUi) : VersionState()
class UpdateRequired(val alertUi: VersionAlertUi) : VersionState()