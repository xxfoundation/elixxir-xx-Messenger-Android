package io.xxlabs.messenger.data.data

data class AvatarWrapper(
    var username: String,
    var userId: ByteArray,
    var photo: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AvatarWrapper

        if (username != other.username) return false

        return true
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}