package io.xxlabs.messenger.ui.main.qrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_qr_code_show.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class QrCodeShowFragment() : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var qrCodeViewModel: QrCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[QrCodeViewModel::class.java]

        return inflater.inflate(R.layout.fragment_qr_code_show, container, false)
    }

    override fun onStart() {
        super.onStart()
        initComponents()
    }

    fun initComponents() {
        qrCodeViewModel.generateUserQrCode()
        qrCodeViewModel.userQrCode.observe(viewLifecycleOwner) { userQr ->
            when (userQr) {
                is DataRequestState.Start -> {
                    qrCodeShowCode.visibility = View.INVISIBLE
                    qrCodeShowLoading.show()
                }

                is DataRequestState.Error -> {
                    showError("Something has gone wrong while generating your qr code. Please, try again!")
                    qrCodeViewModel.userQrCode.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Success -> {
                    qrCodeShowCode.visibility = View.VISIBLE
                    qrCodeShowCode.setImageBitmap(userQr.data)
                    qrCodeViewModel.userQrCode.postValue(DataRequestState.Completed())
                }

                is DataRequestState.Completed -> {
                    qrCodeShowLoading.hide()
                }
            }
        }

        bindUserInfo()
    }

    private fun bindUserInfo() {
        val userEmail = qrCodeViewModel.getUserEmail()
        val userPhone = qrCodeViewModel.getUserPhone()

        if (userPhone != null && userPhone.isNotEmpty()) {
            qrCodeShowSettingsPhone.text = qrCodeViewModel.getUserPhone()
            qrCodeShowSettingsPhoneSwitcher.isChecked = qrCodeViewModel.shouldShareQrCodePhone()
            qrCodeShowSettingsPhoneSwitcher.setOnCheckedChangeListener { _, isChecked ->
                qrCodeViewModel.setShareQrCodePhone(isChecked)
                qrCodeViewModel.generateUserQrCode()
            }
            showPhone()
        } else {
            hidePhone()
        }

        if (userEmail.isNotBlank()) {
            qrCodeShowSettingsEmail.text = qrCodeViewModel.getUserEmail()
            qrCodeShowSettingsEmailSwitcher.isChecked = qrCodeViewModel.shouldShareQrCodeEmail()
            qrCodeShowSettingsEmailSwitcher.setOnCheckedChangeListener { _, isChecked ->
                qrCodeViewModel.setShareQrCodeEmail(isChecked)
                qrCodeViewModel.generateUserQrCode()
            }

            if ((userPhone != null && userPhone.isNotEmpty())) {
                showEmail(true)
            } else {
                showEmail(false)
            }
        } else {
            hideEmail()
        }

        if (userEmail.isEmpty() && (userPhone == null || userPhone.isEmpty())) {
            qrCodeShowSettings.visibility = View.GONE
        }
    }

    private fun hideEmail() {
        qrCodeShowSettingsEmailIcon.visibility = View.GONE
        qrCodeShowSettingsEmail.visibility = View.GONE
        qrCodeShowSettingsEmailHeader.visibility = View.GONE
        qrCodeShowSettingsEmailSwitcher.visibility = View.GONE
        qrCodeShowSettingsEmailSwitchLine.visibility = View.GONE
    }

    private fun showEmail(showLine: Boolean) {
        qrCodeShowSettingsEmailIcon.visibility = View.VISIBLE
        qrCodeShowSettingsEmail.visibility = View.VISIBLE
        qrCodeShowSettingsEmailHeader.visibility = View.VISIBLE
        qrCodeShowSettingsEmailSwitcher.visibility = View.VISIBLE
        qrCodeShowSettingsEmailSwitchLine.visibility = if (showLine) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun hidePhone() {
        qrCodeShowSettingsPhoneIcon.visibility = View.GONE
        qrCodeShowSettingsPhone.visibility = View.GONE
        qrCodeShowSettingsPhoneSwitcher.visibility = View.GONE
        qrCodeShowSettingsPhoneHeader.visibility = View.GONE
    }

    private fun showPhone() {
        qrCodeShowSettingsPhoneIcon.visibility = View.VISIBLE
        qrCodeShowSettingsPhone.visibility = View.VISIBLE
        qrCodeShowSettingsPhoneSwitcher.visibility = View.VISIBLE
        qrCodeShowSettingsPhoneHeader.visibility = View.VISIBLE
    }
}