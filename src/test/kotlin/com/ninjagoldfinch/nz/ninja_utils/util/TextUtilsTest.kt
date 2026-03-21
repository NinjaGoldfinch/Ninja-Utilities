package com.ninjagoldfinch.nz.ninja_utils.util

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests string cleaning utilities in [TextUtils].
 *
 * These functions are used everywhere to strip Minecraft formatting.
 * Tests verify the behavioral contract: given formatted input, return clean output.
 * To add cases: add to the relevant @Nested group or create a new one.
 */
class TextUtilsTest {

    @Nested
    inner class StripFormatting {
        @Test
        fun `strips color codes`() {
            assertEquals("Hello World", TextUtils.stripFormatting("\u00a7aHello \u00a7bWorld"))
        }

        @Test
        fun `strips bold and italic codes`() {
            assertEquals("Bold Text", TextUtils.stripFormatting("\u00a7lBold \u00a7oText"))
        }

        @Test
        fun `strips reset code`() {
            assertEquals("Reset", TextUtils.stripFormatting("\u00a7rReset"))
        }

        @Test
        fun `returns empty for empty string`() {
            assertEquals("", TextUtils.stripFormatting(""))
        }

        @Test
        fun `no-op on clean string`() {
            assertEquals("No formatting here", TextUtils.stripFormatting("No formatting here"))
        }

        @Test
        fun `strips multiple consecutive codes`() {
            assertEquals("Text", TextUtils.stripFormatting("\u00a7a\u00a7l\u00a7nText"))
        }
    }

    @Nested
    inner class StripFormattingAndInvisible {
        @Test
        fun `strips zero-width spaces`() {
            assertEquals("Hello", TextUtils.stripFormattingAndInvisible("He\u200Bllo"))
        }

        @Test
        fun `strips combined formatting and invisible chars`() {
            assertEquals(
                "Purse: 500",
                TextUtils.stripFormattingAndInvisible("\u00a76Purse: \u200B\u00a7a500")
            )
        }

        @Test
        fun `strips soft hyphen`() {
            assertEquals("Test", TextUtils.stripFormattingAndInvisible("Te\u00ADst"))
        }

        @Test
        fun `strips BOM character`() {
            assertEquals("Text", TextUtils.stripFormattingAndInvisible("\uFEFFText"))
        }

        @Test
        fun `no-op on clean string`() {
            assertEquals("Clean text", TextUtils.stripFormattingAndInvisible("Clean text"))
        }
    }
}
