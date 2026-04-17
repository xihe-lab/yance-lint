package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * FixScope 枚举单元测试
 */
class FixScopeTest {

    @Test
    fun `should have correct fix scopes`() {
        val scopes = FixScope.entries
        assertEquals(3, scopes.size)

        assertTrue(scopes.contains(FixScope.ELEMENT))
        assertTrue(scopes.contains(FixScope.FILE))
        assertTrue(scopes.contains(FixScope.MODULE))
    }

    @Test
    fun `ELEMENT scope should fixesingle element`() {
        assertEquals("ELEMENT", FixScope.ELEMENT.toString())
    }

    @Test
    fun `FILE scope should fix entire file`() {
        assertEquals("FILE", FixScope.FILE.toString())
    }

    @Test
    fun `MODULE scope should fix entire module`() {
        assertEquals("MODULE", FixScope.MODULE.toString())
    }
}
