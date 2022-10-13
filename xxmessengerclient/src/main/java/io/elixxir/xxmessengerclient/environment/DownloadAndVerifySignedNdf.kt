package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class DownloadAndVerifySignedNdf(
    private val bindings: Bindings
) {

    operator fun invoke(environment: NDFEnvironment): Result<ByteArray> = nonNullResultOf {
        bindings.downloadAndVerifySignedNdf(
            environment.url, environment.cert
        )
    }
}
