package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.settings.PreferenceSelect
import com.jocmp.capy.articles.ArticleSortField

@Composable
fun SortFieldSelect(
    selected: ArticleSortField,
    update: (ArticleSortField) -> Unit = {},
) {
    PreferenceSelect(
        selected = selected,
        update = update,
        options = ArticleSortField.entries,
        optionText = { stringResource(translationKey(it)) },
        label = R.string.article_list_sort_field_title
    )
}

private fun translationKey(field: ArticleSortField) = when (field) {
    ArticleSortField.PUBLISHED_AT -> R.string.article_list_sort_field_published_at
    ArticleSortField.RETRIEVED_AT -> R.string.article_list_sort_field_retrieved_at
}
