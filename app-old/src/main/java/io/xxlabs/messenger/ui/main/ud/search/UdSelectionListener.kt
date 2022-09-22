package io.xxlabs.messenger.ui.main.ud.search

import android.view.View
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase

interface UdSelectionListener {
    fun onItemSelected(v: View, contactWrapper: ContactWrapperBase)
}