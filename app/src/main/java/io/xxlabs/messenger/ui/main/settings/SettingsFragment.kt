package io.xxlabs.messenger.ui.main.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.biometrics.BiometricUtils
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.support.dialog.BottomSheetPopup
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.MainViewModel
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var loadingProgress: LooperCircularProgressBar

    override fun onBiometricNotAvailable() {
        biometricContainerProvider.showEnableBiometrics()
    }

    override fun onBiometricFailed() {
        if (!areBiometricsEnabled()) {
            settingsViewModel.enableBiometrics(false)
        }
    }

    override fun onBiometricSuccess() {
        if (!areBiometricsEnabled()) {
            settingsViewModel.enableBiometrics(true)
        }
    }

    override fun onBiometricError() {
        if (!areBiometricsEnabled()) {
            settingsViewModel.enableBiometrics(false)
        }
    }

    override fun onBiometricCancel() {
        if (!areBiometricsEnabled()) {
            settingsViewModel.enableBiometrics(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProvider(this, viewModelFactory).get(SettingsViewModel::class.java)

        mainViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            ).get(MainViewModel::class.java)

        loadingProgress = LooperCircularProgressBar(requireContext(), false)

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showFirstTimeSettings()
        initComponents(view)
        bindPredictiveText()
        bindEnterToSend()
        bindUrlPreview()
        bindHideApp()
        bindPushNotificationsSwitcher()
        bindInAppNotificationsSwitcher()
        bindCoverTrafficSwitcher()
        bindBiometricsSwitcher()
        bindDeleteAccount()
    }

    private fun showFirstTimeSettings() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeSettings) {
            showInfoDialog(
                title = R.string.settings_account_recovery_dialog_title,
                body = R.string.settings_account_recovery_dialog_body,
                null
            )
            preferences.isFirstTimeSettings = false
        }
    }

    fun initComponents(root: View) {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericTitle.text = "Settings"

        toolbarGenericBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        settingsPushTitle.setOnClickListener {
            showInfoDialog(
                R.string.settings_notification_info_dialog_title,
                R.string.settings_notification_info_dialog_body,
                mapOf(
                    getString(R.string.settings_notification_info_dialog_link_text)
                            to getString(R.string.settings_notification_info_dialog_link_url)
                )
            )
        }

        settingsCoverTrafficTitle.setOnClickListener {
            showInfoDialog(
                R.string.settings_cover_traffic_title,
                R.string.settings_cover_traffic_dialog_body,
                mapOf(
                    getString(R.string.settings_cover_traffic_link_text)
                            to getString(R.string.settings_cover_traffic_link_url)
                )
            )
        }

        settingsPredictiveTextTitle.setOnClickListener {
            showInfoDialog(
                R.string.settings_predictive_text_info_dialog_title,
                R.string.settings_predictive_text_info_dialog_body
            )
        }

        settingsBiometricsTitle.setOnClickListener {
            showInfoDialog(
                R.string.settings_biometric_info_dialog_title,
                R.string.settings_biometric_info_dialog_body,
            )
        }

        settingsDisclosuresLayout.setOnClickListener {
            DialogUtils.getWebPopup(
                requireContext(),
                getString(R.string.settings_disclosures),
                "https://xx.network/privategrity-corporation-terms-of-use"
            ).show()
        }

        settingsPrivacyLayout.setOnClickListener {
            DialogUtils.getWebPopup(
                requireContext(),
                getString(R.string.settings_privacy_policy),
                "https://xx.network/privategrity-corporation-privacy-policy"
            ).show()
        }

        settingsTermsOfUseLayout.setOnClickListener {
            DialogUtils.getWebPopup(
                requireContext(),
                "Terms of Use",
                "https://xx.network/terms-of-use"
            ).show()
        }

        settingsAdvanced.setOnClickListener {
            findNavController().navigateSafe(R.id.action_settings_to_settings_advanced)
        }
    }

    private fun bindPredictiveText() {
        settingsPredictiveTextSwitch.isChecked = !preferences.isIncognitoKeyboardEnabled
        settingsPredictiveTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.isIncognitoKeyboardEnabled = !isChecked
        }
    }

    private fun bindEnterToSend() {
        settingsEnterToSendSwitch.isChecked = preferences.isEnterToSendEnabled
        settingsEnterToSendSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.isEnterToSendEnabled = isChecked
            setEnterToSendTransparency()
        }
        setEnterToSendTransparency()
    }

    private fun setEnterToSendTransparency() {
        settingsEnterToSendTitle.isEnabled = settingsEnterToSendSwitch.isChecked
    }

    private fun bindUrlPreview() {
        settingsUrlPreviewSwitch.isChecked = false
        settingsUrlPreviewSwitch.setOnCheckedChangeListener { _, isChecked ->
            setUrlPreviewTransparency()
        }
        setUrlPreviewTransparency()
    }

    private fun setUrlPreviewTransparency() {
        settingsUrlPreviewIcon.isEnabled = settingsUrlPreviewSwitch.isChecked
        settingsUrlPreviewTitle.isEnabled = settingsUrlPreviewSwitch.isChecked
    }

    private fun bindHideApp() {
        settingsHideAppSwitch.isChecked = preferences.isHideAppEnabled
        settingsHideAppSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.isHideAppEnabled = isChecked
            if (isChecked) {
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            setHideAppTransparency()
        }
        setHideAppTransparency()
    }

    private fun setHideAppTransparency() {
        if (settingsHideAppSwitch.isChecked) {
            settingsHideAppIcon.isEnabled = true
            settingsHideAppTitle.isEnabled = true
        } else {
            settingsHideAppIcon.isEnabled = false
            settingsHideAppTitle.isEnabled = false
        }
    }

    private fun bindPushNotificationsSwitcher() {
        settingsPushSwitch.isChecked = settingsViewModel.arePushNotificationsOn()
        val checkedListener = CompoundButton.OnCheckedChangeListener { switch, isChecked ->
            settingsViewModel.enablePushNotifications(isChecked)
        }

        settingsViewModel.enableNotifications.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is DataRequestState.Start -> {
                    loadingProgress.show()
                    if (settingsPushSwitch.isChecked) {
                        loadingProgress.setMsg("Enabling push notifications...")
                    } else {
                        loadingProgress.setMsg("Disabling push notifications...")
                    }
                }

                is DataRequestState.Success -> {
                    settingsPushSwitch.isChecked = result.data
                    settingsViewModel.enableNotifications.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Error -> {
                    settingsPushSwitch.isChecked = settingsViewModel.arePushNotificationsOn()
                    showError(result.error, isBindingError = true)
                    settingsViewModel.enableNotifications.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Completed -> {
                    loadingProgress.dismiss()
                    settingsPushSwitch.setOnCheckedChangeListener(checkedListener)
                }
            }
        })

        setPushTransparency()
        settingsPushSwitch.setOnCheckedChangeListener(checkedListener)
    }

    private fun setPushTransparency() {
        if (settingsPushSwitch.isChecked) {
            settingsPushIcon.isEnabled = true
            settingsPushTitle.isEnabled = true
        } else {
            settingsPushIcon.isEnabled = false
            settingsPushTitle.isEnabled = false
        }
    }

    private fun bindInAppNotificationsSwitcher() {
        settingsInAppNotificationsSwitch.isChecked = settingsViewModel.areInAppNotificationsOn()
        val checkedListener = CompoundButton.OnCheckedChangeListener { switch, isChecked ->
            settingsViewModel.enableInAppNotifications(isChecked)
        }
        settingsInAppNotificationsSwitch.setOnCheckedChangeListener(checkedListener)
    }

    private fun bindCoverTrafficSwitcher() {
        settingsCoverTrafficSwitch.isChecked = settingsViewModel.isCoverTrafficOn()
        val checkedListener = CompoundButton.OnCheckedChangeListener { switch, isChecked ->
            settingsViewModel.enableCoverTraffic(isChecked)
        }

        settingsCoverTrafficSwitch.setOnCheckedChangeListener(checkedListener)
    }

    private fun bindBiometricsSwitcher() {
        if (BiometricUtils.areBiometricsAvailable(requireContext())) {
            settingsBiometricSwitch.isChecked = areBiometricsEnabled()

            val biometricsNotificationSwitcher =
                CompoundButton.OnCheckedChangeListener { switcher, isEnabled ->
                    switchBiometrics(isEnabled)
                }

            settingsBiometricSwitch.setOnCheckedChangeListener(biometricsNotificationSwitcher)

            settingsViewModel.enableBiometrics.observe(viewLifecycleOwner, { result ->
                if (result is SimpleRequestState.Success) {
                    settingsBiometricSwitch.isChecked = result.value
                    settingsBiometricSwitch.setOnCheckedChangeListener(
                        biometricsNotificationSwitcher
                    )
                    settingsViewModel.enableBiometrics.value = SimpleRequestState.Completed()
                }
            })
        } else {
            settingsBiometricSwitch.isChecked = false
            settingsBiometricSwitch.isEnabled = false
        }
    }

    private fun switchBiometrics(isChecked: Boolean) {
        if (isChecked) {
            biometricContainerProvider.openBiometricsSetup(
                true,
                primaryBtnCallback = {
                    biometricContainerProvider.checkFingerprintSetup()
                },
                secondaryBtnCallback = { settingsViewModel.enableBiometrics(false) },
                onCancel = { settingsViewModel.enableBiometrics(false) }
            )
        } else {
            biometricContainerProvider.resetKey()
            settingsViewModel.enableBiometrics(false)
        }
    }

    private fun bindBackupProfile() {
//        settingsBackupProfile.setOnClickListener {
//            FileUtils.checkPermissionDo(
//                this,
//                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
//            ) { onWriteGranted() }
//        }
    }

    private fun backupProfile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        intent.putExtra("android.content.extra.FANCY", true)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        startActivityForResult(
            Intent.createChooser(intent, "Choose a directory"),
            REQUEST_CODE_SELECT_FOLDER
        )
    }

    private fun navigateToDeleteAccount() {
        val deleteAccountDirections = SettingsFragmentDirections
            .actionSettingsFragmentToDeleteAccountFragment()
        findNavController().navigate(deleteAccountDirections)
    }

    private fun bindDeleteAccount() {
        settingsDeleteAccount.setOnSingleClickListener {
            navigateToDeleteAccount()
        }

        settingsViewModel.deleteUser.observe(viewLifecycleOwner, { result ->
            when (result) {
                is DataRequestState.Start -> {
                    loadingProgress.show()
                    loadingProgress.setMsg("Deleting user...")
                }

                is DataRequestState.Success -> {
                    loadingProgress.dismiss()
                    onDeleteSuccess()
                }

                is DataRequestState.Error -> {
                    showError(result.error)
                    settingsViewModel.deleteUser.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Completed -> {
                    loadingProgress.dismiss()
                }
            }
        })
    }

    private fun onDeleteSuccess() {
        Handler(Looper.getMainLooper()).postDelayed({
            context?.apply {
                val packageManager = packageManager
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
        }, 500)
    }

    private fun onWriteGranted() {
        backupProfile()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                onWriteGranted()
            } else {
                showError("This permission is required in order to activate this feature.")
            }
            return
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 100
        private const val REQUEST_CODE_SELECT_FOLDER = 200
    }
}