package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class DownloadAndVerifySignedNdf(
    private val bindings: Bindings,
    private val udAddress: () -> String,
    private val udCert: () -> String,
) {

    operator fun invoke(): Result<ByteArray> = nonNullResultOf {
        bindings.downloadAndVerifySignedNdf(
            udAddress(), udCert()
        )
    }
}
