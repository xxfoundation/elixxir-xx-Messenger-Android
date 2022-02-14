package io.xxlabs.url.preview.utils

import io.xxlabs.url.preview.crawler.WebRegex
import java.util.*

object MetaUtils {
    internal fun getMetaTags(content: String): HashMap<String, String> {
        val metaTags = mutableMapOf<String, String>().apply {
            this["url"] = ""
            this["title"] = ""
            this["description"] = ""
            this["image"] = ""
            this["propImage"] = ""
            this["favicon"] = ""
        }

        val matches = WebRegex.matchAll(content, WebRegex.METATAG_PATTERN)
        for (match in matches) {
            val lowerCasedMatch = match.lowercase(Locale.getDefault())
            when {
                isUrl(lowerCasedMatch) -> {
                    setMetaTag(metaTags, "url", match)
                }
                isTitle(lowerCasedMatch) -> {
                    setMetaTag(metaTags, "title", match)
                }
                isDescription(lowerCasedMatch) -> {
                    setMetaTag(metaTags, "description", match)
                }
                isImage(lowerCasedMatch) -> {
                    setMetaTag(metaTags, "image", match)
                }
                isPropImage(lowerCasedMatch) -> {
                    setMetaTag(metaTags, "propImage", match)
                }
            }
        }

        return metaTags as HashMap<String, String>
    }

    private fun isUrl(lowerCasedMatch: String): Boolean {
        return (lowerCasedMatch.contains("property=\"og:url\"")
                || lowerCasedMatch.contains("property='og:url'")
                || lowerCasedMatch.contains("name=\"url\"")
                || lowerCasedMatch.contains("name='url'"))
    }

    private fun isTitle(lowerCasedMatch: String): Boolean {
        return (lowerCasedMatch.contains("property=\"og:title\"")
                || lowerCasedMatch.contains("property='og:title'")
                || lowerCasedMatch.contains("name=\"title\"")
                || lowerCasedMatch.contains("name='title'"))
    }

    private fun isDescription(lowerCasedMatch: String): Boolean {
        return (lowerCasedMatch.contains("property=\"og:description\"")
                || lowerCasedMatch.contains("property='og:description'")
                || lowerCasedMatch.contains("name=\"description\"")
                || lowerCasedMatch.contains("name='description'"))
    }

    private fun isImage(lowerCasedMatch: String): Boolean {
        return (lowerCasedMatch.contains("property=\"og:image\"")
                || lowerCasedMatch.contains("property='og:image'")
                || lowerCasedMatch.contains("name=\"image\"")
                || lowerCasedMatch.contains("name='image'"))
    }

    private fun isPropImage(lowerCasedMatch: String): Boolean {
        return (lowerCasedMatch.contains("itemprop=\"image\""))
    }

    private fun setMetaTag(
        metaTags: MutableMap<String, String>,
        url: String,
        value: String
    ) {
        if (value.isNotEmpty()) {
            metaTags[url] = extractMetaContent(value)
        }
    }

    private fun extractMetaContent(content: String): String {
        val result = WebRegex.match(content, WebRegex.METATAG_CONTENT_PATTERN)
        val cleanResult = result
            .replace("content=\"", "")
            .replace("itemprop=\"image\"", "")
            .replace("\"".toRegex(), "")
        return UrlUtils.parseContent(cleanResult)
    }
}