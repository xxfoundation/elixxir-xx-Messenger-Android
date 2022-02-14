package io.xxlabs.messenger.support.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import io.xxlabs.messenger.support.appContext

object BitmapResolver {
    private const val TAG = "BitmapResolver"

    fun getBitmap(fileUri: Uri): Bitmap? {
        return  BitmapFactory.decodeStream(appContext().contentResolver.openInputStream(fileUri))
    }

    fun getBitmap(array: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(array, 0, array.size)
    }

    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    fun getResizedBitmap(image: Bitmap, maxSize: Int = image.width + image.height): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}