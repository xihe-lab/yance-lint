package com.xihe_lab.yance.core.provider.p3c

import com.xihe_lab.yance.core.model.LanguageType
import com.xihe_lab.yance.core.model.RuleCategory
import com.xihe_lab.yance.core.model.RuleSeverity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * P3cRuleMetadata 单元测试
 */
class P3cRuleMetadataTest {

    @Test
    fun `should return all rules including PoC and Phase 2-3`() {
        val allRules = P3cRuleMetadata.getAllRules()

        // 应该包含 3 个 PoC 规则 + 6 个 Phase 2-3 规则
        assertEquals(9, allRules.size)
    }

    @Test
    fun `should contain PoC naming rules`() {
        val allRules = P3cRuleMetadata.getAllRules()

        val classNamingRule = allRules.find { it.id == "p3c-naming-001" }
        val methodNamingRule = allRules.find { it.id == "p3c-naming-002" }
        val constantNamingRule = allRules.find { it.id == "p3c-naming-003" }

        assertNotNull(classNamingRule, "Should contain class naming rule")
        assertNotNull(methodNamingRule, "Should contain method naming rule")
        assertNotNull(constantNamingRule, "Should contain constant naming rule")
    }

    @Test
    fun `PoC rule 1 should be class naming with UpperCamelCase`() {
        val rule = P3cRuleMetadata.getAllRules().find { it.id == "p3c-naming-001" }

        assertNotNull(rule)
        assertEquals("p3c-naming-001", rule!!.id)
        assertEquals("类名使用 UpperCamelCase", rule.name)
        assertEquals(RuleCategory.NAMING, rule.category)
        assertEquals(RuleSeverity.WARNING, rule.severity)
        assertEquals(LanguageType.JAVA, rule.language)
        assertTrue(rule.autoFixable)
    }

    @Test
    fun `PoC rule 2 should be method naming with lowerCamelCase`() {
        val rule = P3cRuleMetadata.getAllRules().find { it.id == "p3c-naming-002" }

        assertNotNull(rule)
        assertEquals("p3c-naming-002", rule!!.id)
        assertEquals("方法名使用 lowerCamelCase", rule.name)
        assertEquals(RuleCategory.NAMING, rule.category)
        assertEquals(RuleSeverity.WARNING, rule.severity)
    }

    @Test
    fun `PoC rule 3 should be constant naming with CONSTANT_CASE`() {
        val rule = P3cRuleMetadata.getAllRules().find { it.id == "p3c-naming-003" }

        assertNotNull(rule)
        assertEquals("p3c-naming-003", rule!!.id)
        assertEquals("常量全大写下划线分隔", rule.name)
        assertEquals(RuleCategory.NAMING, rule.category)
        assertEquals(RuleSeverity.WARNING, rule.severity)
    }

    @Test
    fun `Phase 2-3 rules should exist`() {
        val allRules = P3cRuleMetadata.getAllRules()

        val packageNamingRule = allRules.find { it.id == "p3c-naming-004" }
        val equalsRule = allRules.find { it.id == "p3c-oop-001" }
        val hashCodeRule = allRules.find { it.id == "p3c-oop-002" }
        val arraylistContainsRule = allRules.find { it.id == "p3c-collection-001" }
        val exceptionRule = allRules.find { it.id == "p3c-exception-001" }
        val threadPoolRule = allRules.find { it.id == "p3c-concurrency-001" }

        assertNotNull(packageNamingRule)
        assertNotNull(equalsRule)
        assertNotNull(hashCodeRule)
        assertNotNull(arraylistContainsRule)
        assertNotNull(exceptionRule)
        assertNotNull(threadPoolRule)
    }

    @Test
    fun `should have correct tags for each rule`() {
        val namingRule = P3cRuleMetadata.getAllRules().find { it.id == "p3c-naming-001" }

        assertNotNull(namingRule)
        assertTrue(namingRule!!.tags.contains("p3c"))
        assertTrue(namingRule!!.tags.contains("naming"))
    }

    @Test
    fun `exception rules should have WARNING severity`() {
        val exceptionRule = P3cRuleMetadata.getAllRules().find { it.id == "p3c-exception-001" }

        assertNotNull(exceptionRule)
        assertEquals(RuleSeverity.WARNING, exceptionRule!!.severity)
    }
}
