package io.xxlabs.messenger.backup.cloud.sftp

import com.hierynomus.sshj.key.KeyAlgorithm
import com.hierynomus.sshj.key.KeyAlgorithms
import com.hierynomus.sshj.transport.kex.DHGroups
import com.hierynomus.sshj.transport.kex.ExtInfoClientFactory
import com.hierynomus.sshj.transport.kex.ExtendedDHGroups
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.transport.kex.DHGexSHA1
import net.schmizz.sshj.transport.kex.DHGexSHA256
import net.schmizz.sshj.transport.kex.ECDHNistP

/**
 * Initializes SSH client with algorithms supported by Android.
 */
object Config : DefaultConfig() {
    override fun initKeyAlgorithms() {
        keyAlgorithms = listOf<Factory.Named<KeyAlgorithm>>(
            KeyAlgorithms.SSHRSA(),
            KeyAlgorithms.SSHRSACertV01(),
            KeyAlgorithms.RSASHA256(),
            KeyAlgorithms.RSASHA512(),
            KeyAlgorithms.SSHDSA(),
            KeyAlgorithms.SSHDSSCertV01(),
        )
    }

    override fun initKeyExchangeFactories(bouncyCastleRegistered: Boolean) {
        setKeyExchangeFactories(
            DHGexSHA256.Factory(),
            ECDHNistP.Factory521(),
            ECDHNistP.Factory384(),
            ECDHNistP.Factory256(),
            DHGexSHA1.Factory(),
            DHGroups.Group1SHA1(),
            DHGroups.Group14SHA1(),
            DHGroups.Group14SHA256(),
            DHGroups.Group15SHA512(),
            DHGroups.Group16SHA512(),
            DHGroups.Group17SHA512(),
            DHGroups.Group18SHA512(),
            ExtendedDHGroups.Group14SHA256AtSSH(),
            ExtendedDHGroups.Group15SHA256(),
            ExtendedDHGroups.Group15SHA256AtSSH(),
            ExtendedDHGroups.Group15SHA384AtSSH(),
            ExtendedDHGroups.Group16SHA256(),
            ExtendedDHGroups.Group16SHA384AtSSH(),
            ExtendedDHGroups.Group16SHA512AtSSH(),
            ExtendedDHGroups.Group18SHA512AtSSH(),
            ExtInfoClientFactory()
        )
    }
}