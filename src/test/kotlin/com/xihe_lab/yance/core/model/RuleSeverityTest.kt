package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * RuleSeverity 枚举单元测试
 */
class RuleSeverityTest {

    @Test
    fun `should have correct levels`() {
        assertEquals(3, RuleSeverity.ERROR.level)
        assertEquals(2, RuleSeverity.WARNING.level)
        assertEquals(1, RuleSeverity.INFO.level)
    }

    @Test
    fun `should have correct labels`() {
        assertEquals("Error", RuleSeverity.ERROR.label)
        assertEquals("Warning", RuleSeverity.WARNING.label)
        assertEquals("Info", RuleSeverity.INFO.label)
    }

    @Test
    fun `severity level ERROR should be greater than WARNING`() {
        assertTrue(RuleSeverity.ERROR.level > RuleSeverity.WARNING.level)
    }

    @Test
    fun `severity level WARNING should be greater than INFO`() {
        assertTrue(RuleSeverity.WARNING.level > RuleSeverity.INFO.level)
    }

    @Test
    fun `should find severity by label`() {
        assertEquals(RuleSeverity.ERROR, RuleSeverity.fromLabel("Error"))
        assertEquals(RuleSeverity.ERROR, RuleSeverity.fromLabel("ERROR"))
        assertEquals(RuleSeverity.WARNING, RuleSeverity.fromLabel("Warning"))
        assertEquals(RuleSeverity.WARNING, RuleSeverity.fromLabel("WARNING"))
        assertEquals(RuleSeverity.INFO, RuleSeverity.fromLabel("Info"))
        assertEquals(RuleSeverity.INFO, RuleSeverity.fromLabel("INFO"))
    }

    @Test
    fun `should return null for non-existent severity`() {
        assertNull(RuleSeverity.fromLabel("NON_EXISTENT"))
        assertNull(RuleSeverity.fromLabel(""))
    }
}
