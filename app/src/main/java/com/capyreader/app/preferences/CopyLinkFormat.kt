package com.capyreader.app.preferences

enum class CopyLinkFormat {
    PLAIN_URL,
    MARKDOWN;

    companion object {
        val default = PLAIN_URL
    }
}
