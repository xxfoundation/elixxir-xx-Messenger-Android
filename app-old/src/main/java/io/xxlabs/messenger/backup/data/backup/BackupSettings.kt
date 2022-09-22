package io.xxlabs.messenger.backup.data.backup

import io.xxlabs.messenger.support.extensions.capitalizeWords

/**
 * Preferences that determine if and when the backup runs automatically.
 */
interface BackupSettings {
    val frequency: Frequency
    val network: Network

    enum class Frequency {
        AUTOMATIC, MANUAL;

        override fun toString(): String {
            return super.toString().capitalizeWords()
        }
    }
    enum class Network {
        WIFI_ONLY {
            override fun toString() = "Wi-Fi Only"
        },
        ANY {
            override fun toString() = "Wi-Fi or Cellular"
        };
    }
}