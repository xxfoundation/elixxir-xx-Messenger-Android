package io.xxlabs.url.preview.crawler

import android.util.Patterns

object WebRegex {
    const val IMAGE_PATTERN = "(.+?)\\.(jpg|png|gif|bmp)$"
    const val IMAGE_TAG_PATTERN =
        "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?"
    const val TITLE_PATTERN = "<title(.*?)>(.*?)</title>"
    const val SCRIPT_PATTERN = "<script(.*?)>(.*?)</script>"
    const val METATAG_PATTERN = "<meta(.*?)>"
    const val METATAG_CONTENT_PATTERN = "content=\"(.*?)\""

    fun matchUrl(possibleUrl: String): Boolean {
        return Patterns.WEB_URL.matcher(possibleUrl).matches()
    }

    fun match(
        content: String,
        pattern: String
    ): String {
        val regex = Regex(pattern)
        val result = regex.find(content)?.value

        return if (!result.isNullOrEmpty()) {
            removeExtraSpace(result)
        } else {
            ""
        }
    }

    fun matchAll(
        content: String,
        pattern: String
    ): List<String> {
        val regex = Regex(pattern)
        return regex.findAll(content).map { match ->
            removeExtraSpace(match.value)
        }.toList()
    }

    fun getTagPattern(tag: String): String {
        return "<$tag(.*?)>(.*?)</$tag>"
    }

    private fun removeExtraSpace(content: String): String =
        content.replace("\\s+".toRegex(), " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .trim()
}