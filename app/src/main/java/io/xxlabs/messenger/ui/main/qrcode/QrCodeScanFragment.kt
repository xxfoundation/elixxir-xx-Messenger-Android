package io.xxlabs.messenger.ui.main.qrcode

import android.Manifest
import android.animation.LayoutTransition
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.Result
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.runOnUiThread
import io.xxlabs.messenger.support.singleExecutorInstance
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.qrcode.zxing.ZXingQrCodeAnalyzer
import kotlinx.android.synthetic.main.fragment_qr_code_scan.*
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class QrCodeScanFragment : BaseFragment() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var flashEnabled = false
    private var isCodeScanned = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var qrCodeViewModel: QrCodeViewModel

    private var currAnalysis: ImageAnalysis? = null
    private var currProvider: ProcessCameraProvider? = null

    override fun onDestroy() {
        unbindAnalysis(currAnalysis, currProvider, unbindAnalysis = true, unbindCamera = true)
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(QrCodeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_qr_code_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents(view)
    }

    fun initComponents(root: View) {
        cameraPreviewHolder.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        qrCodeBottomLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        cameraPreviewHolder.layoutTransition.setAnimateParentHierarchy(false)
        qrCodeBottomLayout.layoutTransition.setAnimateParentHierarchy(false)
        qrCodeBottomAnimation.post {
            val marginsParams = qrCodeBottomAnimation.layoutParams as ViewGroup.MarginLayoutParams
            marginsParams.setMargins(
                marginsParams.leftMargin,
                marginsParams.topMargin,
                qrCodeBottomText.width / 10,
                marginsParams.bottomMargin
            )
        }
        startScanning()
        observeScanningResult()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        if (isDetached || isRemoving) {
            return
        }

        cameraProvider?.unbindAll()
        buildCamera(cameraProvider)
    }

    private fun buildCamera(cameraProvider: ProcessCameraProvider?) {
        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(Surface.ROTATION_90)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(singleExecutorInstance(), ZXingQrCodeAnalyzer { result ->
                    if (!qrCodeViewModel.disableScan()) {
                        if (!isCodeScanned) {
                            currAnalysis = analysis
                            currProvider = cameraProvider
                            showResult(analysis, cameraProvider, result)
                        }
                    }
                })
            }
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)

        val camera =
            cameraProvider?.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                imageAnalysis,
                preview
            )

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            cameraFlashControl.visibility = View.VISIBLE

            cameraFlashControl.setOnClickListener {
                camera.cameraControl.enableTorch(!flashEnabled)
            }

            camera.cameraInfo.torchState.observe(viewLifecycleOwner, Observer {
                it?.let { torchState ->
                    if (torchState == TorchState.ON) {
                        flashEnabled = true
                        cameraFlashControl.setImageResource(R.drawable.ic_flash_on)
                    } else {
                        flashEnabled = false
                        cameraFlashControl.setImageResource(R.drawable.ic_flash_off)
                    }
                }
            })
        }

        Timber.v("Sending as ${qrCodeViewModel.getUsername()}")
        cameraUsername.text = qrCodeViewModel.getUsername()
    }

    private fun showResult(
        analysis: ImageAnalysis,
        cameraProvider: ProcessCameraProvider?,
        result: Result
    ) {
        if (result.text.isNullOrEmpty()) {
            showScanError(analysis = analysis, cameraProvider = cameraProvider)
        } else {
            if (!isCodeScanned) {
                isCodeScanned = true
                currAnalysis =  analysis
                currProvider = cameraProvider

                qrCodeViewModel.handleAnalysis(result)
            }
        }
    }

    private fun observeScanningResult() {
        qrCodeViewModel.qrResult.observe(viewLifecycleOwner, { result ->
            when (result) {
                is DataRequestState.Success -> {
                    val pair: Pair<String, ContactData?> = result.data
                    val resultText = pair.first
                    val contact = pair.second
                    if (contact == null) {
                        val bundle = bundleOf("contact" to resultText)
                        showScanSuccess(bundle, currAnalysis, currProvider)
                    } else {
                        showScanError(contact, currAnalysis, currProvider)
                    }

                    qrCodeViewModel.qrResult.value = DataRequestState.Completed()
                }
                is DataRequestState.Error -> {
                    showScanError(
                        analysis = currAnalysis,
                        cameraProvider = currProvider,
                        errorMessage = result.error.localizedMessage
                    )
                    qrCodeViewModel.qrResult.value = DataRequestState.Completed()
                }
                else -> {
                    Timber.v("[QR CODE] Completed")
                }
            }
        })
    }

    private fun unbindAnalysis(
        analysis: ImageAnalysis?,
        cameraProvider: ProcessCameraProvider?,
        unbindAnalysis: Boolean = false,
        unbindCamera: Boolean = false
    ) {
        if (unbindAnalysis) {
            analysis?.clearAnalyzer()
        }
        if (unbindCamera) {
            cameraProvider?.unbindAll()
        }

        isCodeScanned = false
    }

    private fun showScanSuccess(
        bundle: Bundle,
        analysis: ImageAnalysis?,
        cameraProvider: ProcessCameraProvider?
    ) {
        runOnUiThread {
            qrCodeViewModel.setWindowBackgroundColor(R.color.neutral_active)
            qrCodeBottomAnimation?.visibility = View.GONE
            qrCodeBottomText?.text = "Success"
            qrCodeBottomIcon.visibility = View.VISIBLE
            qrCodeBottomIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_check
                )
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            qrCodeViewModel.navigateSuccess(bundle)
            unbindAnalysis(analysis, cameraProvider, unbindAnalysis = true, unbindCamera = true)
        }, 1000)
    }

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraWithScanner()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraWithScanner()
            } else if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                openSettings()
            } else {
                hideCameraScanner()
            }
        }
    }

    private fun openCameraWithScanner() {
        cameraNoPermission.visibility = View.GONE
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
            qrCodeViewModel.background.observe(viewLifecycleOwner, Observer { newColor ->
                if (newColor != null) {
                    cameraBackground.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            newColor
                        )
                    )
                } else {
                    clearErrorStatus()
                }
            })
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun hideCameraScanner() {
        cameraNoPermission.visibility = View.VISIBLE
        cameraNoPermission.setOnClickListener {
            openSettings()
        }
    }

    private fun showScanError(
        contact: ContactData? = null,
        analysis: ImageAnalysis?,
        cameraProvider: ProcessCameraProvider?,
        time: Long = 1500,
        errorMessage: String? = null,
    ) {
        runOnUiThread {
            qrCodeBottomAnimation?.visibility = View.GONE
            qrCodeBottomIcon?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_danger
                )
            )
            qrCodeBottomIcon?.visibility = View.VISIBLE
            qrCodeBottomText?.text = when {
                errorMessage?.contains("You cannot add yourself") == true -> {
                    qrCodeBottomButton.visibility = View.GONE
                    errorMessage
                }
                contact == null -> {
                    qrCodeBottomButton.visibility = View.GONE
                    "Scan Failed"
                }

                contact.status == RequestStatus.ACCEPTED.value -> {
                    qrCodeBottomButton?.text = "Go to Contact"
                    qrCodeBottomButton?.visibility = View.VISIBLE
                    qrCodeBottomButton?.setOnClickListener {
                        val bundle =
                            bundleOf("contact_id" to contact.userId)
                        unbindAnalysis(
                            currAnalysis,
                            currProvider,
                            unbindAnalysis = true,
                            unbindCamera = true
                        )
                        findNavController().navigateSafe(R.id.qr_code_to_contact_details, bundle)
                    }

                    val contactName = contact.displayName
                    val bottomText =
                        SpannableStringBuilder("You have already added\n").append(contactName)
                    bottomText.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.brand_dark
                            )
                        ),
                        bottomText.length - contactName.length,
                        bottomText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    bottomText
                }
                else -> {
                    qrCodeBottomButton?.text = "Check Requests"
                    qrCodeBottomButton?.visibility = View.VISIBLE
                    qrCodeBottomButton?.setOnClickListener {
                        clearErrorStatus()
                        unbindAnalysis(
                            currAnalysis,
                            currProvider,
                            unbindAnalysis = true,
                            unbindCamera = true
                        )
                        findNavController().navigateSafe(R.id.action_global_requests)
                    }
                    "You already have a request open with this contact."
                }
            }
        }
        qrCodeViewModel.setWindowBackgroundColor(R.color.redDarkThemeDark)

        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                unbindAnalysis(analysis, cameraProvider)
            }
        }, time)
    }

    internal fun clearErrorStatus() {
        runOnUiThread {
            qrCodeBottomAnimation?.visibility = View.VISIBLE
            qrCodeBottomText?.text = "Reading QR Code"
            qrCodeBottomIcon.visibility = View.GONE
            qrCodeBottomButton.visibility = View.GONE
        }
        qrCodeViewModel.setWindowBackgroundColor(R.color.neutral_active)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraPermissionRequestCode) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCameraWithScanner()
            }
        }
    }

    companion object {
        const val cameraPermissionRequestCode = 100
    }
}