package io.xxlabs.url.preview.utils

import android.util.Log
import io.xxlabs.url.preview.crawler.WebCrawler.Companion.removeExtraSpace
import io.xxlabs.url.preview.crawler.WebRegex
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

object UrlUtils {
    const val HTTP_PROTOCOL = "http://"
    const val HTTPS_PROTOCOL = "https://"
    const val FTP_PROTOCOL = "ftp://"

    internal fun parseContent(content: String?): String {
        return Jsoup.parse(content).text()
    }

    fun extractUrls(textUrl: String): List<String> {
        val urls: MutableList<String> = mutableListOf()
        val splitString = textUrl.split(" ".toRegex())

        for (possibleUrl in splitString) {
            if (WebRegex.matchUrl(possibleUrl)) {
                try {
                    when {
                        possibleUrl.startsWith(HTTPS_PROTOCOL) -> {
                            urls.add(possibleUrl.replace(HTTPS_PROTOCOL, HTTP_PROTOCOL))
                            urls.add(possibleUrl)
                        }
                        possibleUrl.startsWith(HTTP_PROTOCOL) -> {
                            urls.add(possibleUrl)
                            urls.add(possibleUrl.replace(HTTP_PROTOCOL, HTTPS_PROTOCOL))
                        }
                        else -> {
                            urls.add(HTTP_PROTOCOL + possibleUrl)
                            urls.add(HTTPS_PROTOCOL + possibleUrl)
                        }
                    }
                } catch (err: Exception) {
                    Log.d(UrlUtils.javaClass.simpleName, "Not an URL")
                }
            }
        }

        urls.forEachIndexed { index: Int, url: String ->
            urls[index] = removeExtraSpace(url)
        }

        return urls
    }

    internal fun getFinalUrl(
        originURL: String,
        maxRedirects: Int = 6
    ): String {
        val urlConn = if (originURL.startsWith(HTTP_PROTOCOL)) {
            checkUrlConnection(originURL) as HttpURLConnection
        } else {
            checkUrlConnection(originURL) as HttpsURLConnection
        }

        urlConn.instanceFollowRedirects = false

        if (maxRedirects > 0
            && urlConn.responseCode == HttpURLConnection.HTTP_MOVED_PERM
            || urlConn.responseCode == HttpURLConnection.HTTP_MOVED_TEMP
        ) {
            val redirectUrl: String = urlConn.getHeaderField("Location")
            urlConn.disconnect()
            return getFinalUrl(redirectUrl, maxRedirects - 1)
        }

        return originURL
    }

    private fun checkUrlConnection(textUrl: String?): URLConnection? {
        return try {
            val url = URL(textUrl)
            url.openConnection()
        } catch (err: MalformedURLException) {
            throw err
        }
    }

    internal fun extractDomain(url: String): String {
        var urlTmp = url
        var domain = ""

        if (url.startsWith(HTTP_PROTOCOL)) {
            urlTmp = url.substring(HTTP_PROTOCOL.length)
        } else if (url.startsWith(HTTPS_PROTOCOL)) {
            urlTmp = url.substring(HTTPS_PROTOCOL.length)
        }

        val urlLength = urlTmp.length
        for (i in 0 until urlLength) {
            domain += if (urlTmp[i] != '/') urlTmp[i] else break
        }
        return domain
    }
}