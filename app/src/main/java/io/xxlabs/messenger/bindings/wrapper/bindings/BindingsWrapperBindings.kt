package io.xxlabs.messenger.bindings.wrapper.bindings

import android.content.Context
import bindings.Bindings
import bindings.TimeSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Single
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings.Companion.development
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBase
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBindings
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBindings
import io.xxlabs.messenger.data.datatype.Environment
import io.xxlabs.messenger.repository.client.NodeErrorException
import io.xxlabs.messenger.support.appContext
import timber.log.Timber
import java.io.File
import java.lang.UnsupportedOperationException

private val devUserDiscoveryIp = "18.198.117.203:11420".encodeToByteArray()

class BindingsWrapperBindings {

    companion object : BindingsWrapperBase {
        private const val NDF_URL_MAINNET =
            "https://elixxir-bins.s3.us-west-1.amazonaws.com/ndf/mainnet.json"
        private const val NDF_URL_RELEASE =
            "https://elixxir-bins.s3.us-west-1.amazonaws.com/ndf/release.json"

        override fun getNdf(): String {
            return when (BuildConfig.ENVIRONMENT) {
                Environment.MAIN_NET -> {
                    downloadAndVerifySignedNdfWithUrl(
                        NDF_URL_MAINNET,
                        certificateFor(Environment.MAIN_NET)
                    )
                }
                Environment.RELEASE_NET -> {
                    downloadAndVerifySignedNdfWithUrl(
                        NDF_URL_RELEASE,
                        certificateFor(Environment.RELEASE_NET)
                    )
                }
                else -> getLocalNdf()
            }
        }

        private fun certificateFor(environment: Environment): String {
            val certFile: Int = when (environment) {
                Environment.MAIN_NET -> R.raw.mainnet
                Environment.RELEASE_NET -> R.raw.release
                else -> {
                    throw UnsupportedOperationException("No certificate found for $environment")
                }
            }
            val reader= appContext().resources.openRawResource(certFile)
                .bufferedReader()
            return reader.use { reader.readText() }
        }

        private fun downloadAndVerifySignedNdfWithUrl(
            url: String,
            certificate: String
        ): String = String(Bindings.downloadAndVerifySignedNdfWithUrl(url, certificate))

        private fun getLocalNdf() = BuildConfig.NDF

        override fun registerGrpc() {
            Bindings.registerLogWriter { writer ->
                Timber.v("[DEFAULT LOGWRITER] $writer")
            }
            Bindings.logLevel(1)
        }

        override fun newClient(path: String, password: ByteArray) {
            val ndf = getNdf()
            Bindings.newClient(
                ndf,
                path,
                password,
                ""
            )
        }

        override fun login(storageDir: String, password: ByteArray): ClientWrapperBase {
            return ClientWrapperBindings(Bindings.login(storageDir, password, ""))
        }

        override fun newUserDiscovery(clientWrapper: ClientWrapperBase): UserDiscoveryWrapperBase {
            return UserDiscoveryWrapperBindings(
                Bindings.newUserDiscovery((clientWrapper as ClientWrapperBindings).client),
                clientWrapper.getUser().getContact() as ContactWrapperBindings
            ).apply { onUdInitialized() }
        }

        override fun newUserDiscoveryFromBackup(
            clientWrapper: ClientWrapperBase,
            emailStringified: String?,
            phoneStringified: String?
        ): UserDiscoveryWrapperBase {
            return UserDiscoveryWrapperBindings(Bindings.newUserDiscoveryFromBackup(
                (clientWrapper as ClientWrapperBindings).client, emailStringified, phoneStringified,),
                clientWrapper.getUser().getContact() as ContactWrapperBindings
            ).apply { onUdInitialized() }
        }

        private fun UserDiscoveryWrapperBindings.onUdInitialized() {
            XxMessengerApplication.isUserDiscoveryRunning = true
            development(BuildConfig.DEBUG)
        }

        private fun UserDiscoveryWrapperBindings.development(enabled: Boolean) {
            if (enabled) {
                setAlternativeUD(
                    devUserDiscoveryIp,
                    rawBytes(R.raw.ud_elixxir_io),
                    rawBytes(R.raw.ud_contact_test)
                )
            } else {
                restoreNormalUD()
            }
        }

        private fun rawBytes(resourceId: Int): ByteArray {
            return appContext().resources
                .openRawResource(resourceId)
                .use { it.readBytes() }
        }

        override fun createSessionFolder(context: Context): File =
            getSessionFolder(context).apply {
                if (exists()) {
                    Timber.v("Bindings folder from previous installation was found.")
                    Timber.v("It contains ${listFiles()?.size ?: 0} files.")
                    Timber.v("Deleting!")
                    deleteRecursively()
                }
                mkdir()
                Timber.v("Bindings folder was successfully created at: $absolutePath")
            }

        override fun getSessionFolder(context: Context): File =
            File(context.filesDir, "xxmessenger/session").apply {
                Timber.v("Session folder location: $absolutePath")
            }

        override fun setTimeSource(kronosFunction: () -> Long) {
            Bindings.setTimeSource(TimeSource(kronosFunction))
        }

        override fun unmarshallContact(rawData: ByteArray): Any {
            return Bindings.unmarshalContact(rawData)
        }

        override fun updateCommonErrors(jsonFile: String) {
            Bindings.updateCommonErrors(jsonFile)
        }

        override fun downloadCommonErrors(): Single<String> {
            return Single.create { emitter ->
                try {
                    val commonErrors = Bindings.downloadErrorDB().decodeToString()
                    Bindings.updateCommonErrors(commonErrors)
                    emitter.onSuccess(commonErrors)
                } catch (err: Exception) {
                    err.printStackTrace()
                    emitter.onError(err)
                }
            }
        }

        override fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase {
            return SendReportBindings(Bindings.unmarshalSendReport(marshalledReport))
        }

        fun getXxdkVersion(): String {
            return Bindings.getVersion()
        }

        fun getGitVersion(): String {
            return Bindings.getGitVersion().substringBefore(" ")
        }

        fun getGitDependencies(): String {
            return Bindings.getDependencies()
        }
    }
}

fun bindingsErrorMessage(exception: Throwable): String {
    val bindingsErrorMsg = Bindings.errorStringToUserFriendlyMessage(exception.localizedMessage)
    return when {
        exception is NodeErrorException ->
            "Establishing secure connection. Please try again in a moment."
        bindingsErrorMsg.startsWith("UR") -> {
            FirebaseCrashlytics.getInstance().recordException(exception)
            "Unexpected error. Please try again."
        }
        else -> bindingsErrorMsg
    }
}