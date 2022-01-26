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
 * An Attribute Matcher based on [IdentifierMatcher]
 *
 * @author Ruben Deisenroth
 * @see IdentifierMatcher
 */
class AttributeMatcher @JvmOverloads constructor(
    name: String?,
    similarity: Double,
    modifier: Int,
    type: Class<*>?,
    allowSuperClass: Boolean = false
) : IdentifierMatcher(name, similarity) {
    /**
     * The expected Access Modifier
     */
    var modifier = 0

    /**
     * The Expected Attribute Type
     */
    var type: Class<*>? = null

    /**
     * whether to also match super implementations
     */
    var allowSuperClass = false
    /**
     * Generates a new [AttributeMatcher]
     *
     * @param name            The Name to match
     * @param similarity      The Minimum similarity required
     * @param modifier        The expected Access Modifier
     * @param type            The Expected Attribute Type
     * @param allowSuperClass whether to also match super implementations
     */
    /**
     * Generates a new [AttributeMatcher]
     *
     * @param name       The Name to match
     * @param similarity The Minimum similarity required
     * @param modifier   The expected Access Modifier
     * @param type       The Expected Attribute Type
     */
    init {
        this.modifier = modifier
        this.type = type
        this.allowSuperClass = allowSuperClass
    }

    /**
     * Generates a new [AttributeMatcher]
     *
     * @param name            The Name to match
     * @param similarity      The Minimum similarity required
     * @param type            The Expected Attribute Type
     * @param allowSuperClass whether to also match super implementations
     */
    constructor(name: String?, similarity: Double, type: Class<*>?, allowSuperClass: Boolean) : this(
        name,
        similarity,
        -1,
        type,
        allowSuperClass
    ) {
    }

    /**
     * Generates a new [AttributeMatcher]
     *
     * @param name       The Name to match
     * @param similarity The Minimum similarity required
     * @param type       The Expected Attribute Type
     */
    constructor(name: String?, similarity: Double, type: Class<*>?) : this(name, similarity, -1, type) {}
}
