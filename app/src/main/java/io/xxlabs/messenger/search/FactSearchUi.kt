package io.xxlabs.messenger.search

import androidx.lifecycle.LiveData

interface FactSearchUi {
    val countryCode: LiveData<String?>
    val searchHint: String
    fun onCountryClicked()
}