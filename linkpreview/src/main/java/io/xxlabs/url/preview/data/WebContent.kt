package io.xxlabs.url.preview.data

import java.util.*

data class WebContent(
    var htmlCode: String = "",
    var raw: String = "",
    var url: String = "",
    var urlDomain: String = "",
    var urlData: Array<String?> = arrayOfNulls(2),
    var finalUrl: String = "",
    var title: String? = "",
    var description: String? = "",
    var images: ArrayList<String> = arrayListOf(),
    var metaTags: HashMap<String, String> = HashMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebContent

        if (!raw.contentEquals(other.raw)) return false

        return true
    }

    fun isEmpty(): Boolean {
        return htmlCode.isEmpty()
    }

    fun getFavicon(): String? {
        return metaTags["favicon"]
    }

    override fun hashCode(): Int {
        return urlData.contentHashCode()
    }
}