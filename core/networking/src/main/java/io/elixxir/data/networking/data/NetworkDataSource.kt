package io.elixxir.data.networking.data

import io.elixxir.core.common.Config
import io.elixxir.core.common.util.resultOf
import io.elixxir.data.networking.BindingsRepository
import io.elixxir.data.networking.NetworkRepository
import io.elixxir.xxclient.cmix.CMix
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkDataSource @Inject internal constructor(
    private val bindings: BindingsRepository,
    config: Config
) : NetworkRepository, Config by config {

    private val cMix: CMix
        get() = bindings.getCMix() ?: throw(IllegalStateException("CMix not initialized"))

    override suspend fun initializeNetwork(): Result<Unit> = resultOf {
        do {
            waitTilHealthy()
            delay(NETWORK_POLL_INTERVAL_MS)
        } while (!waitTilHealthy())

        initializeNetworkFollower()
    }

    private suspend fun waitTilHealthy(): Boolean = suspendCoroutine { continuation ->
        continuation.resume(cMix.waitForNetwork(NETWORK_TIMEOUT_MS))
    }

    private suspend fun initializeNetworkFollower(): Unit = withContext(dispatcher) {
        cMix.startNetworkFollower(NETWORK_TIMEOUT_MS)
    }

    companion object {
        private const val MAX_NETWORK_RETRIES = 29
        private const val NETWORK_POLL_INTERVAL_MS = 1000L
        private const val NETWORK_TIMEOUT_MS = 30_000L
    }
}