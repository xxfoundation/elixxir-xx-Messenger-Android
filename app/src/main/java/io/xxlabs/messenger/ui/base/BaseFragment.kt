package io.xxlabs.messenger.ui.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.biometrics.BiometricContainerCallback
import io.xxlabs.messenger.biometrics.BiometricContainerProvider
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.dialog.action.ActionDialog
import io.xxlabs.messenger.support.dialog.action.ActionDialogUI
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialog
import io.xxlabs.messenger.support.dialog.confirm.ConfirmDialogUI
import io.xxlabs.messenger.support.dialog.info.InfoDialog
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.dialog.info.SpanConfig
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialog
import io.xxlabs.messenger.ui.main.chats.TwoButtonInfoDialogUI
import io.xxlabs.messenger.ui.main.qrcode.QrCodeScanFragment
import javax.inject.Inject

abstract class BaseFragment : Fragment(), Injectable {
    @Inject
    lateinit var preferences: PreferencesRepository
    private lateinit var parentActivity: AppCompatActivity
    protected var currentDialog: PopupActionDialog? = null
    protected var isPausing = false
    val biometricContainerProvider: BiometricContainerProvider by lazy {
        BiometricContainerProvider.getInstance(
            this,
            preferences,
            biometricContainerCallback
        )
    }

    private val biometricContainerCallback by lazy {
        object : BiometricContainerCallback {
            override fun onBiometricsNotAvailable() { biometricContainerProvider.showEnableBiometrics() }

            override fun onBiometricFingerprintNotEnrolledDo(isDecryptionMode: Boolean) {
                if (isDecryptionMode) {
                    biometricContainerProvider.resetKey()
                    hideBlurry()
                } else biometricContainerProvider.showEnableBiometrics()
            }

            override fun onFailedDo() { onBiometricFailed() }

            override fun onSuccessDo() {
                hideBlurry()
                onBiometricSuccess()
            }

            override fun onErrorDo() { onBiometricError() }

            override fun onCancelDo() {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!isPausing) showBiometricsLogin()
                    isPausing = false
                }, 300)
                onBiometricCancel()
            }
        }
    }

    open fun onBiometricFailed() {}
    open fun onBiometricSuccess() {}
    open fun onBiometricError() {}
    open fun onBiometricCancel() {}
    open fun onBiometricNotAvailable() {}
    open fun onBiometricNotEnrolled() {}

    fun showBiometricsLogin() {
        if (areBiometricsEnabled()) {
            (parentActivity as MainActivity).showBlurry()
            biometricContainerProvider.authenticateViaFingerprint(true)
        }
    }

    fun hideBiometrics() {
        isPausing = true
        if (areBiometricsEnabled()) {
            biometricContainerProvider.currentBiometricPromptManager?.dismiss(false)
        }
    }

    private fun hideBlurry() {
        (parentActivity as MainActivity).hideBlurry()
    }

    protected fun areBiometricsEnabled(): Boolean {
        return biometricContainerProvider.run {
            isFingerprintEnabled() && isKeySet()
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is AppCompatActivity) {
            parentActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setInsets(
            bottomMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime(),
        )
    }

    fun showError(
        exception: Throwable,
        isBindingError: Boolean = false
    ) {
        val newString = if (isBindingError) {
            bindingsErrorMessage(exception)
        } else {
            exception.localizedMessage
        }
        currentDialog?.dismiss()
        currentDialog = DialogUtils.createErrorPopupDialog(
            requireActivity(),
            Exception(newString),
            preferences.areDebugLogsOn
        )
        currentDialog!!.show()
    }

    fun showError(text: String, isBindingError: Boolean = false) =
        showError(Exception(text), isBindingError)

    protected fun showInfoDialog(
        title: Int,
        body: Int,
        linkTextToUrlMap: Map<String, String>? = null
    ) {
        var spans: MutableList<SpanConfig>? = null
        linkTextToUrlMap?.apply {
            spans = mutableListOf()
            for (entry in keys) {
                val spanConfig = SpanConfig.create(
                    entry,
                    this[entry],
                )
                spans?.add(spanConfig)
            }
        }
        val ui = InfoDialogUI.create(
            title = getString(title),
            body = getString(body),
            spans = spans,
        )
        InfoDialog(ui).show(requireActivity().supportFragmentManager, null)
    }

    protected fun showTwoButtonInfoDialog(
        title: Int,
        body: Int,
        linkTextToUrlMap: Map<String, String>? = null,
        positiveClick: ()-> Unit,
        negativeClick: (()-> Unit)? = null,
        onDismiss: ()-> Unit = { },
    ) {
        var spans: MutableList<SpanConfig>? = null
        linkTextToUrlMap?.apply {
            spans = mutableListOf()
            for (entry in keys) {
                val spanConfig = SpanConfig.create(
                    entry,
                    this[entry],
                )
                spans?.add(spanConfig)
            }
        }
        val infoDialogUI = InfoDialogUI.create(
            title = getString(title),
            body = getString(body),
            spans = spans,
            onDismiss
        )
        val twoButtonUI = TwoButtonInfoDialogUI.create(
            infoDialogUI,
            positiveClick,
            negativeClick
        )
        TwoButtonInfoDialog(twoButtonUI).show(requireActivity().supportFragmentManager, null)
    }

    protected fun showConfirmDialog(
        title: Int,
        body: Int,
        button: Int,
        action: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val ui = ConfirmDialogUI.create(
            infoDialogUI = InfoDialogUI.create(
                title = getString(title),
                body = getString(body),
                null,
                onDismiss
            ),
            buttonText = getString(button),
            buttonOnClick = action
        )
        ConfirmDialog(ui).show(requireActivity().supportFragmentManager, null)
    }

    protected fun showConfirmDialog(
        title: String,
        body: String,
        button: String,
        action: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val ui = ConfirmDialogUI.create(
            infoDialogUI = InfoDialogUI.create(
                title = title,
                body = body,
                null,
                onDismiss
            ),
            buttonText = button,
            buttonOnClick = action
        )
        ConfirmDialog(ui).show(requireActivity().supportFragmentManager, null)
    }

    protected fun showActionDialog(
        title: Int,
        body: Int,
        button: Int,
        action: () -> Unit
    ) {
        val ui = ActionDialogUI.create(
            ConfirmDialogUI.create(
                infoDialogUI = InfoDialogUI.create(
                    title = getString(title),
                    body = getString(body),
                ),
                buttonText = getString(button),
                buttonOnClick = action
            )
        )
        ActionDialog(ui).show(requireActivity().supportFragmentManager, null)
    }

    protected fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", appContext().packageName, null)
        intent.data = uri
        startActivityForResult(intent, QrCodeScanFragment.cameraPermissionRequestCode)
    }

}