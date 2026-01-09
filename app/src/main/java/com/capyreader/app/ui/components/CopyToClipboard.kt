package com.capyreader.app.ui.components

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.capyreader.app.preferences.CopyLinkFormat
import kotlinx.coroutines.launch

@Composable
fun buildCopyToClipboard(text: String): () -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", text)))
        }
    }
}

@Composable
fun buildCopyToClipboard(
    url: String,
    title: String?,
    format: CopyLinkFormat
): () -> Unit {
    val clipboardManager = LocalClipboardManager.current

    return {
        val textToCopy = formatLink(url, title, format)
        clipboardManager.setText(AnnotatedString(textToCopy))
    }
}

fun formatLink(url: String, title: String?, format: CopyLinkFormat): String {
    return when (format) {
        CopyLinkFormat.PLAIN_URL -> url
        CopyLinkFormat.MARKDOWN -> {
            if (!title.isNullOrBlank()) {
                formatMarkdownLink(title, url)
            } else {
                url
            }
        }
        CopyLinkFormat.WIKI_LINK -> {
            if (!title.isNullOrBlank()) {
                formatWikiLink(title, url)
            } else {
                url
            }
        }
    }
}

private fun formatMarkdownLink(title: String, url: String): String {
    val escapedTitle = title.replace("[", "\\[").replace("]", "\\]")
    return "[$escapedTitle]($url)"
}

private fun formatWikiLink(title: String, url: String): String {
    return "[$url $title]"
}
