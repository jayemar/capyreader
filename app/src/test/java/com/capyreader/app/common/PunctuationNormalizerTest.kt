package com.capyreader.app.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PunctuationNormalizerTest {

    @Test
    fun `replaces fullwidth punctuation when enabled`() {
        val input = "Hello，world．How are you？"
        val expected = "Hello,world.How are you?"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `does nothing when disabled`() {
        val input = "Hello，world．"
        assertEquals(input, PunctuationNormalizer.normalize(input, enabled = false))
    }

    @Test
    fun `handles all punctuation types`() {
        val input = "Test！？（）［］｛｝：；，．"
        val expected = "Test!?()[]{}:;,."
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `preserves normal text`() {
        val input = "Regular text with normal, punctuation."
        assertEquals(input, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles mixed fullwidth and normal`() {
        val input = "Normal, but also，fullwidth"
        val expected = "Normal, but also,fullwidth"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles CJK text`() {
        // Note: This WILL replace in CJK text when enabled
        // User chooses to enable knowing this
        val input = "日本語，テスト"
        val expected = "日本語,テスト"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles empty string`() {
        assertEquals("", PunctuationNormalizer.normalize("", enabled = true))
    }

    @Test
    fun `handles multiple commas in sentence`() {
        val input = "First，second，third item"
        val expected = "First,second,third item"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles fullwidth symbols`() {
        val input = "Email＠example．com　＃hashtag　＄price　％percent"
        val expected = "Email@example.com　#hashtag　\$price　%percent"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles fullwidth brackets and braces`() {
        val input = "Test（paren）［bracket］｛brace｝"
        val expected = "Test(paren)[bracket]{brace}"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles fullwidth comparison operators`() {
        val input = "a　＜　b　＞　c"
        val expected = "a　<　b　>　c"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles fullwidth slashes`() {
        val input = "path／to／file　escape＼char"
        val expected = "path/to/file　escape\\char"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles real world example`() {
        val input = "Moltbot， the AI agent"
        val expected = "Moltbot, the AI agent"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }

    @Test
    fun `handles mixed language text`() {
        val input = "English text，中文文本，more English"
        val expected = "English text,中文文本,more English"
        assertEquals(expected, PunctuationNormalizer.normalize(input, enabled = true))
    }
}
