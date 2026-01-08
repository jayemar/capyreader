package com.capyreader.app.preferences

enum class CopyLinkFormat {
    PLAIN_URL,
    MARKDOWN,
    WIKI_LINK;

    companion object {
        val default = PLAIN_URL
    }
}
