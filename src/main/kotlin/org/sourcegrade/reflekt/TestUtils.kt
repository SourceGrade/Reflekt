package org.sourcegrade.reflekt

import com.google.common.reflect.ClassPath
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.math.min

/**
 * Test Utilities by Ruben
 *
 * @author Ruben Deisenroth
 */
object TestUtils {
    const val BRIDGE = 0x00000040
    const val VARARGS = 0x00000080
    const val SYNTHETIC = 0x00001000
    const val ANNOTATION = 0x00002000
    const val ENUM = 0x00004000
    const val MANDATED = 0x00008000

    /**
     * Asserts matching Modifiers
     *
     * @param expected Erwarteter Wert
     * @param actual   Eigentlicher Wert
     * @param name     Feld Name
     */
    fun assertModifier(expected: Int, actual: Int, name: String?) {
        if (expected < 0) {
            return
        }
        assertEquals(
            expected, actual, String.format(
                "incorrect modifiers for %s", name,
                Modifier.toString(expected), Modifier.toString(actual)
            )
        )
    }

    /**
     * Asserts matching Modifiers
     *
     * @param expected Erwarteter Wert
     * @param clazz    Klasse mit Modifier
     */
    fun assertModifier(expected: Int, clazz: Class<*>) {
        assertModifier(expected, clazz.modifiers, "class < " + clazz.name + ">")
    }

    /**
     * Asserts matching Modifiers
     *
     * @param expected Erwarteter Wert
     * @param method   Methode mit Modifier
     */
    fun assertModifier(expected: Int, method: Method) {
        assertModifier(
            expected, method.modifiers,
            "method <" + method.declaringClass + "." + method.name + ">"
        )
    }

    /**
     * Asserts matching Modifiers
     *
     * @param expected    Erwarteter Wert
     * @param constructor Konstruktor mit Modifier
     */
    fun assertModifier(expected: Int, constructor: Constructor<*>) {
        assertModifier(
            expected, constructor.modifiers,
            "constructor <" + constructor.declaringClass + "." + constructor.name + ">"
        )
    }

    /**
     * Asserts matching Modifiers
     *
     * @param expected Erwarteter Wert
     * @param attribut Attribut mit Modifier
     */
    fun assertModifier(expected: Int, attribut: Field) {
        assertModifier(
            expected, attribut.modifiers,
            "attribute <" + attribut.declaringClass + "." + attribut.name + ">"
        )
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     *
     * @param s1 String 1
     * @param s2 String 2
     * @return the similarity
     */
    fun similarity(s1: String?, s2: String?): Double {
        var longer = s1
        var shorter = s2
        if (s1!!.length < s2!!.length) {
            longer = s2
            shorter = s1
        }
        val longerLength = longer!!.length
        return if (longerLength == 0) {
            1.0
            /* both strings are zero length */
        } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
        /*
         * // If you have Apache Commons Text, you can use it to calculate the edit
         * distance: LevenshteinDistance levenshteinDistance = new
         * LevenshteinDistance(); return (longerLength -
         * levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
         */
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     *
     * @param s1 string 1
     * @param s2 string 2
     * @return the calculated similarity (a number within 0 and 1) between two
     * strings.
     * @see http://rosettacode.org/wiki/Levenshtein_distance.Java
     */
    fun editDistance(s1: String?, s2: String?): Int {
        var s1 = s1
        var s2 = s2
        s1 = s1!!.lowercase(Locale.getDefault())
        s2 = s2!!.lowercase(Locale.getDefault())
        val costs = IntArray(s2.length + 1)
        for (i in 0..s1.length) {
            var lastValue = i
            for (j in 0..s2.length) {
                if (i == 0) {
                    costs[j] = j
                } else {
                    if (j > 0) {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) {
                            newValue = min(min(newValue, lastValue), costs[j]) + 1
                        }
                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }
            if (i > 0) {
                costs[s2.length] = lastValue
            }
        }
        return costs[s2.length]
    }

    /**
     * Scans all classes accessible from the context class loader which belong to
     * the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException if the Classes were defined Faulty
     * @throws IOException            if an IO Exception occurs
     */
    @Throws(ClassNotFoundException::class, IOException::class)
    fun getClasses(packageName: String?): Array<Class<*>> {
        val cycle: Unit = TestCycleResolver.getTestCycle()
        return if (cycle != null) {
            // Autograder Run
            cycle.getSubmission().getClassNames().stream()
                .map { x -> assertDoesNotThrow { cycle.getClassLoader().loadClass(x) } }.toArray { _Dummy_.__Array__() }
        } else {
            // Regular Junit Run
            val loader = Thread.currentThread().contextClassLoader
            ClassPath.from(loader).getTopLevelClasses(packageName).stream().map { x -> x.load() }
                .toArray { _Dummy_.__Array__() }
        }
    }

    /**
     * Returns `true` if [A.getTestCycle]
     * does not return `null`
     *
     * @return `true` if [A.getTestCycle]
     * does not return `null`
     */
    val isAutograderRun: Boolean
        get() = TestCycleResolver.getTestCycle() != null
}
