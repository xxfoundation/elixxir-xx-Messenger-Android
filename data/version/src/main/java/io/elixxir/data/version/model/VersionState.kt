package io.elixxir.data.version.model

sealed class VersionState

class Checking: VersionState()
class VersionOk : VersionState()
class UpdateRecommended(val updateUrl: String) : VersionState()
class UpdateRequired(val message: String, val updateUrl: String) : VersionState()