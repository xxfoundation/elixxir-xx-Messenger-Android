package io.xxlabs.messenger.backup.cloud.sftp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import io.xxlabs.messenger.backup.cloud.sftp.login.SshCredentials
import io.xxlabs.messenger.backup.cloud.sftp.login.ui.SshLogin
import io.xxlabs.messenger.backup.cloud.sftp.login.ui.SshLoginListener
import io.xxlabs.messenger.randomString
import io.xxlabs.messenger.test.utils.MockEditable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SftpLoginTest {

    private class TestListener : SshLoginListener {
        var credentials: SshCredentials? = null
            private set
        var error: String? = null
            private set

        override fun onLoginSuccess(credentials: SshCredentials) {
            this.credentials = credentials
        }

        override fun onLoginError(message: String) {
            this.error = message
        }
    }

    @Rule @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var listener: TestListener
    private lateinit var subject: SshLogin

    @Before
    fun setUp() {
        listener =  TestListener()
        subject = SshLogin(listener)
    }

    @Test
    fun `Incorrect hostname returns connection error`() {
        subject.apply {
            onHostInput(MockEditable(INCORRECT_HOSTNAME))
            onUsernameInput(MockEditable(VALID_USERNAME))
            onPasswordInput(MockEditable(VALID_PASSWORD))
            onSubmitClicked()
        }
        Truth.assertThat(listener.error).isNotNull()
    }

    @Test
    fun `Incorrect port returns connection error`() {
        subject.apply {
            onHostInput(MockEditable(INCORRECT_HOSTNAME))
            onPortInput(MockEditable(INCORRECT_PORT))
            onUsernameInput(MockEditable(VALID_USERNAME))
            onPasswordInput(MockEditable(VALID_PASSWORD))
            onSubmitClicked()
        }
        Truth.assertThat(listener.error).isNotNull()
    }

    @Test
    fun `Incorrect username returns auth error`() {
        subject.apply {
            onHostInput(MockEditable(CORRECT_HOSTNAME))
            onUsernameInput(MockEditable(INCORRECT_CREDENTIAL))
            onPasswordInput(MockEditable(VALID_PASSWORD))
            onSubmitClicked()
        }
        Truth.assertThat(listener.error).isNotNull()
    }

    @Test
    fun `Incorrect password returns auth error`() {
        subject.apply {
            onHostInput(MockEditable(CORRECT_HOSTNAME))
            onUsernameInput(MockEditable(VALID_USERNAME))
            onPasswordInput(MockEditable(INCORRECT_CREDENTIAL))
            onSubmitClicked()
        }
        Truth.assertThat(listener.error).isNotNull()
    }

    @Test
    fun `Valid connection and credentials returns success`() {
        subject.apply {
            onHostInput(MockEditable(CORRECT_HOSTNAME))
            onUsernameInput(MockEditable(VALID_USERNAME))
            onPasswordInput(MockEditable(VALID_PASSWORD))
            onSubmitClicked()
        }
        Truth.assertThat(listener.credentials).isNotNull()
    }

    companion object {
        private const val CORRECT_HOSTNAME = "127.0.0.1" // Enter test hostname here
        private const val CORRECT_PORT = "23" // Enter test port here
        private const val VALID_USERNAME = "testaccount" // Enter test username here
        private const val VALID_PASSWORD = "password123" // Enter test password here

        private const val INCORRECT_HOSTNAME = "google.com"
        private const val INCORRECT_PORT = "53"
        private val INCORRECT_CREDENTIAL = randomString(8)
    }
}