package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import com.capyreader.app.preferences.CopyLinkFormat
import com.capyreader.app.ui.components.formatLink
import com.jocmp.capy.Article

fun Context.shareArticle(
    article: Article,
    format: CopyLinkFormat = CopyLinkFormat.PLAIN_URL
) {
    val url = article.url ?: return

    val textToShare = formatLink(url.toString(), article.title, format)
    val share = Intent.createChooser(Intent().apply {
        type = "text/plain"
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        putExtra(Intent.EXTRA_TITLE, article.title)
    }, null)
    startActivity(share)
}
