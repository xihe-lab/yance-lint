package com.xihe_lab.yance.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * FixType 枚举单元测试
 */
class FixTypeTest {

    @Test
    fun `should have correct fix types`() {
        val fixTypes = FixType.entries
        assertEquals(2, fixTypes.size)

        assertTrue(fixTypes.contains(FixType.PSI_QUICK_FIX))
        assertTrue(fixTypes.contains(FixType.GUTTER_ACTION))
    }

    @Test
    fun `PSI_QUICK_FIX should be for native Alt+Enter`() {
        assertEquals("PSI_QUICK_FIX", FixType.PSI_QUICK_FIX.toString())
    }

    @Test
    fun `GUTTER_ACTION should be for external tools`() {
        assertEquals("GUTTER_ACTION", FixType.GUTTER_ACTION.toString())
    }
}
