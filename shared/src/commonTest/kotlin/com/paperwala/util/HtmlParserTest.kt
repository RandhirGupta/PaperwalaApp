package com.paperwala.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlParserTest {

    @Test
    fun stripTagsReturnsEmptyForNull() {
        assertEquals("", HtmlParser.stripTags(null))
    }

    @Test
    fun stripTagsReturnsEmptyForBlank() {
        assertEquals("", HtmlParser.stripTags(""))
        assertEquals("", HtmlParser.stripTags("   "))
    }

    @Test
    fun stripTagsRemovesHtmlTags() {
        assertEquals("Hello world", HtmlParser.stripTags("<p>Hello <b>world</b></p>"))
    }

    @Test
    fun stripTagsConvertsBreaksToNewlines() {
        val result = HtmlParser.stripTags("Line one<br/>Line two<br>Line three")
        assertEquals("Line one Line two Line three", result)
    }

    @Test
    fun stripTagsDecodesHtmlEntities() {
        assertEquals("Tom & Jerry", HtmlParser.stripTags("Tom &amp; Jerry"))
        assertEquals("1 < 2 > 0", HtmlParser.stripTags("1 &lt; 2 &gt; 0"))
        assertEquals("He said \"hi\"", HtmlParser.stripTags("He said &quot;hi&quot;"))
        assertEquals("it's fine", HtmlParser.stripTags("it&apos;s fine"))
    }

    @Test
    fun stripTagsDecodesNumericEntities() {
        // &#65; = 'A'
        assertEquals("A", HtmlParser.stripTags("&#65;"))
    }

    @Test
    fun stripTagsCollapsesWhitespace() {
        assertEquals("lots of space", HtmlParser.stripTags("lots   of    space"))
    }

    @Test
    fun stripTagsHandlesComplexHtml() {
        val html = """
            <div class="article">
                <h1>Breaking News</h1>
                <p>India &amp; Pakistan hold talks on <b>border</b> security.</p>
            </div>
        """.trimIndent()
        val result = HtmlParser.stripTags(html)
        assertEquals("Breaking News India & Pakistan hold talks on border security.", result)
    }

    @Test
    fun extractFirstImageUrlReturnsNullForNull() {
        assertNull(HtmlParser.extractFirstImageUrl(null))
    }

    @Test
    fun extractFirstImageUrlReturnsNullForBlank() {
        assertNull(HtmlParser.extractFirstImageUrl(""))
    }

    @Test
    fun extractFirstImageUrlReturnsNullWhenNoImage() {
        assertNull(HtmlParser.extractFirstImageUrl("<p>No image here</p>"))
    }

    @Test
    fun extractFirstImageUrlFindsImgSrc() {
        val html = """<p>Text</p><img src="https://example.com/photo.jpg" alt="photo"/>"""
        assertEquals("https://example.com/photo.jpg", HtmlParser.extractFirstImageUrl(html))
    }

    @Test
    fun extractFirstImageUrlFindsFirstOfMultiple() {
        val html = """
            <img src="https://first.com/a.jpg"/>
            <img src="https://second.com/b.jpg"/>
        """.trimIndent()
        assertEquals("https://first.com/a.jpg", HtmlParser.extractFirstImageUrl(html))
    }
}
