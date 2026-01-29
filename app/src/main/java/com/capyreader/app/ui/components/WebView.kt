package com.capyreader.app.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebView.HitTestResult.SRC_ANCHOR_TYPE
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.common.WebViewInterface
import com.capyreader.app.common.rememberTalkbackPreference
import com.capyreader.app.ui.articles.detail.articleTemplateColors
import com.capyreader.app.ui.articles.detail.byline
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import com.jocmp.capy.logging.CapyLog
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebView(
    modifier: Modifier,
    state: WebViewState,
    article: Article? = null,
    showImages: Boolean = true,
) {
    AndroidView(
        modifier = modifier,
        factory = { state.webView },
        update = {
            article?.let {
                state.loadHtml(article, showImages)
            }
        }
    )
}

class AccompanistWebViewClient(
    private val assetLoader: WebViewAssetLoader,
    private val onOpenLink: (url: Uri) -> Unit,
    private val httpClient: OkHttpClient,
) : WebViewClient(),
    KoinComponent {
    lateinit var state: WebViewState
        internal set

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val asset = assetLoader.shouldInterceptRequest(request.url)
        if (asset != null) {
            val headers = asset.responseHeaders ?: mutableMapOf()
            headers["Access-Control-Allow-Origin"] = "*"
            asset.responseHeaders = headers
            return asset
        }

        if (!shouldProxyRequest(request)) {
            return null
        }

        // Intercept image requests to apply authentication
        if (isImageRequest(request)) {
            return loadAuthenticatedImage(request)
        }

        // Handle CORS and iframe requests
        return proxyCorsRequest(request)
    }

    private fun isImageRequest(request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val accept = request.requestHeaders["Accept"] ?: ""

        // Check if Accept header indicates an image request
        if (accept.contains("image/")) {
            return true
        }

        // Check common image extensions
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".ico")
        return imageExtensions.any { url.lowercase().contains(it) }
    }

    private fun loadAuthenticatedImage(request: WebResourceRequest): WebResourceResponse? {
        return try {
            val okRequest = Request.Builder()
                .url(request.url.toString())
                .apply {
                    request.requestHeaders.forEach { (key, value) ->
                        header(key, value)
                    }
                }
                .build()

            val response = httpClient.newCall(okRequest).execute()

            if (!response.isSuccessful) {
                CapyLog.error("webview_image_load_failed", Exception("HTTP ${response.code} for ${request.url}"))
                return null
            }

            val contentType = response.header("Content-Type") ?: "application/octet-stream"
            val mimeType = contentType.substringBefore(";").trim()
            val charset = contentType
                .substringAfter("charset=", "UTF-8")
                .substringBefore(";")
                .trim()

            WebResourceResponse(
                mimeType,
                charset,
                response.code,
                response.message.ifEmpty { "OK" },
                response.headers.toMultimap().mapValues { it.value.firstOrNull() ?: "" },
                response.body.byteStream()
            )
        } catch (e: Exception) {
            CapyLog.error("webview_image_intercept", e)
            null
        }
    }

    private fun shouldProxyRequest(request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val origin = request.requestHeaders["Origin"]
        val accept = request.requestHeaders["Accept"]

        // XHR/fetch from null origin (loadDataWithBaseURL)
        // Issue #1616
        val isCorsRequest = origin == "null" && url.startsWith("http")

        // iframe document load
        // Strips X-Frame-Options to allow embeds like Slashdot
        // Issue #1605
        val isIframeNavigation = !request.isForMainFrame &&
            accept?.startsWith("text/html") == true &&
            url.startsWith("http")

        return isCorsRequest || isIframeNavigation
    }

    /**
     * Avoids CORS issues when loading additional pages from Mercury.js
     * Issue #1616
     */
    private fun proxyCorsRequest(request: WebResourceRequest): WebResourceResponse? {
        return try {
            val okRequest = Request.Builder()
                .url(request.url.toString())
                .apply {
                    request.requestHeaders.forEach { (key, value) ->
                        header(key, value)
                    }
                }
                .build()

            val response = httpClient.newCall(okRequest).execute()
            val contentType = response.header("Content-Type") ?: "text/html"
            val mimeType = contentType.substringBefore(";").trim()
            val charset = contentType
                .substringAfter("charset=", "UTF-8")
                .substringBefore(";")
                .trim()

            WebResourceResponse(
                mimeType,
                charset,
                response.code,
                response.message.ifEmpty { "OK" },
                mapOf(
                    "Access-Control-Allow-Origin" to "*",
                    "Access-Control-Allow-Methods" to "GET, POST, OPTIONS",
                    "Access-Control-Allow-Headers" to "*"
                ),
                response.body.byteStream()
            )
        } catch (e: Exception) {
            CapyLog.error("webview_intercept_request", e)
            null
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url

        if (url != null) {
            onOpenLink(url)
        }

        return true
    }
}

data class FindInPageResult(
    val activeMatch: Int = 0,
    val totalMatches: Int = 0,
)

@Stable
class WebViewState(
    private val renderer: ArticleRenderer,
    private val colors: Map<String, String>,
    private val enableNativeScroll: Boolean,
    internal val webView: WebView,
) {
    private var htmlId: String? = null
    private var contentHash: Int = 0
    private var currentAudioUrl: String? = null
    private var isAudioPlaying: Boolean = false

    var onFindResultChanged: ((FindInPageResult) -> Unit)? = null

    init {
        loadEmpty()
        webView.setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
            if (isDoneCounting) {
                onFindResultChanged?.invoke(
                    FindInPageResult(
                        activeMatch = if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0,
                        totalMatches = numberOfMatches
                    )
                )
            }
        }
    }

    fun loadHtml(article: Article, showImages: Boolean) {
        val id = article.id
        val hash = article.content.hashCode()

        if (id == htmlId && hash == contentHash) {
            return
        }

        webView.isVerticalScrollBarEnabled = enableNativeScroll
        htmlId = id
        contentHash = hash

        val html = renderer.render(
            article,
            hideImages = !showImages,
            byline = article.byline(context = webView.context),
            colors = colors
        )

        webView.loadDataWithBaseURL(
            null,
            html,
            null,
            "UTF-8",
            null,
        )
    }

    fun reset() {
        htmlId = null
        loadEmpty()
    }

    fun updateAudioPlayState(url: String?, isPlaying: Boolean) {
        currentAudioUrl = url
        isAudioPlaying = isPlaying
        if (htmlId == null) {
            return
        }
        webView.post {
            if (url != null) {
                val escapedUrl = url.replace("'", "\\'")
                webView.evaluateJavascript("updateAudioPlayState('$escapedUrl', $isPlaying)", null)
            } else {
                webView.evaluateJavascript("resetAudioPlayState()", null)
            }
        }
    }

    fun resetAudioPlayState() {
        updateAudioPlayState(null, false)
    }

    fun findInPage(query: String) {
        webView.findAllAsync(query)
    }

    fun findNext(forward: Boolean = true) {
        webView.findNext(forward)
    }

    fun clearFind() {
        webView.clearMatches()
        onFindResultChanged?.invoke(FindInPageResult())
    }

    private fun loadEmpty() = webView.loadUrl("about:blank")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberWebViewState(
    renderer: ArticleRenderer = koinInject(),
    httpClient: OkHttpClient = koinInject<com.jocmp.capy.Account>().httpClient,
    onNavigateToMedia: (media: Media) -> Unit,
    onRequestLinkDialog: (link: ShareLink) -> Unit,
    onRequestImageDialog: (imageUrl: String, title: String?) -> Unit = { _, _ -> },
    onOpenLink: (url: Uri) -> Unit,
    onOpenAudioPlayer: (audio: AudioEnclosure) -> Unit = {},
    onPauseAudio: () -> Unit = {},
    currentAudioUrl: String? = null,
    isAudioPlaying: Boolean = false,
    key: String? = null,
): WebViewState {
    val enableNativeScroll by rememberTalkbackPreference()
    val colors = articleTemplateColors()
    val context = LocalContext.current
    val currentAudioUrlState by rememberUpdatedState(currentAudioUrl)
    val isAudioPlayingState by rememberUpdatedState(isAudioPlaying)

    val reset = if (enableNativeScroll) {
        key
    } else {
        null
    }

    val client = remember {
        AccompanistWebViewClient(
            assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", AssetsPathHandler(context))
                .addPathHandler("/res/", ResourcesPathHandler(context))
                .build(),
            onOpenLink = onOpenLink,
            httpClient = httpClient,
        )
    }

    return remember {
        val webViewInterface = WebViewInterface(
            navigateToMedia = { onNavigateToMedia(it) },
            onRequestLinkDialog = onRequestLinkDialog,
            onRequestImageDialog = onRequestImageDialog,
            onOpenAudioPlayer = onOpenAudioPlayer,
            onPauseAudio = onPauseAudio,
        )

        val webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            setOnLongClickListener {
                hitTestResult.type == SRC_ANCHOR_TYPE
            }

            addJavascriptInterface(webViewInterface, WebViewInterface.INTERFACE_NAME)

            setBackgroundColor(context.getColor(android.R.color.transparent))

            webViewClient = client
        }

        WebViewState(
            renderer,
            colors,
            enableNativeScroll = enableNativeScroll,
            webView,
        ).also {
            client.state = it
            webViewInterface.onRequestAudioState = {
                it.updateAudioPlayState(currentAudioUrlState, isAudioPlayingState)
            }
        }
    }
}
