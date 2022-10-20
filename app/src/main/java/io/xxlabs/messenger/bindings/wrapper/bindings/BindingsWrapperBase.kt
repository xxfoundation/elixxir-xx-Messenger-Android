package io.xxlabs.messenger.bindings.wrapper.bindings

import android.content.Context
import io.reactivex.Single
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBase
import java.io.File

interface BindingsWrapperBase {
    fun getNdf(): String
    fun registerGrpc()
    fun newUserDiscovery(clientWrapper: ClientWrapperBase): UserDiscoveryWrapperBase
    fun newUserDiscoveryFromBackup(
        clientWrapper: ClientWrapperBase,
        emailStringified: String? = "",
        phoneStringified: String? = ""
    ): UserDiscoveryWrapperBase
    fun createSessionFolder(context: Context): File?
    fun getSessionFolder(context: Context): File?
    fun setTimeSource(kronosFunction: () -> Long)
    fun unmarshallContact(rawData: ByteArray): Any
    fun updateCommonErrors(jsonFile: String)
    fun downloadCommonErrors(): Single<String>
    fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase
}
