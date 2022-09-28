package io.elixxir.data.user.data

import io.elixxir.data.user.UserRepository
import javax.inject.Inject

class UserDataSource @Inject internal constructor(): UserRepository {
    override suspend fun registerUsername(username: String): Result<Unit> {
        // Save name to preferences if successful
        TODO("Not yet implemented")
    }

    private fun registerUsername(username: String, isDemoAcct: Boolean = false) {
        if (!sessionExists) {
            getOrCreateSession()
            return
        }

        repo.registerUdUsername(username)
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError {
                it.message?.let { error ->
                    if (error.isNetworkNotHealthyError()) handleNetworkHealthError()
                    else {
                        displayError(error)
                        enableUI()
                    }
                }
            }.doOnSuccess {
                onSuccessfulRegistration(username, isDemoAcct)
            }.subscribe()
    }

    private fun displayError(errorMsg: String) {
        error.postValue(bindingsErrorMessage(Exception(errorMsg)))
    }
}