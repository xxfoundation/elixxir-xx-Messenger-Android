package io.elixxir.core.common

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

fun NotExposedYet(): Nothing = TODO("Not exposed in Bindings yet")

fun newScopeNamed(name: String): CoroutineScope =
    CoroutineScope(
        CoroutineName(name)
                + Job()
                + Dispatchers.Default
    )