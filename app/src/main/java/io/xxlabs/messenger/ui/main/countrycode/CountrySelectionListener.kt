package io.xxlabs.messenger.ui.main.countrycode

import io.xxlabs.messenger.data.data.Country

interface CountrySelectionListener {
    fun onItemSelected(country: Country)
    val onDismiss: () -> Unit get() = {}
}