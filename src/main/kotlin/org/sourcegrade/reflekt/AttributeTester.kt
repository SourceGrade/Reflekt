package org.sourcegrade.reflekt

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class AttributeTester {
    private var classTester: ClassTester<*>? = null
    private var matcher: AttributeMatcher? = null
    private var field: Field? = null
    fun setMatcher(matcher: AttributeMatcher?): AttributeTester {
        this.matcher = matcher
        return this
    }

    fun setClassTester(classTester: ClassTester<*>?): AttributeTester {
        this.classTester = classTester
        return this
    }

    fun assureExists(): AttributeTester {
        Objects.requireNonNull(classTester, "no class tester defined")
        Objects.requireNonNull<AttributeMatcher>(matcher, "no matcher defined")
        field = classTester!!.resolveAttribute(matcher!!)
        return this
    }

    fun assertModifiers(): AttributeTester {
        if (matcher!!.modifier >= 0) {
            assertModifier(matcher!!.modifier, field)
        }
        return this
    }

    fun assertDeclaration(): AttributeTester {
        test()
            .add { assertModifiers() }
            .run(SHOW_ALL)
        return this
    }

    private fun assureAccessible(`object`: Any?) {
        if (!field!!.canAccess(if (Modifier.isStatic(field!!.modifiers)) null else `object`)) {
            assertDoesNotThrow { field!!.isAccessible = true }
        }
    }

    fun <T> getValue(): T {
        return getValue(null)
    }

    fun <T> getValue(`object`: Any?): T {
        assureAccessible(`object`)
        return assertDoesNotThrow { field!![`object`] } as T
    }

    fun <T> setValue(`object`: Any?, value: T) {
        assureAccessible(`object`)
        assertDoesNotThrow { field!![`object`] = value }
    }
}
