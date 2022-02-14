package io.xxlabs.messenger.biometrics

data class EncryptedData(
    val key: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!key.contentEquals(other.key)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}