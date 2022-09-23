package io.xxlabs.messenger.start.model

import io.xxlabs.messenger.start.ui.VersionAlertUi

sealed class VersionState

class Checking: VersionState()
class VersionOk : VersionState()
class UpdateRecommended(val alertUi: VersionAlertUi) : VersionState()
class UpdateRequired(val alertUi: VersionAlertUi) : VersionState()