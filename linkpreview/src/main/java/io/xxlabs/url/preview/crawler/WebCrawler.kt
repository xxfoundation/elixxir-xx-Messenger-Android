package io.xxlabs.url.preview.crawler

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.xxlabs.url.preview.data.WebContent
import io.xxlabs.url.preview.data.WebResult
import io.xxlabs.url.preview.utils.ImgUtils.isImage
import io.xxlabs.url.preview.utils.ImgUtils.parseImage
import io.xxlabs.url.preview.utils.MetaUtils
import io.xxlabs.url.preview.utils.UrlUtils.extractDomain
import io.xxlabs.url.preview.utils.UrlUtils.extractUrls
import io.xxlabs.url.preview.utils.UrlUtils.getFinalUrl
import io.xxlabs.url.preview.utils.UrlUtils.parseContent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


open class WebCrawler {
    private var cache: MutableMap<String, WebContent> = mutableMapOf()
    private var disposables = CompositeDisposable()
    private val processor: PublishProcessor<WebResult> = PublishProcessor.create()

    fun parseUrlSingle(text: String, callback: RunBeforeCallback? = null): Single<WebContent> {
        callback?.invoke(text)

        val urls: List<String> = extractUrls(text)

        return if (urls.isNotEmpty()) {
            val firstUrl = urls[0]
            if (cache.containsKey(firstUrl)) {
                Log.d(WebCrawler::class.java.simpleName, "link was already searched.. using cache")
                Single.just(cache[firstUrl]!!)
            } else {
                initCrawlingSingle(urls)
            }
        } else {
            Single.error(Exception("No URL"))
        }
    }

    fun parseUrl(text: String, callback: RunBeforeCallback? = null): Flowable<WebResult> {
        callback?.invoke(text)

        val urls: List<String> = extractUrls(text)

        if (urls.isNotEmpty()) {
            if (cache.containsKey(urls[0])) {
                val firstUrl = urls[0]
                processor.onNext(WebResult(cache[firstUrl], firstUrl))
            } else {
                initCrawling(urls)
            }
        } else {
            initCrawling(urls)
        }

        return processor
    }

    private fun initCrawlingSingle(urls: List<String>): Single<WebContent> {
        return getContent(urls)
            .subscribeOn(Schedulers.single())
            .observeOn(Schedulers.single())
            .doOnError { println("error timeout ${System.currentTimeMillis()}") }
            .doOnSuccess { content -> cache[content.url] = content }
    }

    private fun initCrawling(urls: List<String>) {
        disposables.add(
            getContent(urls)
                .doOnError {
                    Log.d(
                        WebCrawler::class.java.simpleName,
                        "error timeout ${System.currentTimeMillis()}"
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ content ->
                    Log.d(WebCrawler::class.java.simpleName, "Url parse failed")
                    cache[content.url] = content
                    processor.onNext(WebResult(cache[content.url], content.url))
                }, {
                    Log.d(WebCrawler::class.java.simpleName, "Url parse failed")
                })
        )
    }

    fun clearUrls() {
        disposables.clear()
        disposables = CompositeDisposable()
    }

    private fun getContent(urls: List<String>): Single<WebContent> {
        val content = WebContent()
        return Single.fromCallable {
            var protocol = 0
            for (url in urls) {
                do {
                    content.finalUrl = if (urls.isNotEmpty()) {
                        getFinalUrl(removeExtraSpace(url))
                    } else {
                        ""
                    }
                    protocol++
                } while (content.finalUrl.isNullOrEmpty() || protocol < 2)

                if (content.finalUrl.isNotEmpty()) {
                    break
                }
            }

            if (content.finalUrl.isNotEmpty()) {
                if (isImage(content.finalUrl)) {
                    parseImage(content)
                } else {
                    handleContent(content)
                }
            }

            getContentFromUrl(content)
        }
    }

    private fun handleContent(content: WebContent) {
        try {
            val doc: Document = getDocument(content.finalUrl)
            content.htmlCode = removeExtraSpace(doc.toString())

            val metaTags: HashMap<String, String> = MetaUtils.getMetaTags(content.htmlCode)
            metaTags["favicon"] = "http://" + extractDomain(content.finalUrl) + "/favicon.ico"
            content.metaTags = metaTags
            content.title = metaTags["title"]
            content.description = metaTags["description"]

            if (content.title.isNullOrEmpty()) {
                val matchTitle = WebRegex.match(content.htmlCode, WebRegex.TITLE_PATTERN)
                if (matchTitle.isNotEmpty()) {
                    content.title = parseContent(matchTitle)
                }
            }

            if (content.description.isNullOrEmpty()) {
                content.description = crawlHtml(content.htmlCode)
            }

            val cleanDescription = removeScriptFrom(content)
            content.description = cleanDescription

            if (metaTags["image"]!!.isNotEmpty()) {
                content.images.add(metaTags["image"]!!)
            }

            if (metaTags["propImage"]!!.matches(WebRegex.IMAGE_PATTERN.toRegex())) {
                val img = metaTags["propImage"]!!
                if (img.startsWith("/")) {
                    content.images.add(content.finalUrl + img)
                } else if (WebRegex.matchUrl(img)) {
                    content.images.add(img)
                }
            }
        } catch (err: Exception) {
            Log.e(
                WebCrawler::class.java.simpleName,
                "Failed to handle content: ${err.localizedMessage}"
            )
        }
    }

    private fun getContentFromUrl(content: WebContent): WebContent {
        val linksSet = content.finalUrl.split("&")
        content.url = linksSet[0]
        content.urlDomain = extractDomain(content.finalUrl)
        content.description = parseContent(content.description)
        return content
    }

    private fun getTagContent(tag: String, content: String): String {
        val pattern = WebRegex.getTagPattern(tag)
        val matches = WebRegex.matchAll(content, pattern)
        var result = ""

        for (match in matches) {
            val currentMatch = parseContent(match)
            if (hasSpacing(currentMatch)) {
                result = removeExtraSpace(currentMatch)
                break
            }
        }

        if (result.isEmpty()) {
            result = WebRegex.match(content, pattern)
        }

        result = removeNonBreakingSpace(result)
        return parseContent(result)
    }

    private fun crawlHtml(content: String): String {
        val spans = getTagContent("span", content)
        val paragraphs = getTagContent("p", content)
        val divs = getTagContent("div", content)

        val result = when {
            paragraphs.length < spans.length || paragraphs.length >= divs.length -> paragraphs
            else -> divs
        }
        return parseContent(result)
    }

    private fun removeScriptFrom(content: WebContent) =
        content.description!!.replace(WebRegex.SCRIPT_PATTERN, "")

    private fun removeNonBreakingSpace(text: String) =
        text.replace("&nbsp;".toRegex(), "")

    private fun getDocument(finalUrl: String): Document {
        val userAgent = if (finalUrl.contains("twitter.com")) {
            "Twitterbot"
        } else {
            "Mozilla"
        }

        val okHttp = OkHttpClient()
        val request: Request = Request.Builder()
            .url(finalUrl)
            .header("User-Agent", userAgent)
            .get()
            .build()

        return Jsoup.parse(okHttp.newCall(request).execute().body?.string())

//        return Jsoup.connect(finalUrl)
//
//            .timeout(12000)
//            .get()
    }

    companion object {
        private fun hasSpacing(currentMatch: String): Boolean {
            return currentMatch
                .contains("\\s+".toRegex())
                .or(currentMatch.contains("\n"))
                .or(currentMatch.contentEquals("\r"))
        }

        internal fun removeExtraSpace(content: String): String =
            content.replace("\\s+".toRegex(), " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim()
    }
}