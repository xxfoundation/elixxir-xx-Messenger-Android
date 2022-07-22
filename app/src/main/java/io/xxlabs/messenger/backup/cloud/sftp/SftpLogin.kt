package io.xxlabs.messenger.backup.cloud.sftp

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.*
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.userauth.UserAuthException
import java.io.Serializable

data class SftpCredentials(
    val host: String,
    val port: String,
    val username: String,
    val password: String
) : Serializable {

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): SftpCredentials =
            Gson().fromJson(json, SftpCredentials::class.java)
    }
}

interface SftpLoginListener {
    fun onLoginSuccess(credentials: SftpCredentials)
    fun onLoginError(message: String)
}

class SftpLogin(private val listener: SftpLoginListener) : SftpLoginUi {

    private val scope =  CoroutineScope(
        CoroutineName("SftpLogin")
                + Job()
                + Dispatchers.Default
    )

    override val maxHostLength: Int = MAX_INPUT_LENGTH
    override val hostError: LiveData<String?> by ::_hostError
    private val _hostError = MutableLiveData<String?>(null)

    override val maxPortLength: Int = MAX_PORT_RANGE.toString().length
    override val portError: LiveData<String?> by ::_portError
    private val _portError = MutableLiveData<String?>(null)

    override val maxUsernameLength: Int = MAX_INPUT_LENGTH
    override val usernameError: LiveData<String?> by ::_usernameError
    private val _usernameError = MutableLiveData<String?>(null)

    override val maxPasswordLength: Int = MAX_INPUT_LENGTH
    override val passwordError: LiveData<String?> by ::_passwordError
    private val _passwordError = MutableLiveData<String?>(null)

    override val submitButtonEnabled: LiveData<Boolean> by ::_submitButtonEnabled
    private val _submitButtonEnabled = MutableLiveData(false)

    override val textInputEnabled: LiveData<Boolean> by ::_textInputEnabled
    private val _textInputEnabled = MutableLiveData(true)

    private var host: String = ""
    private var port: String = DEFAULT_SSH_PORT.toString()
        get() = field.ifEmpty { DEFAULT_SSH_PORT.toString() }
    private var username: String = ""
    private var password: String = ""

    override fun onHostInput(text: Editable) {
        host = text.toString()
        maybeEnableSubmitButton()
    }

    override fun onPortInput(text: Editable) {
        port = text.toString()
        enforceValidPort(port)
    }

    private fun enforceValidPort(port: String) {
        if (port.toInt() in MIN_PORT_RANGE..MAX_PORT_RANGE) _portError.value = null
        else _portError.value = ERROR_PORT_RANGE
    }

    override fun onUsernameInput(text: Editable) {
        username = text.toString()
        maybeEnableSubmitButton()
    }

    override fun onPasswordInput(text: Editable) {
        password = text.toString()
        maybeEnableSubmitButton()
    }

    override fun onSubmitClicked() {
        clearErrors()
        disableInput()

        scope.launch {
            try {
                login()
            } catch (e: Exception) {
                onError(e.message)
            }
        }
    }

    private fun login() {
        val ssh = SSHClient().apply {
            connect(host, port.toInt())
        }
        try {
            ssh.authPassword(username, password)
            onSuccess()
        } catch (e: UserAuthException) {
            showCredentialsError()
        } catch (e: TransportException) {
            showConnectionError()
        } catch (e: Exception) {
            onError(e.message)
        } finally {
            ssh.close()
        }
    }

    private fun showCredentialsError() {
        _usernameError.postValue(ERROR_CREDENTIALS)
        _passwordError.postValue(ERROR_CREDENTIALS)
        onError(ERROR_CREDENTIALS)
    }

    private fun showConnectionError() {
        _hostError.postValue(ERROR_CONNECTION)
        _portError.postValue(ERROR_CONNECTION)
        onError(ERROR_CONNECTION)
    }

    private fun clearErrors() {
        _hostError.postValue(null)
        _portError.postValue(null)
        _usernameError.postValue(null)
        _passwordError.postValue(null)
    }

    private fun disableInput() {
        _textInputEnabled.value = false
        _submitButtonEnabled.value = false
    }

    private fun onSuccess() {
        listener.onLoginSuccess(SftpCredentials(host, port, username, password))
        enableInput()
    }

    private fun onError(e: String?) {
        listener.onLoginError(e ?: ERROR_GENERIC)
        enableInput()
    }

    private fun enableInput() {
        _textInputEnabled.postValue(true)
        _submitButtonEnabled.postValue(true)
    }

    private fun maybeEnableSubmitButton() {
        _submitButtonEnabled.value = isFormComplete()
    }

    private fun isFormComplete(): Boolean {
        return !(host.isEmpty() && username.isEmpty() && password.isEmpty())
                && _portError.value.isNullOrEmpty()
    }

    companion object {
        private const val DEFAULT_SSH_PORT = 22
        private const val MIN_PORT_RANGE = 1024
        private const val MAX_PORT_RANGE = 32767
        private const val MAX_INPUT_LENGTH = 256

        private const val ERROR_CREDENTIALS = "Failed to login. Check your credentials and try again."
        private const val ERROR_CONNECTION = "Failed to connect to server. Check the hostname and port and try again."
        private const val ERROR_GENERIC = "Failed to login."
        private const val ERROR_PORT_RANGE = "Enter a value between 1024 and 32,767."
    }
}