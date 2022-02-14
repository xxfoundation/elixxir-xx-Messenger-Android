package io.xxlabs.messenger.data.data

data class UserSearchResult(
    val receptionId: ByteArray,
    val publicKey: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserSearchResult

        if (!receptionId.contentEquals(other.receptionId)) return false
        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = receptionId.contentHashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }
}