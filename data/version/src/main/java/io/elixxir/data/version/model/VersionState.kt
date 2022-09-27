package io.elixxir.data.version.model

sealed class VersionState

object VersionOk : VersionState()
class UpdateRecommended(val updateUrl: String) : VersionState()
class UpdateRequired(val message: String, val updateUrl: String) : VersionState()