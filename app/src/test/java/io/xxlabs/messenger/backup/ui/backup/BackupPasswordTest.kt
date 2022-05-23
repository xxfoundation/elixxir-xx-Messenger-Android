package io.xxlabs.messenger.backup.ui.backup

import com.google.common.truth.Truth
import io.xxlabs.messenger.randomCaps
import io.xxlabs.messenger.randomLower
import io.xxlabs.messenger.randomNum
import io.xxlabs.messenger.randomString
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BackupPasswordTest {

    @Test
    fun `minimum chars 1 uppercase 1 lowercase 1 number returns true`() {
        val password = randomString(BackupPassword.MIN_LENGTH) +
                randomCaps() + randomLower() + randomNum()
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isTrue()
    }

    @Test
    fun `less than minimum chars returns false`() {
        val password = randomCaps() + randomLower() + randomNum()
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isFalse()
    }

    @Test
    fun `no uppercase returns false`() {
        val password = randomLower(BackupPassword.MIN_LENGTH) + randomNum()
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isFalse()
    }

    @Test
    fun `no lowercase returns false`() {
        val password = randomCaps(BackupPassword.MIN_LENGTH) + randomNum()
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isFalse()
    }

    @Test
    fun `no number returns false`() {
        val password = randomCaps(BackupPassword.MIN_LENGTH) + randomLower()
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isFalse()
    }

    @Test
    fun `valid password containing special character returns true`() {
        val password = randomString(BackupPassword.MIN_LENGTH) +
                randomCaps() + randomLower() + randomNum() + "!@#$"
        val subject = BackupPassword(password)
        Truth.assertThat(subject.isValid).isTrue()
    }
}