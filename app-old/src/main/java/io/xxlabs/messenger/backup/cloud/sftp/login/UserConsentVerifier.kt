package io.xxlabs.messenger.backup.cloud.sftp.login

import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import java.security.PublicKey

interface UserConsentListener {
    fun showConsentPrompt(dialogUI: TwoButtonInfoDialogUI)
}

/**
 * Prompts user to allow connection to a host with an unverified fingerprint.
 */
class UserConsentVerifier(
    private val listener: UserConsentListener? = null
) : HostKeyVerifier {

    override fun verify(hostname: String?, port: Int, key: PublicKey?): Boolean {
        return false
    }

    private fun promptForConsent() {
        // TODO: Pass a TwoButtonInfoDialogUI to the UserConsentListener.
    }
}