package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * YanceRule 单元测试
 */
class YanceRuleTest {

    @Test
    fun `should create rule with all properties`() {
        val rule = YanceRule(
            id = "test-rule-001",
            name = "Test Rule",
            description = "A test rule",
            severity = RuleSeverity.WARNING,
            language = LanguageType.JAVA,
            category = RuleCategory.NAMING,
            source = "test",
            enabled = true,
            autoFixable = true,
            fixType = FixType.PSI_QUICK_FIX,
            tags = listOf("test", "naming"),
            fix = null
        )

        assertEquals("test-rule-001", rule.id)
        assertEquals("Test Rule", rule.name)
        assertEquals("A test rule", rule.description)
        assertEquals(RuleSeverity.WARNING, rule.severity)
        assertEquals(LanguageType.JAVA, rule.language)
        assertEquals(RuleCategory.NAMING, rule.category)
        assertEquals("test", rule.source)
        assertTrue(rule.enabled)
        assertTrue(rule.autoFixable)
        assertEquals(FixType.PSI_QUICK_FIX, rule.fixType)
        assertTrue(rule.tags.contains("test"))
        assertTrue(rule.tags.contains("naming"))
    }

    @Test
    fun `should have default values for optional properties`() {
        val rule = YanceRule(
            id = "minimal-rule",
            name = "Minimal Rule",
            description = "A minimal rule",
            severity = RuleSeverity.ERROR,
            language = LanguageType.KOTLIN,
            category = RuleCategory.STYLE,
            source = "test"
        )

        assertTrue(rule.enabled)
        assertFalse(rule.autoFixable)
        assertEquals(FixType.PSI_QUICK_FIX, rule.fixType)
        assertTrue(rule.tags.isEmpty())
    }

    @Test
    fun `should match language correctly`() {
        val javaRule = createRule(language = LanguageType.JAVA)
        val allRule = createRule(language = LanguageType.ALL)

        assertTrue(javaRule.matchesLanguage(LanguageType.JAVA))
        assertFalse(javaRule.matchesLanguage(LanguageType.KOTLIN))

        assertTrue(allRule.matchesLanguage(LanguageType.JAVA))
        assertTrue(allRule.matchesLanguage(LanguageType.KOTLIN))
        assertTrue(allRule.matchesLanguage(LanguageType.JAVASCRIPT))
    }

    @Test
    fun `should match category correctly`() {
        val namingRule = createRule(category = RuleCategory.NAMING)
        val styleRule = createRule(category = RuleCategory.STYLE)

        assertTrue(namingRule.matchesCategory(RuleCategory.NAMING))
        assertFalse(namingRule.matchesCategory(RuleCategory.STYLE))

        assertTrue(styleRule.matchesCategory(RuleCategory.STYLE))
        assertFalse(styleRule.matchesCategory(RuleCategory.NAMING))
    }

    @Test
    fun `should generate rule id correctly`() {
        val id = YanceRule.generateId("p3c", "naming", "001")
        assertEquals("p3c-naming-001", id)
    }

    private fun createRule(
        language: LanguageType = LanguageType.JAVA,
        category: RuleCategory = RuleCategory.NAMING
    ): YanceRule {
        return YanceRule(
            id = "test-rule",
            name = "Test Rule",
            description = "A test rule",
            severity = RuleSeverity.WARNING,
            language = language,
            category = category,
            source = "test"
        )
    }
}
