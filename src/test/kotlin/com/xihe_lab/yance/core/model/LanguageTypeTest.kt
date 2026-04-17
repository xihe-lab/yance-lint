package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * LanguageType 枚举单元测试
 */
class LanguageTypeTest {

    @Test
    fun `should have correct display names`() {
        assertEquals("Java", LanguageType.JAVA.displayName)
        assertEquals("Kotlin", LanguageType.KOTLIN.displayName)
        assertEquals("JavaScript", LanguageType.JAVASCRIPT.displayName)
        assertEquals("TypeScript", LanguageType.TYPESCRIPT.displayName)
        assertEquals("CSS", LanguageType.CSS.displayName)
        assertEquals("HTML", LanguageType.HTML.displayName)
        assertEquals("All", LanguageType.ALL.displayName)
    }

    @Test
    fun `should have correct file extensions`() {
        assertEquals(setOf("java"), LanguageType.JAVA.fileExtensions)
        assertEquals(setOf("kt", "kts"), LanguageType.KOTLIN.fileExtensions)
        assertEquals(setOf("js", "jsx", "mjs"), LanguageType.JAVASCRIPT.fileExtensions)
        assertEquals(setOf("ts", "tsx"), LanguageType.TYPESCRIPT.fileExtensions)
        assertEquals(setOf("css", "scss", "less"), LanguageType.CSS.fileExtensions)
        assertEquals(setOf("html", "htm"), LanguageType.HTML.fileExtensions)
        assertEquals(emptySet<String>(), LanguageType.ALL.fileExtensions)
    }

    @Test
    fun `should match file extension for java`() {
        assertTrue(LanguageType.JAVA.matchesExtension("Test.java"))
        assertTrue(LanguageType.JAVA.matchesExtension("Test.JAVA"))
        assertFalse(LanguageType.JAVA.matchesExtension("Test.kt"))
    }

    @Test
    fun `should match file extension for kotlin`() {
        assertTrue(LanguageType.KOTLIN.matchesExtension("Test.kt"))
        assertTrue(LanguageType.KOTLIN.matchesExtension("Test.kts"))
        assertFalse(LanguageType.KOTLIN.matchesExtension("Test.java"))
    }

    @Test
    fun `should match file extension for javascript`() {
        assertTrue(LanguageType.JAVASCRIPT.matchesExtension("Test.js"))
        assertTrue(LanguageType.JAVASCRIPT.matchesExtension("Test.jsx"))
        assertTrue(LanguageType.JAVASCRIPT.matchesExtension("Test.mjs"))
        assertFalse(LanguageType.JAVASCRIPT.matchesExtension("Test.ts"))
    }

    @Test
    fun `should match file extension for typescript`() {
        assertTrue(LanguageType.TYPESCRIPT.matchesExtension("Test.ts"))
        assertTrue(LanguageType.TYPESCRIPT.matchesExtension("Test.tsx"))
        assertFalse(LanguageType.TYPESCRIPT.matchesExtension("Test.js"))
    }

    @Test
    fun `should match file extension for css`() {
        assertTrue(LanguageType.CSS.matchesExtension("Test.css"))
        assertTrue(LanguageType.CSS.matchesExtension("Test.scss"))
        assertTrue(LanguageType.CSS.matchesExtension("Test.less"))
        assertFalse(LanguageType.CSS.matchesExtension("Test.html"))
    }

    @Test
    fun `should match file extension for html`() {
        assertTrue(LanguageType.HTML.matchesExtension("Test.html"))
        assertTrue(LanguageType.HTML.matchesExtension("Test.htm"))
        assertFalse(LanguageType.HTML.matchesExtension("Test.java"))
    }

    @Test
    fun `should return all for all extensions`() {
        assertTrue(LanguageType.ALL.matchesExtension("Test.java"))
        assertTrue(LanguageType.ALL.matchesExtension("Test.kt"))
        assertTrue(LanguageType.ALL.matchesExtension("Test.js"))
    }

    @Test
    fun `should find language from extension`() {
        assertEquals(LanguageType.JAVA, LanguageType.fromExtension("Test.java"))
        assertEquals(LanguageType.KOTLIN, LanguageType.fromExtension("Test.kt"))
        assertEquals(LanguageType.JAVASCRIPT, LanguageType.fromExtension("Test.js"))
        assertEquals(LanguageType.TYPESCRIPT, LanguageType.fromExtension("Test.tsx"))
        assertEquals(LanguageType.CSS, LanguageType.fromExtension("Test.scss"))
        assertEquals(LanguageType.HTML, LanguageType.fromExtension("Test.htm"))
        assertNull(LanguageType.fromExtension("Test.py"))
    }
}
