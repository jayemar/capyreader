package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.preferences.CopyLinkFormat
import com.capyreader.app.ui.settings.PreferenceSelect

@Composable
fun CopyLinkFormatSelect(
    selected: CopyLinkFormat,
    update: (CopyLinkFormat) -> Unit = {},
) {
    PreferenceSelect(
        selected = selected,
        update = update,
        options = CopyLinkFormat.entries,
        optionText = { stringResource(translationKey(it)) },
        label = R.string.settings_copy_link_format
    )
}

private fun translationKey(format: CopyLinkFormat) =
    when (format) {
        CopyLinkFormat.PLAIN_URL -> R.string.settings_copy_link_format_plain_url
        CopyLinkFormat.MARKDOWN -> R.string.settings_copy_link_format_markdown
        CopyLinkFormat.WIKI_LINK -> R.string.settings_copy_link_format_wiki_link
    }
