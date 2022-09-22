package io.xxlabs.messenger.ui.main.qrcode.show

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.databinding.FragmentQrCodeShowBinding
import io.xxlabs.messenger.ui.base.BaseProfileRegistrationFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeViewModel
import kotlinx.android.synthetic.main.fragment_qr_code_show.*
import kotlin.math.absoluteValue

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class QrCodeShowFragment : BaseProfileRegistrationFragment(false) {

    lateinit var qrCodeViewModel: QrCodeViewModel
    private lateinit var binding: FragmentQrCodeShowBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeShowBinding.inflate(
            inflater,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[QrCodeViewModel::class.java]

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initComponents()
        detectGestures()
        observeUI()
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
    }

    private fun observeUI() {
        qrCodeViewModel.contactInfo.observe(viewLifecycleOwner) { contactInfo ->
            binding.ui = contactInfo
        }

        qrCodeViewModel.showContactInfoOptions.observe(viewLifecycleOwner) { expand ->
            animateContactInfo(expand)
        }

        qrCodeViewModel.navigateToAddEmail.observe(viewLifecycleOwner) { addEmail ->
            if (addEmail) {
                navigateToAddEmail()
                qrCodeViewModel.onNavigateToAddEmailHandled()
            }
        }

        qrCodeViewModel.navigateToAddPhone.observe(viewLifecycleOwner) { addPhone ->
            if (addPhone) {
                navigateToAddPhone()
                qrCodeViewModel.onNavigateToAddPhoneHandled()
            }
        }

        qrCodeViewModel.onQrCopied.observe(viewLifecycleOwner) { copied ->
            if (copied) {
                showCopiedUI()
            }
        }
    }

    private fun animateContactInfo(expand: Boolean) {
        val view = binding.contactInfoLayout.root
        val peekGuideline = binding.contactInfoPeekGuideline
        val constraintSet = ConstraintSet().apply {
            clone(binding.showQrRoot)
            if (expand) {
                clear(view.id, ConstraintSet.TOP)
                connect(
                    view.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
            } else {
                clear(view.id, ConstraintSet.BOTTOM)
                connect(
                    view.id,
                    ConstraintSet.TOP,
                    peekGuideline.id,
                    ConstraintSet.BOTTOM
                )
            }
        }
        val transition = ChangeBounds().apply {
            interpolator = AnticipateOvershootInterpolator(1.0f)
            duration = 500
        }
        TransitionManager.beginDelayedTransition(binding.showQrRoot, transition)
        constraintSet.applyTo(binding.showQrRoot)
        binding.contactInfoLayout.apply {
            expandButton.visibility = if (expand) View.INVISIBLE else View.VISIBLE
            collapseButton.visibility = if (expand) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun navigateToAddEmail() {
        addEmail()
    }

    private fun navigateToAddPhone() {
        addPhone()
    }

    private fun saveToGallery(bitmap: Bitmap) {

    }

    private fun showCopiedUI() {
        val view = binding.copiedFeedbackText
        val animDurationMs = 2000L
        view.apply {
            alpha = 1.0f
            visibility = View.VISIBLE

            animate()
                .alpha(0f)
                .setDuration(animDurationMs)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                        qrCodeViewModel.onQrCopiedHandled()
                    }
                })
        }
    }

    private  var dY = 0F
    private fun detectGestures() {
        binding.contactInfoLayout.contactInfoRoot.apply {
            setOnTouchListener { view, motionEvent ->
                view.performClick()
                when (motionEvent.action) {
                    ACTION_DOWN -> {
                        dY = motionEvent.rawY
                        true
                    }
                    ACTION_MOVE -> {
                        animateSwipe(dY - motionEvent.rawY)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun animateSwipe(movement: Float) {
        if (movement.absoluteValue > 100) animateContactInfo(movement > 0)
    }

    override fun onUdLoaded() {}
    override fun onShowDialog(dialogFragment: DialogFragment) {}
    override fun onRemoveFact(factToDelete: FactType) {}
    override fun onSkipEmail() {}
    override fun onSkipPhone() {}
    override fun changeContactPhoto(photo: Bitmap) {}
    override fun onImageNotSelectedOrRevoked() {}
    override fun initComponents(root: View) {}

    override fun onEnterCode() {
        currentInputPopupDialog?.btnBack?.visibility = View.VISIBLE
        currentInputPopupDialog?.btnBack?.setOnClickListener {
            if (isCurrentDialogEmail) {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createEmailDialog()
            } else {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createPhoneDialog()
            }
        }
    }

    override fun onEmailSuccess() {
        dismissDialogAfterSuccess()
        qrCodeViewModel.onContactInfoChanged()
    }

    override fun onPhoneSuccess() {
        dismissDialogAfterSuccess()
        qrCodeViewModel.onContactInfoChanged()
    }
}