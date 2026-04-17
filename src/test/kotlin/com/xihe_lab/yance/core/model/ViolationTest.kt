package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Violation 单元测试
 */
class ViolationTest {

    private val testRule = YanceRule(
        id = "test-rule",
        name = "Test Rule",
        description = "A test rule",
        severity = RuleSeverity.WARNING,
        language = LanguageType.JAVA,
        category = RuleCategory.NAMING,
        source = "test",
        enabled = true,
        autoFixable = true
    )

    @Test
    fun `should create violation with all properties`() {
        val violation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 10,
            column = 5,
            message = "Test violation",
            severity = RuleSeverity.WARNING
        )

        assertEquals(testRule, violation.rule)
        assertEquals("/path/to/Test.java", violation.filePath)
        assertEquals(10, violation.line)
        assertEquals(5, violation.column)
        assertEquals("Test violation", violation.message)
        assertEquals(RuleSeverity.WARNING, violation.severity)
    }

    @Test
    fun `should determine error status correctly`() {
        val errorViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Error",
            severity = RuleSeverity.ERROR
        )
        assertTrue(errorViolation.isError)

        val warningViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Warning",
            severity = RuleSeverity.WARNING
        )
        assertFalse(warningViolation.isError)
    }

    @Test
    fun `should determine warning status correctly`() {
        val warningViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Warning",
            severity = RuleSeverity.WARNING
        )
        assertTrue(warningViolation.isWarning)

        val errorViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Error",
            severity = RuleSeverity.ERROR
        )
        assertFalse(errorViolation.isWarning)
    }

    @Test
    fun `should determine info status correctly`() {
        val infoViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Info",
            severity = RuleSeverity.INFO
        )
        assertTrue(infoViolation.isInfo)

        val warningViolation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Warning",
            severity = RuleSeverity.WARNING
        )
        assertFalse(warningViolation.isInfo)
    }

    @Test
    fun `should have correct location format`() {
        val violation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 10,
            column = 5,
            message = "Test violation",
            severity = RuleSeverity.WARNING
        )

        // line and column are 0-based, so display shows line+1 and column+1
        assertEquals("/path/to/Test.java:11:6", violation.location)
    }

    @Test
    fun `should compare violations correctly`() {
        val v1 = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 10,
            column = 5,
            message = "Test violation",
            severity = RuleSeverity.WARNING
        )

        val v2 = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 10,
            column = 5,
            message = "Different message",
            severity = RuleSeverity.ERROR
        )

        val v3 = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 20,
            column = 5,
            message = "Test violation",
            severity = RuleSeverity.WARNING
        )

        assertTrue(Violation.isSameViolation(v1, v2)) // Same rule, file, line, column
        assertFalse(Violation.isSameViolation(v1, v3)) // Different line
    }

    @Test
    fun `should support null quickFix and externalFixPayload`() {
        val violation = Violation(
            rule = testRule,
            filePath = "/path/to/Test.java",
            line = 0,
            column = 0,
            message = "Test violation",
            severity = RuleSeverity.WARNING,
            quickFix = null,
            externalFixPayload = null
        )

        assertNull(violation.quickFix)
        assertNull(violation.externalFixPayload)
    }
}
