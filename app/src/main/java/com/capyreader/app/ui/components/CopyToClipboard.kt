package com.capyreader.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.capyreader.app.preferences.CopyLinkFormat

@Composable
fun buildCopyToClipboard(text: String): () -> Unit {
    val clipboardManager = LocalClipboardManager.current

    return {
        clipboardManager.setText(AnnotatedString(text))
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
        val textToCopy = when (format) {
            CopyLinkFormat.PLAIN_URL -> url
            CopyLinkFormat.MARKDOWN -> {
                if (!title.isNullOrBlank()) {
                    formatMarkdownLink(title, url)
                } else {
                    url
                }
            }
        }
        clipboardManager.setText(AnnotatedString(textToCopy))
    }
}

private fun formatMarkdownLink(title: String, url: String): String {
    val escapedTitle = title.replace("[", "\\[").replace("]", "\\]")
    return "[$escapedTitle]($url)"
}
