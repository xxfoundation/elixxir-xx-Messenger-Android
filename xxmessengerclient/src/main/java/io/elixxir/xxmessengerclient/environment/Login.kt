package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.models.ReceptionIdentity
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.E2eParams
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class Login(
    private val bindings: Bindings,
    private val cMixId: () -> E2eId,
    private val authCallbacks: () -> AuthEventListener,
    private val receptionId: () -> ReceptionIdentity,
    private val e2eParams: () -> E2eParams
) {

    operator fun invoke(ephemeral: Boolean = false): Result<E2e> {
        return if (ephemeral) loginEphemeral() else login()
    }

    private fun login(): Result<E2e> {
        return nonNullResultOf {
            bindings.login(
                cMixId(),
                authCallbacks(),
                receptionId(),
                e2eParams()
            )
        }
    }

    private fun loginEphemeral(): Result<E2e> {
        return nonNullResultOf {
            bindings.loginEphemeral(
                cMixId(),
                authCallbacks(),
                receptionId(),
                e2eParams()
            )
        }
    }
}