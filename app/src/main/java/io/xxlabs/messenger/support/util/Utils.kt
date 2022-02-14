package io.xxlabs.messenger.support.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spanned
import android.text.format.DateUtils
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.text.HtmlCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import io.reactivex.Single
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.data.datatype.AuthState
import io.xxlabs.messenger.support.extensions.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Utils {
    companion object {
        /***
         * Date Related
         */
        fun isToday(date: Calendar): Boolean {
            val currDate = Calendar.getInstance()
            return date.get(Calendar.DAY_OF_YEAR) == currDate.get(Calendar.DAY_OF_YEAR) &&
                    date.get(Calendar.YEAR) == currDate.get(Calendar.YEAR)
        }

        fun isSameDay(firstDate: Calendar, secondDate: Calendar): Boolean {
            return firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR) &&
                    firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR)
        }

        fun getCurrentDate(): String {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat("MM/dd/yy", Locale.ENGLISH)
            return formatter.format(calendar.time)
        }

        fun getDateByTimestamp(timestamp: Long): String {
            val calendar = Calendar.getInstance()
            calendar.time = Date(timestamp)
            val formatter = SimpleDateFormat("MM/dd/yy", Locale.ENGLISH)
            return formatter.format(calendar.time)
        }

        /***
         * Get the current screen height
         */
        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }

        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }

        /***
         *  Converts pixels to density independent pixel
         */
        fun pxToDp(px: Int): Int {
            return (px / XxMessengerApplication.appResources.displayMetrics.density).toInt()
        }

        /***
         *  Converts density independent pixel to pixels
         */
        fun dpToPx(dp: Int): Int {
            return (dp * XxMessengerApplication.appResources.displayMetrics.density).toInt()
        }

        /***
         *  Converts a drawable to a bitmap
         */
        fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

        fun getBitmapFromView(view: View): Bitmap? {
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        fun getBitmapFromView(view: View, defaultColor: Int): Bitmap? {
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(defaultColor)
            view.draw(canvas)
            return bitmap
        }

        /***
         * Tells whether an app is installed or not
         * @param packageName App bundle id
         * @param packageManager Android Package Manager
         */
        fun isPackageInstalled(packageName: String, packageManager: PackageManager?): Boolean {
            if (packageManager == null) return false

            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (err: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun getCurrentTimeStamp(): Long {
            return System.currentTimeMillis()
        }

        fun getCurrentTimeStampNano(): Long {
            return TimeUnit.NANOSECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        }

        fun openLink(context: Context, url: String) {
            val webIntent = Intent(Intent.ACTION_VIEW)
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                webIntent.data = Uri.parse(url)
                val title = "Choose your browser"
                val chooser = Intent.createChooser(webIntent, title)
                context.startActivity(chooser)
            } catch (e: Exception) {
                context.toast("You have no browser apps available.")
                Timber.e(e, "Error on loading link $e")
            }
        }

        fun calculateGetTimestampString(msgTimestamp: Long, format: String = "HH:mm"): String {
            return timestampMillisToHourMin(msgTimestamp, format)
        }

        private fun timestampNanoToHourMin(timestamp: Long, format: String): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = TimeUnit.NANOSECONDS.toMillis(timestamp)
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            return (sdf.format(calendar.time))
        }

        private fun timestampMillisToHourMin(timestamp: Long, format: String): String {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            return (sdf.format(timestamp))
        }

        fun getAuthErrCode(err: String): AuthState {
            return when {
                err.contains("Giving up", true) -> {
                    Timber.e("Server error detected %s", err)
                    AuthState.SERVER_ERROR
                }
                err.contains("Failed connecting to permissioning", true) -> {
                    Timber.e("Connection error detected %s", err)
                    AuthState.CONNECTION_ERROR
                }
                err.contains("Failed to connect to gateway", true) -> {
                    Timber.e("Could not connect to the gateway %s", err)
                    AuthState.GATEWAY_ERROR
                }
                err.contains("The source did not signal an event for", true) ||
                        err.contains("Failed connecting to", true) ||
                        err.contains("UD register timeout exceeded", true) ||
                        err.contains("UD search timeout exceeded", true) -> {
                    Timber.e("Timeout on server %s", err)
                    AuthState.TIMEOUT
                }
                err.contains("Cannot register with existing username", true) -> {
                    Timber.e("Username was already registered %s", err)
                    AuthState.USERNAME_ALREADY_TAKEN
                }
                err.contains("Login: Could not login: Loading both sessions", true) -> {
                    AuthState.WRONG_PASSWORD
                }
                err.contains("cannot search for yourself on UD", true) -> {
                    Timber.e("You can't add yourself as a contact! %s", err)
                    AuthState.UD_ERROR_ADD_YOURSELF
                }
                err.contains("already searched", true) -> {
                    Timber.e("Error: you had already shared keys with this user %s", err)
                    AuthState.UD_ERROR_ALREADY_SHARED
                }
                err.contains("connect to gateways: Versions incompatible", true) ||
                        err.contains("failed to get ndf from permissioning", true) -> {
                    Timber.e("Error: versions incompatible %s", err)
                    AuthState.VERSIONS_INCOMPATIBLE
                }
                else -> AuthState.NONE
            }
        }

        fun shareText(text: String, context: Context) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(context, shareIntent, null)
        }

        fun sendMail(activity: Activity, email: String) {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", email, null
                )
            )
            activity.startActivity(intent)
        }

        fun generateQrCode(
            data: ByteArray,
            sizeInDp: Int
        ): Bitmap? {
            val writer = QRCodeWriter()
            return try {
                val bitMatrix = writer.encode(
                    String(data, Charsets.ISO_8859_1),
                    BarcodeFormat.QR_CODE,
                    dpToPx(sizeInDp),
                    dpToPx(sizeInDp)
                )
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bmp
            } catch (err: WriterException) {
                err.printStackTrace()
                return null
            }
        }

        fun generateQrCodeAsync(
            data: ByteArray,
            sizeInDp: Int
        ): Single<Bitmap?> {
            val writer = QRCodeWriter()
            return Single.create { emitter ->
                try {
                    val bitMatrix = writer.encode(
                        String(data, Charsets.ISO_8859_1),
                        BarcodeFormat.QR_CODE,
                        dpToPx(sizeInDp),
                        dpToPx(sizeInDp)
                    )
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    emitter.onSuccess(bmp)
                } catch (err: WriterException) {
                    err.printStackTrace()
                    emitter.onError(err)
                }
            }
        }

        fun generateQrCode(
            data: String,
            sizeInDp: Int
        ): Bitmap? {
            return try {
                val bitMatrix = MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    sizeInDp, sizeInDp, null
                )
                val bitMatrixWidth = bitMatrix.width
                val bitMatrixHeight = bitMatrix.height

                val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

                for (y in 0 until bitMatrixHeight) {
                    val offset = y * bitMatrixWidth
                    for (x in 0 until bitMatrixWidth) {
                        pixels[offset + x] =
                            if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    }
                }
                val bitmap =
                    Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888)

                bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
                bitmap
            } catch (err: WriterException) {
                err.printStackTrace()
                null
            }
        }

        fun formatStringUrl(string: String): Spanned {
            val patternUrl = Patterns.WEB_URL
            var formattedText: String = string
            val matcherUrl = patternUrl.matcher(string)
            while (matcherUrl.find()) {
                Timber.d(matcherUrl.group(0))
                val replaceText =
                    "<font><a href=\"https://" + matcherUrl.group(1) + "\">${
                        matcherUrl.group(1)
                    }</a></font>"
                formattedText = formattedText.replace(matcherUrl.group(0)!!, replaceText)
            }

            return HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        fun hideKeyboardGlobal(context: Context, v: View, ev: MotionEvent? = null) {
            val outRect = Rect()
            v.getGlobalVisibleRect(outRect)

            if (ev == null) {
                hideKeyboardOnView(v, context)
            } else if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                hideKeyboardOnView(v, context)
            }
        }

        private fun hideKeyboardOnView(v: View, context: Context) {
            v.clearFocus()
            val imm =
                context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }

        fun getTimestampString(time: Long): String {
            val currentTimestamp = getCurrentTimeStamp()
            val diff = TimeUnit.MILLISECONDS.toSeconds(currentTimestamp - time)
            return if (diff in 0..59) {
                "moments ago"
            } else {
                val diffMinutes = time + (time % 60)
                val diffMinutesNow = currentTimestamp - (currentTimestamp % 60)
                DateUtils.getRelativeTimeSpanString(
                    diffMinutes,
                    diffMinutesNow,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
                ).toString()
            }
        }
    }
}