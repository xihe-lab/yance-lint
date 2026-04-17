package com.xihe_lab.yance.core.engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * RuleProvider 接口单元测试
 */
class RuleProviderTest {

    @Test
    fun `P3cRuleProvider should implement RuleProvider interface`() {
        val provider = com.xihe_lab.yance.core.provider.p3c.P3cRuleProvider()

        assertNotNull(provider)
        assertEquals("p3c", provider.source)
        // P3cRuleProvider always returns true regardless of project
        // Using @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") to handle IntelliJ annotations
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val isAvailable = provider.isAvailable(null)
        assertTrue(isAvailable)
    }

    @Test
    fun `P3cRuleProvider should provide rules`() {
        val provider = com.xihe_lab.yance.core.provider.p3c.P3cRuleProvider()

        val rules = provider.provideRules()
        assertNotNull(rules)
        assertTrue(rules.size > 0)
    }

    @Test
    fun `P3cRuleProvider should filter rules by language`() {
        val provider = com.xihe_lab.yance.core.provider.p3c.P3cRuleProvider()

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val javaRules = provider.getActiveRules(null, com.xihe_lab.yance.core.model.LanguageType.JAVA)
        assertNotNull(javaRules)

        javaRules.forEach { rule ->
            assertEquals(com.xihe_lab.yance.core.model.LanguageType.JAVA, rule.language)
        }
    }
}
