package io.xxlabs.messenger.bindings.wrapper.bindings

import android.content.Context
import bindings.Bindings
import io.reactivex.Single
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBase
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperMock
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperMock
import io.xxlabs.messenger.data.room.model.ContactData
import timber.log.Timber
import java.io.File

class BindingsWrapperMock {
    companion object : BindingsWrapperBase {
        override fun getNdf(): String {
            return ""
        }

        override fun registerGrpc() {}

        override fun newClient(path: String, password: ByteArray) {}

        override fun login(storageDir: String, password: ByteArray): ClientWrapperBase {
            return ClientWrapperMock(ContactData())
        }

        override fun newUserDiscovery(clientWrapper: ClientWrapperBase): UserDiscoveryWrapperBase {
            return UserDiscoveryWrapperMock(clientWrapper.getUser().getContact() as ContactWrapperMock)
        }

        override fun createSessionFolder(context: Context): File? {
            val sessionFolder = getSessionFolder(context)
            if (sessionFolder?.exists() == true) {
                sessionFolder.deleteRecursively()
            }

            sessionFolder?.mkdir()
            Timber.v("Bindings folder was successfully created at: ${sessionFolder?.absolutePath}")
            return sessionFolder
        }

        override fun getSessionFolder(context: Context): File? {
            val appFolder = context.getExternalFilesDir(null)

            return if (appFolder != null && appFolder.exists()) {
                val sessionFolder = File(appFolder, "xxmessenger/session")
                Timber.v("Session folder location:  ${sessionFolder.absolutePath}")
                sessionFolder
            } else {
                Timber.e("External path is not accessible")
                null
            }
        }

        override fun setTimeSource(kronosFunction: () -> Long) {

        }

        override fun unmarshallContact(rawData: ByteArray): Any {
            return Bindings.unmarshalContact(rawData)
        }

        override fun updateCommonErrors(jsonFile: String) {

        }

        override fun downloadCommonErrors(): Single<String> {
            return Single.just("")
        }

        override fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase {
            TODO("Not yet implemented")
        }
    }
}