package io.xxlabs.messenger.ui.main.qrcode.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.common.util.concurrent.ListenableFuture
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentQrCodeScanBinding
import io.xxlabs.messenger.support.singleExecutorInstance
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.qrcode.QrCodeViewModel
import io.xxlabs.messenger.ui.main.qrcode.zxing.ZXingQrCodeAnalyzer
import kotlinx.android.synthetic.main.fragment_qr_code_scan.*
import javax.inject.Inject
import com.google.zxing.Result as QrScanResult

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class QrCodeScanFragment : BaseFragment() {

    private lateinit var binding: FragmentQrCodeScanBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var qrCodeViewModel: QrCodeViewModel

    private var currAnalysis: ImageAnalysis? = null
    private var currProvider: ProcessCameraProvider? = null
    private var flashEnabled = false

    private val hasCamera: Boolean
        get() = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeScanBinding.inflate(
            inflater,
            container,
            false
        )

        qrCodeViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(QrCodeViewModel::class.java)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        observeUI()
        requireCamera()
    }

    private fun observeUI() {
        qrCodeViewModel.scanQrCodeState.observe(viewLifecycleOwner) { ui ->
            binding.ui = ui
        }

        qrCodeViewModel.startScanner.observe(viewLifecycleOwner) { start ->
            if (start) {
                startQrCodeScan()
                qrCodeViewModel.onScannerStarted()
            }
        }
    }

    private fun requireCamera() {
        if (hasCamera) checkCameraPermission()
        else noCameraFound()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            qrCodeViewModel.onCameraPermissionResult(true)
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionRequestCode
            )
        }
    }

    private fun noCameraFound() {
        showError("A camera is required to use this feature.")
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
                analysis.setAnalyzer(singleExecutorInstance(), ZXingQrCodeAnalyzer { qrResult ->
                    if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                        currAnalysis = analysis
                        currProvider = cameraProvider
                        showResult(analysis, cameraProvider, qrResult)
                    }
                })
            }
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)

        val camera = cameraProvider?.bindToLifecycle(
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
    }

    private fun showResult(
        analysis: ImageAnalysis,
        cameraProvider: ProcessCameraProvider?,
        scanResult: QrScanResult
    ) {
        if (scanResult.text.isNullOrEmpty()) return

        currAnalysis =  analysis
        currProvider = cameraProvider
        qrCodeViewModel.parseData(scanResult)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode && grantResults.isNotEmpty()) {
            qrCodeViewModel.onCameraPermissionResult(
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraPermissionRequestCode) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startQrCodeScan()
            }
        }
    }

    private fun startQrCodeScan() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            startCamera(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startCamera(cameraProvider: ProcessCameraProvider?) {
        if (isDetached || isRemoving) return

        cameraProvider?.unbindAll()
        buildCamera(cameraProvider)
    }

    override fun onDestroy() {
        tearDown()
        super.onDestroy()
    }

    private fun tearDown() {
        currAnalysis?.clearAnalyzer()
        currProvider?.unbindAll()
    }

    companion object {
        const val cameraPermissionRequestCode = 100
    }
}