package com.xihe_lab.yance.core.fix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * QuickFixRegistry 单元测试
 */
class QuickFixRegistryTest {

    @Test
    fun `should create registry instance`() {
        assertNotNull(QuickFixRegistry.instance)
    }

    @Test
    fun `should register and retrieve fix`() {
        val registry = QuickFixRegistry.instance
        val fix = RenameToUpperCamelCaseFix()

        registry.register("test-rule", fix)

        val retrievedFix = registry.getQuickFix("test-rule")
        assertNotNull(retrievedFix)
        assertEquals("test-rule", retrievedFix!!.id)
    }

    @Test
    fun `should return null for non-existent fix`() {
        val registry = QuickFixRegistry.instance

        val fix = registry.getQuickFix("non-existent-rule")
        assertNull(fix)
    }

    @Test
    fun `should get all auto fixable rule ids`() {
        val registry = QuickFixRegistry.instance
        val fix1 = RenameToUpperCamelCaseFix()
        val fix2 = RenameToLowerCamelCaseFix()

        registry.register("rule-1", fix1)
        registry.register("rule-2", fix2)

        val autoFixableIds = registry.getAutoFixableRuleIds()
        assertEquals(2, autoFixableIds.size)
        assertTrue(autoFixableIds.contains("rename-to-upper-camel-case"))
        assertTrue(autoFixableIds.contains("rename-to-lower-camel-case"))
    }

    @Test
    fun `should overwrite existing fix`() {
        val registry = QuickFixRegistry.instance
        val fix1 = RenameToUpperCamelCaseFix()
        val fix2 = RenameToLowerCamelCaseFix()

        registry.register("test-rule", fix1)
        registry.register("test-rule", fix2)

        val retrievedFix = registry.getQuickFix("test-rule")
        assertNotNull(retrievedFix)
        assertEquals("rename-to-lower-camel-case", retrievedFix!!.id)
    }
}
