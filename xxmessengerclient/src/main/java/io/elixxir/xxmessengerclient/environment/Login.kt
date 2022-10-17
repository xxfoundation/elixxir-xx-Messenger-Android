package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.models.ReceptionIdentity
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.E2eParams
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class Login(private val bindings: Bindings) {

    operator fun invoke(
        cMixId: E2eId,
        authCallbacks:  AuthEventListener,
        receptionId: ReceptionIdentity,
        e2eParams: E2eParams,
        ephemeral: Boolean = false
    ): Result<E2e> {
        return if (ephemeral) {
            nonNullResultOf {
                bindings.loginEphemeral(
                    cMixId,
                    authCallbacks,
                    receptionId,
                    e2eParams
                )
            }
        } else {
            nonNullResultOf {
                bindings.login(
                    cMixId,
                    authCallbacks,
                    receptionId,
                    e2eParams
                )
            }
        }
    }
}