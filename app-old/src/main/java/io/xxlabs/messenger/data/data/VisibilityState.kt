package io.xxlabs.messenger.data.data

sealed class VisibilityState {
    object Visible : VisibilityState()
    object Hidden : VisibilityState()
}