package io.xxlabs.messenger.support.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialog
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.isMockVersion
import timber.log.Timber

class DialogUtils {
    companion object {
        fun getWebPopup(
            context: Context,
            title: String,
            link: String
        ): PopupActionBottomDialog {
            return createPopupDialog(
                context,
                titleText = "Do you want to open the $title?",
                icon = ContextCompat.getDrawable(context, R.drawable.ic_alert_rounded),
                subtitleText = "The $title will be opened using your default browser",
                positiveBtnText = "Open",
                negativeBtnText = "Not now",
                onClickPositive = { _, _ ->
                    Utils.openLink(context, link)
                },
                isCancelable = false
            )
        }

        fun createPopupDialog(
            context: Context,
            titleText: String,
            icon: Drawable? = null,
            subtitleText: String,
            positiveBtnText: String? = null,
            negativeBtnText: String? = null,
            onClickPositive: ((String, String?) -> Unit)? = { _, _ -> },
            onClickNegative: (() -> Unit)? = { },
            positiveBtnColor: Int = R.color.brand_dark,
            isCancelable: Boolean = true
        ): PopupActionBottomDialog {
            return PopupActionBottomDialog.getInstance(
                context = context,
                icon = icon,
                titleText = titleText,
                subtitleText = subtitleText,
                positiveBtnText = positiveBtnText,
                negativeBtnText = negativeBtnText,
                onClickPositive = onClickPositive,
                onClickNegative = onClickNegative,
                positiveBtnColor = positiveBtnColor,
                isIncognito = false,
                isCancellable = isCancelable
            )
        }

        fun createPopupDialogFragment(
            titleText: String,
            icon: Drawable? = null,
            subtitleText: String,
            positiveBtnText: String? = null,
            negativeBtnText: String? = null,
            onClickPositive: ((String) -> Unit)? = { },
            onClickNegative: (() -> Unit)? = { },
            positiveBtnColor: Int = R.color.accent_danger,
            isCancelable: Boolean = true
        ): PopupActionBottomDialogFragment {
            return PopupActionBottomDialogFragment.getInstance(
                icon = icon,
                titleText = titleText,
                subtitleText = subtitleText,
                positiveBtnText = positiveBtnText,
                negativeBtnText = negativeBtnText,
                onClickPositive = onClickPositive,
                onClickNegative = onClickNegative,
                positiveBtnColor = positiveBtnColor,
                isIncognito = false,
                isCancellable = isCancelable
            )
        }

        fun createErrorPopupDialog(
            context: Context,
            exception: Throwable,
            exportLogs: Boolean = false
        ): PopupActionDialog {
            Timber.e("[ERROR] ${exception.localizedMessage}")
            val errorString = exception.localizedMessage ?: "Unknown error"
            val cleanedError = when {
                errorString.contains("rpc error: code") -> {
                    errorString.substringAfter("desc =")
                }
                errorString.contains("gitlab") -> {
                    errorString.substringBefore("gitlab")
                }
                errorString.contains("Get nodes") -> {
                    errorString
                }
                errorString.contains("[NODE_ERROR]") -> {
                    "We are still doing a few things in background, please try in a minute"
                }
                errorString.startsWith("UR:") -> {
                    reportErrorToFirebase(exception)

                    if (errorString.contains("Failed to login")) {
                        errorString.substringAfter("UR:").trim()
                    } else {
                        "Unexpected Error"
                    }
                }
                else -> {
                    errorString
                }
            }
            return PopupActionDialog.getInstance(
                context,
                titleText = "Error",
                icon = R.drawable.ic_alert_rounded,
                subtitleText = cleanedError,
                positiveBtnText = "Ok",
                isCancellable = true,
                exportLogs = exportLogs
            )
        }

        private fun reportErrorToFirebase(exception: Throwable) {
            val firebaseInstance = FirebaseCrashlytics.getInstance()
            firebaseInstance.recordException(exception)
        }
    }
}