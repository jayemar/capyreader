package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.SummaryMaxLines
import com.capyreader.app.ui.settings.PreferenceSelect

@Composable
fun SummaryMaxLinesSelect(
    selected: SummaryMaxLines,
    update: (SummaryMaxLines) -> Unit = {},
) {
    PreferenceSelect(
        selected = selected,
        update = update,
        options = SummaryMaxLines.entries,
        optionText = { stringResource(translationKey(it)) },
        label = R.string.settings_article_list_summary_max_lines
    )
}

private fun translationKey(maxLines: SummaryMaxLines) =
    when (maxLines) {
        SummaryMaxLines.TWO -> R.string.settings_article_list_summary_max_lines_two
        SummaryMaxLines.THREE -> R.string.settings_article_list_summary_max_lines_three
        SummaryMaxLines.FOUR -> R.string.settings_article_list_summary_max_lines_four
        SummaryMaxLines.FIVE -> R.string.settings_article_list_summary_max_lines_five
        SummaryMaxLines.UNLIMITED -> R.string.settings_article_list_summary_max_lines_unlimited
    }
