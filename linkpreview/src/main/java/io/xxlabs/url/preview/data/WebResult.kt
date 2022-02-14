package io.xxlabs.url.preview.data

data class WebResult(
    val content: WebContent?,
    val url: String
) {
    fun isEmpty(): Boolean {
        val isContentNull = content == null
        val isContentEmpty = content?.isEmpty()
        return isContentNull || isContentEmpty!!
    }
}