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
 * An Identifier Matcher
 *
 * @author Ruben Deisenroth
 */
open class IdentifierMatcher
/**
 * Creates a new [IdentifierMatcher]
 *
 * @param identifierName The Name to match
 * @param packageName    The package Name
 * @param similarity     The Minimum similarity required
 */(
    /**
     * The Name to match
     */
    var identifierName: String?,
    /**
     * The package Name
     */
    var packageName: String?,
    /**
     * The Minimum similarity required
     */
    var similarity: Double
) {
    /**
     * Creates a new [IdentifierMatcher]
     *
     * @param identifierName The Name to match
     * @param similarity     The Minimum similarity required
     */
    constructor(identifierName: String?, similarity: Double) : this(identifierName, null, similarity) {}
}
