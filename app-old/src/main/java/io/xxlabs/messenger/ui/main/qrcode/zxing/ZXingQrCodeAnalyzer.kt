package io.xxlabs.messenger.ui.main.qrcode.zxing

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class ZXingQrCodeAnalyzer(
    private val onQrCodeScanned: (qrCode: Result) -> Unit
) : ImageAnalysis.Analyzer {
    private var isScanning = AtomicBoolean(false)
    private var multiFormatReader: MultiFormatReader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
        )
        setHints(map)
    }

    override fun analyze(image: ImageProxy) {
        try {
            if (isScanning.get()) {
                image.close()
                return
            }

            isScanning.set(true)
            if (image.format !in yuvFormats) {
                Timber.e("QRCodeAnalyze - Expected YUV, now = ${image.format}")
                return
            }

            val data = image.planes[0].buffer.toByteArray()

            val source = PlanarYUVLuminanceSource(
                data,
                image.width,
                image.height,
                0, 0,
                image.width,
                image.height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val rawResult = multiFormatReader.decode(binaryBitmap)
            Timber.d("QR Code: ${rawResult.text}")
            onQrCodeScanned(rawResult)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            multiFormatReader.reset()
            image.close()
            isScanning.set(false)
        }
    }

    companion object {
        private val yuvFormats = mutableListOf(
            ImageFormat.YUV_420_888,
            ImageFormat.YUV_422_888,
            ImageFormat.YUV_444_888
        )
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}