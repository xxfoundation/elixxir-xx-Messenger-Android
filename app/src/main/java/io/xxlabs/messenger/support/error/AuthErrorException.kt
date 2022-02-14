package io.xxlabs.messenger.support.error

import io.xxlabs.messenger.data.datatype.AuthState

class AuthErrorException(authState: AuthState) : Exception(getErrorMsg(authState)) {

    companion object {
        fun getInstance(authState: AuthState): Exception {
            val exceptionMsg = getErrorMsg(authState)
            return Exception(exceptionMsg)
        }

        private fun getErrorMsg(authState: AuthState) =
            when (authState) {
                AuthState.GATEWAY_ERROR -> {
                    "Failed to connect to the gateway. Try again."
                }

                AuthState.SERVER_ERROR, AuthState.CONNECTION_ERROR -> {
                    "The Elixxir Network is experiencing some difficulties, try again later"
                }

                AuthState.TIMEOUT -> {
                    "The network timed out. Please try again."
                }

                AuthState.USERNAME_ALREADY_TAKEN -> {
                    "Username is already taken"
                }

                AuthState.WRONG_PASSWORD -> {
                    "Wrong password"
                }


                AuthState.UD_ERROR_ADD_YOURSELF -> {
                    "You can\'t add yourself as a contact!"
                }

                AuthState.UD_ERROR_ALREADY_SHARED -> {
                    "You have already shared keys with this user"
                }

                AuthState.VERSIONS_INCOMPATIBLE -> {
                    "Your version is incompatible. Please update your app to continue use!"
                }

                else -> {
                    "Something went wrong"
                }
            }
    }
}