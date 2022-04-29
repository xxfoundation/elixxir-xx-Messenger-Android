package io.xxlabs.messenger.ui.main.ud.search

import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.dialog.info.showInfoDialog
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainViewModel
import io.xxlabs.messenger.ui.main.countrycode.CountryFullscreenDialog
import io.xxlabs.messenger.ui.main.countrycode.CountrySelectionListener
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_private_search.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class UdSearchFragment : BaseFragment() {
    private lateinit var phoneDialog: CountryFullscreenDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel
    lateinit var udSearchViewModel: UdSearchViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var navController: NavController
    private lateinit var resultsAdapter: UdResultAdapter
    private lateinit var snackBar: Snackbar

    private var isUdInitialized = false
    private var currentAddButton: View? = null
    private var lastClickTime: Long = 0
    private var currentFilter: UdSearchFilter = UdSearchFilter.USERNAME
    private var phoneCurrentDialCode: String? = null
    private var phoneCurrentCountryCode: String? = null

    private var udSelectionListener = object : UdSelectionListener {
        override fun onItemSelected(v: View, contactWrapper: ContactWrapperBase) {
            v.disableWithAlpha()
            currentAddButton = v
            val contactString = contactWrapper.marshal().toString(Charsets.ISO_8859_1)
            Timber.v("Marshalled String: $contactString")
            val bundle =
                bundleOf("contact" to contactString)
            navController.navigateSafe(R.id.action_ud_search_to_contact_success, bundle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_private_search, container, false)
    }

    override fun onDetach() {
        snackBar.dismiss()
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        mainViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)

        networkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(NetworkViewModel::class.java)

        contactsViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(ContactsViewModel::class.java)

        udSearchViewModel = ViewModelProvider(this, viewModelFactory)
            .get(UdSearchViewModel::class.java)

        initComponents(view)
        showNewUserPopups()
        watchForChanges()
    }

    fun initComponents(root: View) {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericActionText.visibility = View.VISIBLE
        toolbarGenericTitle.text = "Search"

        snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Waiting for Ud...",
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.view.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars())
//        udSearchViewModel.searchTest()

        clearResultsMsg()
        createPhoneFullscreenDialog()
        bindListeners()
        addResultsRecyclerView()

        if (resultsAdapter.itemCount > 0) {
            hideInitialText()
        }

        udSearchInput.incognito(preferences.isIncognitoKeyboardEnabled)
    }

    private fun clearResultsMsg() {
        udSearchLoading.hide()
        udSearchResultsTitle.visibility = View.GONE
        udSearchInputEmptyMessage.visibility = View.GONE
        toolbarGenericActionText.disableWithAlpha()
    }

    private fun bindListeners() {
        udSearchInput.doAfterTextChanged {
            if (isPhoneFilter()) {
                if (phoneCurrentCountryCode == null || phoneCurrentDialCode == null || it.isNullOrBlank()) {
                    toolbarGenericActionText.disableWithAlpha()
                } else {
                    toolbarGenericActionText.enable()
                }
            } else {
                if (it.isNullOrBlank()) {
                    toolbarGenericActionText.disableWithAlpha()
                } else {
                    toolbarGenericActionText.enable()
                }
            }
        }

        udSearchInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (udSearchInput.text.toString().isValidQuery()) {
                    try {
                        search()
                    } catch (e: Exception) {
                        showError(e, true)
                    }
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        toolbarGenericBackBtn.setOnClickListener {
            navController.navigateUp()
        }

        udSearchUsernameLayout.setOnClickListener {
            changeFilter()
        }

        udSearchEmailLayout.setOnClickListener {
            changeFilter(UdSearchFilter.EMAIL)
        }

        udSearchPhoneLayout.setOnClickListener {
            changeFilter(UdSearchFilter.PHONE)
        }

        udSearchInputPhoneCode.setOnClickListener {
            if (!phoneDialog.isAdded) {
                phoneDialog.show(childFragmentManager, "emailRegistrationDialog")
            }
        }

        udSearchInputMiddleMessage.setOnClickListener {
            showInfoDialog(
                R.string.search_info_dialog_title,
                R.string.search_info_dialog_body,
                mapOf(
                    getString(R.string.search_info_dialog_link_text)
                            to getString(R.string.search_info_dialog_link_url)
                )
            )
        }
    }

    private fun String.isValidQuery(): Boolean {
        if (isNullOrBlank()) return false

        return !preferences.userData.contains(this, true)
    }

    private fun search() {
        if (hasConnection()) {
            if (SystemClock.elapsedRealtime() - lastClickTime >= 1000) {
                lastClickTime = SystemClock.elapsedRealtime()
                val input = udSearchInput.text.toString().trim()
                if (currentFilter != UdSearchFilter.PHONE) {
                    clearResults()
                    disableSearch()
                    hideInitialText()
                    udSearchViewModel.searchUd(input)
                } else {
                    if (phoneCurrentCountryCode == null) {
                        showError("Country code was not selected.")
                    } else {
                        clearResults()
                        disableSearch()
                        hideInitialText()
                        val newInput = "$input$phoneCurrentCountryCode"
                        Timber.v("Phone input: $newInput")
                        udSearchViewModel.searchUd(newInput)
                    }
                }
            }
        } else {
            if (!networkViewModel.hasConnection()) {
                showError("Phone has no connection to the network!")
            } else {
                showError("User discovery is not initialized yet.")
            }
        }
    }

    private fun hideInitialText() {
        udSearchInputEmptyMessage.visibility = View.GONE
        udSearchInputMiddleMessage.visibility = View.GONE
    }

    private fun setHint() {
        if (isPhoneFilter()) {
            udSearchInput.hint = "Phone Number"
        } else {
            udSearchInput.hint = "Search"
        }
    }

    private fun isPhoneFilter() = currentFilter == UdSearchFilter.PHONE

    private fun createPhoneFullscreenDialog() {
        setDefaultCountry()

        phoneDialog = CountryFullscreenDialog.getInstance(object : CountrySelectionListener {
            override fun onItemSelected(country: Country) {
                val countryString = country.flag + country.dialCode
                phoneCurrentCountryCode = country.countryCode
                phoneCurrentDialCode = country.dialCode
                phoneDialog.dismiss()
                udSearchInputPhoneCode.setText(countryString)
            }
        })
    }

    private fun setDefaultCountry() {
        val usa = Country.getDefaultCountry()
        phoneCurrentCountryCode = usa.countryCode
        phoneCurrentDialCode = usa.dialCode

        val countryString = usa.flag + usa.dialCode
        udSearchInputPhoneCode.setText(countryString)
    }

    private fun hasConnection() =
        networkViewModel.isUserDiscoveryRunning() && networkViewModel.hasConnection()

    private fun clearResults() {
        udSearchResultsTitle.visibility = View.GONE
        resultsAdapter.clearResult()
    }

    private fun enableSearch() {
        udSearchLoading.hide()
        toolbarGenericActionText.enable()
        udSearchPhoneLayout.isClickable = true
        udSearchUsernameLayout.isClickable = true
        udSearchEmailLayout.isClickable = true
        udSearchInput.enable()
    }

    private fun disableSearch() {
        udSearchLoading.show()
        toolbarGenericActionText.disableWithAlpha()
        udSearchPhoneLayout.isClickable = false
        udSearchUsernameLayout.isClickable = false
        udSearchEmailLayout.isClickable = false
        udSearchInput.disableWithAlpha()
    }

    private fun changeFilter(filter: UdSearchFilter = UdSearchFilter.USERNAME) {
        udSearchViewModel.changeSearchFilter(filter)
    }

    private fun addResultsRecyclerView() {
        resultsAdapter = UdResultAdapter(udSelectionListener)
        val layoutManager = LinearLayoutManager(requireContext())

        udSearchResultsRecyclerView.layoutManager = layoutManager
        udSearchResultsRecyclerView.adapter = resultsAdapter
    }

    private fun showNewUserPopups() {
        showNotificationDialog()
    }

    private fun showNotificationDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeNotifications) {
            showTwoButtonInfoDialog(
                title = R.string.settings_push_notifications_dialog_title,
                body = R.string.settings_push_notifications_dialog_body,
                linkTextToUrlMap = null,
                positiveClick = ::enablePushNotifications,
                negativeClick = null,
                onDismiss = ::showCoverMessageDialog
            )
            preferences.isFirstTimeNotifications = false
        }
    }

    private fun enablePushNotifications() {
        mainViewModel.enableNotifications { error ->
            error?.let { showError(error) }
        }
    }

    private fun showCoverMessageDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeCoverMessages) {
            showTwoButtonInfoDialog(
                R.string.settings_cover_traffic_title,
                R.string.settings_cover_traffic_dialog_body,
                mapOf(
                    getString(R.string.settings_cover_traffic_link_text)
                            to getString(R.string.settings_cover_traffic_link_url)
                ),
                ::enableCoverMessages,
                ::declineCoverMessages,
            )
            preferences.isFirstTimeCoverMessages = false
        }
    }

    private fun enableCoverMessages() {
        enableDummyTraffic(true)
    }

    private fun declineCoverMessages() {
        enableDummyTraffic(false)
    }

    private fun enableDummyTraffic(enabled: Boolean) {
        try {
            mainViewModel.enableDummyTraffic(enabled)
        } catch (e: Exception) {
            showError(e, true)
        }
    }

    private fun watchForChanges() {
        networkViewModel.networkState.observe(viewLifecycleOwner, { networkState ->
            if (networkState != NetworkState.HAS_CONNECTION) {
                snackBar.show()
            } else {
                snackBar.dismiss()
            }
        })

        networkViewModel.userDiscoveryStatus.observe(
            viewLifecycleOwner,
            { isUdCompletelyInitialized ->
                isUdInitialized = isUdCompletelyInitialized
                if (!isUdCompletelyInitialized) {
                    showError("Error to initialize User Discovery, try again when the network is healthy.")
                }
            })

        contactsViewModel.newAuthRequestSent.observe(viewLifecycleOwner, { result ->
            when (result) {
                is SimpleRequestState.Success -> {
                    contactsViewModel.newAuthRequestSent.postValue(SimpleRequestState.Completed())
                }
                is SimpleRequestState.Error -> {
                    if (result.error != null) {
                        showError(result.error, isBindingError = true)
                    }
                    currentAddButton?.enable()
                }
                else -> {
                    Timber.v("Completed new auth request")
                }
            }
        })

        udSearchViewModel.currentSearchFilter.observe(viewLifecycleOwner, { udFilter ->
            currentFilter = udFilter
            setHint()
            when (udFilter) {
                UdSearchFilter.USERNAME -> {
                    udSearchInput.inputType = (InputType.TYPE_CLASS_TEXT)
                    udSearchInput.setTextAppearance(R.style.InputEditText)
                    udSearchInput.incognito(preferences.isIncognitoKeyboardEnabled)
                    selectBackground(udSearchUsernameLayout)
                    deselectBackground(udSearchEmailLayout)
                    deselectBackground(udSearchPhoneLayout)
                    hidePhone()
                }

                UdSearchFilter.EMAIL -> {
                    udSearchInput.inputType =
                        (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    udSearchInput.setTextAppearance(R.style.InputEditText)
                    udSearchInput.incognito(preferences.isIncognitoKeyboardEnabled)
                    deselectBackground(udSearchUsernameLayout)
                    selectBackground(udSearchEmailLayout)
                    deselectBackground(udSearchPhoneLayout)
                    hidePhone()
                }

                UdSearchFilter.PHONE -> {
                    udSearchInput.inputType =
                        (InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_NUMBER)
                    udSearchInput.setTextAppearance(R.style.InputEditText)
                    udSearchInput.incognito(preferences.isIncognitoKeyboardEnabled)
                    deselectBackground(udSearchUsernameLayout)
                    deselectBackground(udSearchEmailLayout)
                    selectBackground(udSearchPhoneLayout)
                    activatePhone()
                }

                else -> {
                }
            }
        })

        udSearchViewModel.contactResult.observe(viewLifecycleOwner, { result ->
            Timber.v("Result from UI")
            enableSearch()
            if (result.hasError()) {
                handleError(result.error)
            } else {
                handleSuccess(result.contactWrapper)
            }
        })

        udSearchViewModel.searchState.observe(viewLifecycleOwner, { result ->
            if (!isPhoneFilter()) {
                phoneCurrentDialCode = null
                phoneCurrentCountryCode = null
                udSearchInputPhoneCode.text = null
            }
            when (result) {
                is DataRequestState.Error -> {
                    udSearchViewModel.searchState.postValue(DataRequestState.Completed())
                    showError(result.error, isBindingError = true)
                }

                is DataRequestState.Success -> {
                    udSearchViewModel.searchState.postValue(DataRequestState.Completed())
                    contactsViewModel.updateAndRequestAuthChannel(result.data)
                    findNavController().navigateSafe(R.id.action_contact_success_done)
                }
                else -> {
                }
            }
        })
    }

    private fun hidePhone() {
        udSearchInputPhoneCode.visibility = View.GONE
    }

    private fun activatePhone() {
        udSearchInputPhoneCode.visibility = View.VISIBLE
    }

    private fun handleSuccess(contactWrapper: ContactWrapperBase?) {
        if (contactWrapper == null) {
            handleEmptyResult()
        } else {
            handleNonEmptyResult(contactWrapper)
        }
    }

    private fun handleEmptyResult() {
        udSearchInputEmptyMessage.visibility = View.VISIBLE
        udSearchInputEmptyMessage.text =
            getSearchErrorText()
        resultsAdapter.clearResult()
    }

    private fun handleNonEmptyResult(contactWrapper: ContactWrapperBase) {
        Timber.v("Facts from user: ${contactWrapper.getStringifiedFacts()}")
        udSearchResultsTitle.visibility = View.VISIBLE
        hideInitialText()
        resultsAdapter.setResult(contactWrapper)
    }

    private fun handleError(error: String) {
        udSearchResultsTitle.visibility = View.GONE
        udSearchInputEmptyMessage.visibility = View.VISIBLE
        udSearchInputEmptyMessage.text = when {
            error.contains("NO RESULTS FOUND") -> {
                getSearchErrorText()
            }
            error.contains("gitlab") -> {
                error.substringBefore("gitlab")
            }
            else -> {
                error
            }
        }
        showError(error, true)
    }

    private fun getSearchErrorText() =
        "There are no users with that ${udSearchViewModel.getCurrentFilter().value.lowercase(Locale.getDefault())}."

    fun selectBackground(viewGroup: ViewGroup) {
        viewGroup.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.btn_color_dark_small)
        viewGroup.children.forEach { child ->
            if (child is TextView) {
                child.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.neutral_white
                    )
                )
            } else if (child is ImageView) {
                child.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.neutral_white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun deselectBackground(viewGroup: ViewGroup) {
        viewGroup.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.btn_color_outlined_dark_small)
        viewGroup.children.forEach { child ->
            if (child is TextView) {
                child.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.neutral_active
                    )
                )
            } else if (child is ImageView) {
                child.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.neutral_active),
                    android.graphics.PorterDuff.Mode.MULTIPLY
                )
            }
        }
    }
}
