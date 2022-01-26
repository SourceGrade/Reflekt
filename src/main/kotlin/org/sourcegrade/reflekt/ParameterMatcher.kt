package org.sourcegrade.reflekt

import org.sourcegrade.reflekt.TestUtils
import java.util.Locale
import kotlin.Throws
import java.lang.ClassNotFoundException
import java.io.IOException
import kotlin.jvm.JvmOverloads
import org.sourcegrade.reflekt.IdentifierMatcher
import java.util.Arrays
import java.util.stream.Collectors
import java.util.function.Supplier
import org.sourcegrade.reflekt.ClassTester
import java.util.Objects
import org.sourcegrade.reflekt.AttributeMatcher
import org.sourcegrade.reflekt.ParameterMatcher
import org.sourcegrade.reflekt.MethodTester
import java.util.concurrent.ThreadLocalRandom
import java.lang.IllegalArgumentException
import java.lang.IllegalAccessException
import javax.management.RuntimeErrorException
import java.lang.reflect.InvocationTargetException
import java.lang.RuntimeException
import org.sourcegrade.reflekt.AttributeTester

/**
 * A Parameter Matcher based on [IdentifierMatcher]
 *
 * @author Ruben Deisenroth
 * @see IdentifierMatcher
 */
class ParameterMatcher : IdentifierMatcher {
    /**
     * The expected parameter type
     */
    var parameterType: Class<*>

    /**
     * Whether or not to Allow Parameters derived from [.parameterType]
     */
    var allowSubTypes = true

    /**
     * Generates a new [ParameterMatcher]
     *
     * @param identifierName The Name to match
     * @param similarity     The Minimum similarity required
     * @param parameterType  The expected parameter type
     * @param allowSubTypes  Whether or not to Allow Parameters derived from
     * [.parameterType]
     */
    constructor(identifierName: String?, similarity: Double, parameterType: Class<*>, allowSubTypes: Boolean) : super(
        identifierName,
        similarity
    ) {
        this.parameterType = parameterType
        this.allowSubTypes = allowSubTypes
    }

    /**
     * Generates a new [ParameterMatcher]
     *
     * @param identifierName The Name to match
     * @param similarity     The Minimum similarity required
     * @param parameterType  The expected parameter type
     */
    constructor(identifierName: String?, similarity: Double, parameterType: Class<*>) : super(
        identifierName,
        similarity
    ) {
        this.parameterType = parameterType
    }

    /**
     * Generates a new [ParameterMatcher]
     *
     * @param parameterType The expected parameter type
     */
    constructor(parameterType: Class<*>) : this(null, 0.0, parameterType) {}

    /**
     * Generates a new [ParameterMatcher]
     *
     * @param parameterType The expected parameter type
     * @param allowSubTypes Whether or not to Allow Parameters derived from
     * [.parameterType]
     */
    constructor(parameterType: Class<*>, allowSubTypes: Boolean) : this(null, 0.0, parameterType, allowSubTypes) {}
}
