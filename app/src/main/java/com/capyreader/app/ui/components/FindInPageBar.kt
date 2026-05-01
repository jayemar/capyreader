package com.capyreader.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun FindInPageBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onClose: () -> Unit,
    activeMatch: Int,
    totalMatches: Int,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colorScheme.surfaceContainer,
        shadowElevation = 4.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.find_in_page_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onFindNext() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            if (query.isNotEmpty()) {
                Text(
                    text = if (totalMatches > 0) "$activeMatch/$totalMatches" else "0/0",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            IconButton(
                onClick = onFindPrevious,
                enabled = totalMatches > 0
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.find_previous)
                )
            }

            IconButton(
                onClick = onFindNext,
                enabled = totalMatches > 0
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.find_next)
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close)
                )
            }
        }
    }
}

@Preview
@Composable
private fun FindInPageBarPreview() {
    CapyTheme {
        FindInPageBar(
            query = "search term",
            onQueryChange = {},
            onFindNext = {},
            onFindPrevious = {},
            onClose = {},
            activeMatch = 3,
            totalMatches = 12,
        )
    }
}

@Preview
@Composable
private fun FindInPageBarEmptyPreview() {
    CapyTheme {
        FindInPageBar(
            query = "",
            onQueryChange = {},
            onFindNext = {},
            onFindPrevious = {},
            onClose = {},
            activeMatch = 0,
            totalMatches = 0,
        )
    }
}
