package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * RuleCategory 枚举单元测试
 */
class RuleCategoryTest {

    @Test
    fun `should have correct display names`() {
        assertEquals("命名规约", RuleCategory.NAMING.displayName)
        assertEquals("代码风格", RuleCategory.STYLE.displayName)
        assertEquals("复杂度", RuleCategory.COMPLEXITY.displayName)
        assertEquals("安全性", RuleCategory.SECURITY.displayName)
        assertEquals("并发处理", RuleCategory.CONCURRENCY.displayName)
        assertEquals("集合处理", RuleCategory.COLLECTION.displayName)
        assertEquals("异常处理", RuleCategory.EXCEPTION.displayName)
        assertEquals("面向对象", RuleCategory.OOP.displayName)
        assertEquals("注释规约", RuleCategory.COMMENT.displayName)
        assertEquals("通用", RuleCategory.GENERAL.displayName)
    }

    @Test
    fun `should contain all categories`() {
        val categories = RuleCategory.entries
        assertEquals(10, categories.size)

        assertTrue(categories.contains(RuleCategory.NAMING))
        assertTrue(categories.contains(RuleCategory.STYLE))
        assertTrue(categories.contains(RuleCategory.COMPLEXITY))
        assertTrue(categories.contains(RuleCategory.SECURITY))
        assertTrue(categories.contains(RuleCategory.CONCURRENCY))
        assertTrue(categories.contains(RuleCategory.COLLECTION))
        assertTrue(categories.contains(RuleCategory.EXCEPTION))
        assertTrue(categories.contains(RuleCategory.OOP))
        assertTrue(categories.contains(RuleCategory.COMMENT))
        assertTrue(categories.contains(RuleCategory.GENERAL))
    }

    @Test
    fun `should find category by name`() {
        assertEquals(RuleCategory.NAMING, RuleCategory.fromName("NAMING"))
        assertEquals(RuleCategory.NAMING, RuleCategory.fromName("naming"))
        assertEquals(RuleCategory.NAMING, RuleCategory.fromName("命名规约"))

        assertEquals(RuleCategory.STYLE, RuleCategory.fromName("STYLE"))
        assertEquals(RuleCategory.STYLE, RuleCategory.fromName("代码风格"))
    }

    @Test
    fun `should return null for non-existent category`() {
        assertNull(RuleCategory.fromName("NON_EXISTENT"))
        assertNull(RuleCategory.fromName(""))
    }
}
