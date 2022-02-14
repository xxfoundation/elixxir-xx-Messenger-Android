package io.xxlabs.url.preview.utils

import io.xxlabs.url.preview.crawler.WebRegex
import io.xxlabs.url.preview.data.WebContent
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object ImgUtils {
    internal fun isImage(url: String): Boolean {
        return url.matches(Regex(WebRegex.IMAGE_PATTERN))
    }

    internal fun parseImage(content: WebContent) {
        content.images.add(content.finalUrl)
        content.title = ""
        content.description = ""
    }

    internal fun getImages(document: Document): ArrayList<String> {
        val matches: ArrayList<String> = arrayListOf()
        val media: Elements = document.select("[src]")

        media.forEach { element: Element ->
            run {
                if (element.tagName() == "img") {
                    matches.add(element.attr("abs:src"))
                }
            }
        }
        return matches
    }
}