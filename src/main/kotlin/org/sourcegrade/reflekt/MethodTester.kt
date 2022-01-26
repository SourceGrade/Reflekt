package org.sourcegrade.reflekt

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.mockito.invocation.Invocation
import spoon.reflect.code.CtInvocation
import spoon.reflect.declaration.CtElement
import spoon.reflect.declaration.CtMethod
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import kotlin.math.max
import kotlin.math.min

/**
 * A Method Tester
 *
 * @author Ruben Deisenroth
 */
class MethodTester @JvmOverloads constructor(
    classTester: ClassTester<*>?,
    methodName: String?,
    similarity: Double = 1.0,
    accessModifier: Int = -1,
    returnType: Class<*>? = null,
    parameters: List<ParameterMatcher>? = null,
    allowSuperClass: Boolean = false,
) {
    /**
     * whether to also match super implementations
     */
    var allowSuperClass = false
    /**
     * returns the Value of [.methodIdentifier]
     *
     * @return the Value of [.methodIdentifier]
     */
    /**
     * Set [.methodIdentifier] to the given value
     */
    /**
     * The Method-Identifier
     */
    var methodIdentifier: IdentifierMatcher? = null
    /**
     * returns the Value of [.theMethod]
     *
     * @return the Value of [.theMethod]
     */
    /**
     * Set [.theMethod] to the given value
     */
    /**
     * The resolved Method that will be tested
     */
    var theMethod: Method? = null
    /**
     * Returns the Value of [.accessModifier]
     *
     * @return the Value of [.accessModifier]
     */
    /**
     * Sets [.accessModifier] to the given Value
     *
     * @param accessModifier the new Access Modifier
     */
    /**
     * The Expected Access Modifier
     */
    var accessModifier = 0
    /**
     * returns the Value of [.returnType]
     *
     * @return the Value of [.returnType]
     */
    /**
     * Set [.returnType] to the given value
     */
    /**
     * The expected return Type
     */
    var returnType: Class<*>? = null
    /**
     * returns the Value of [.parameters]
     *
     * @return the Value of [.parameters]
     */
    /**
     * Set [.parameters] to the given value
     */
    /**
     * The expected parameters
     */
    var parameters: java.util.ArrayList<ParameterMatcher>? = null
    /**
     * returns the Value of [.classTester]
     *
     * @return the Value of [.classTester]
     */
    /**
     * Set [.classTester] to the given value
     *
     * @param classTester the new Class Tester
     */
    /**
     * A Class Tester (used for invoking)
     */
    var classTester: ClassTester<*>? = null
    /**
     * returns true if [.looseReturnTypeChecking] is true
     *
     * @return true if [.looseReturnTypeChecking] is true
     */
    /**
     * Allow or disallow Loose return Type Checking
     *
     * @param looseReturnTypeChecking the new Rule
     */
    /**
     * Whether to allow derived return Types
     */
    var isLooseReturnTypeChecking = false
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester    A Class Tester (used for invoking)
     * @param methodName     the expected method name
     * @param similarity     the minimum matching similarity
     * @param accessModifier The Expected Access Modifier
     * @param returnType     The expected return Type
     * @param parameters     The expected parameters
     */
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester    A Class Tester (used for invoking)
     * @param methodName     the expected method name
     * @param similarity     the minimum matching similarity
     * @param accessModifier The Expected Access Modifier
     * @param returnType     The expected return Type
     * @param parameters     The expected parameters
     */
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester    A Class Tester (used for invoking)
     * @param methodName     the expected method name
     * @param similarity     the minimum matching similarity
     * @param accessModifier The Expected Access Modifier
     */
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester    A Class Tester (used for invoking)
     * @param methodName     the expected method name
     * @param similarity     the minimum matching similarity
     * @param accessModifier The Expected Access Modifier
     * @param returnType     The expected return Type
     */
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester A Class Tester (used for invoking)
     * @param methodName  the expected method name
     */
    /**
     * Generates a new [MethodTester]
     *
     * @param classTester A Class Tester (used for invoking)
     * @param methodName  the expected method name
     * @param similarity  the minimum matching similarity
     */
    init {
        this.classTester = classTester
        methodIdentifier = IdentifierMatcher(methodName, similarity)
        this.accessModifier = accessModifier
        this.returnType = returnType
        this.parameters = java.util.ArrayList(parameters)
        this.allowSuperClass = allowSuperClass
    }

    /**
     * Generates a new [MethodTester]
     *
     * @param classTester             A Class Tester (used for invoking)
     * @param methodName              the expected method name
     * @param similarity              the minimum matching similarity
     * @param accessModifier          The Expected Access Modifier
     * @param returnType              The expected return Type
     * @param parameters              The expected parameters
     * @param looseReturnTypeChecking whether or not to allow Derived return Types
     */
    constructor(
        classTester: ClassTester<*>?,
        methodName: String?,
        similarity: Double,
        accessModifier: Int,
        returnType: Class<*>?,
        parameters: java.util.ArrayList<ParameterMatcher>?,
        allowSuperClass: Boolean,
        looseReturnTypeChecking: Boolean,
    ) : this(classTester, methodName, similarity, accessModifier, returnType, parameters, allowSuperClass) {
        isLooseReturnTypeChecking = looseReturnTypeChecking
    }

    /**
     * Generates a new [MethodTester]
     *
     * @param classTester A Class Tester (used for invoking)
     * @param methodName  the expected method name
     * @param similarity  the minimum matching similarity
     * @param returnType  The expected return Type
     * @param parameters  The expected parameters
     */
    constructor(
        classTester: ClassTester<*>?,
        methodName: String?,
        similarity: Double,
        returnType: Class<*>?,
        parameters: List<ParameterMatcher>,
    ) : this(classTester, methodName, similarity, -1, returnType, parameters)

    fun assertCorrectDeclaration() {
        assertMethodResolved()
        test().add { assertParametersMatch() }.add { assertReturnType() }.add { assertAccessModifier() }.run()
    }

    /**
     * asserts that the Return type matches the expected return Type
     */
    fun assertReturnType() {
        checkNotNull(returnType) { "Faulty Test: Cannot assert return type null" }
        assertMethodResolved()
        if (isLooseReturnTypeChecking) {
            assertInstanceOf(
                returnType, theMethod!!.returnType, getInvalidReturnTypeMessage(
                    methodIdentifier!!.identifierName
                )
            )
        } else {
            assertSame(
                returnType, theMethod!!.returnType, getInvalidReturnTypeMessage(
                    methodIdentifier!!.identifierName
                )
            )
        }
    }

    /**
     * Verifies that the Method was declared correctly
     *
     * @returns this
     */
    fun verify(): MethodTester {
        if (!methodResolved()) {
            resolveMethod()
        }
        if (accessModifier >= 0) {
            assertAccessModifier()
        }
        assertParametersMatch()
        assertReturnType()
        return this
    }

    /**
     * Adds expected Parameter Matchers to [.parameters]
     *
     * @param interfaceMatcher the Interface Metchers to add
     */
    fun addParameter(vararg interfaceMatcher: ParameterMatcher?) {
        if (parameters == null) {
            parameters = java.util.ArrayList()
        }
        parameters!!.addAll(listOf<ParameterMatcher>(*interfaceMatcher))
    }

    /**
     * Adds expected Parameter Matchers to [.parameters]
     *
     * @param type       The expected parameter type
     * @param name       The Name to match
     * @param similarity The Minimum similarity required
     */
    fun addParameter(type: Class<*>, name: String?, similarity: Double) {
        addParameter(ParameterMatcher(name, similarity, type))
    }

    /**
     * Adds expected Parameter Matchers to [.parameters]
     *
     * @param type The expected parameter type
     */
    fun addParameter(type: Class<*>) {
        addParameter(ParameterMatcher(null, 1, type))
    }

    /**
     * Generates a Method not found Message
     *
     * @return the generated Message
     */
    val methodNotFoundMessage: String
        get() = getMethodNotFoundMessage(methodIdentifier!!.identifierName)

    /**
     * returns `true` if [.theMethod] is not `null`
     *
     * @return `true` if [.theMethod] is not `null`
     */
    fun methodResolved(): Boolean {
        return theMethod != null
    }

    /**
     * Assert that the method is resolved
     */
    fun assertMethodResolved() {
        assertTrue(methodResolved(), methodNotFoundMessage)
    }

    /**
     * Assert that [.classTester] is not `null`
     */
    fun assertClassTesterNotNull() {
        assertNotNull(classTester, getClassTesterNullMessage(methodIdentifier!!.identifierName))
    }

    /**
     * returns `true` if [.classTester] is not
     * `null` and [ClassTester.class_resolved] returns true
     *
     * @return `true` if [.classTester] is not `null`
     */
    fun classResolved(): Boolean {
        return classTester != null && classTester!!.class_resolved()
    }

    /**
     * Asserts that [ClassTester.classInstance] is not `null`
     */
    fun assertClassResolved() {
        assertClassTesterNotNull()
        classTester!!.assertClassResolved()
    }

    /**
     * returns `true`, if the Method is invokable.
     *
     * <br></br>
     * To be exact: returns `true` if
     * [.classTester] [.theMethod] and
     * [ClassTester.classInstance] are
     * resolved
     *
     * @return returns `true`, if the Method is invokable.
     */
    fun invokeable(): Boolean {
        return classResolved() && classTester!!.classInstanceResolved() && methodResolved() && classTester!!.classInstanceResolved()
    }

    /**
     * Asserts that the Method is invokable.
     *
     *
     * To be exact: asserts that [.classTester] [.theMethod] and
     * [ClassTester.classInstance] are resolved
     */
    fun assertInvokeable() {
        assertClassResolved()
        classTester!!.assertClassInstanceResolved()
        assertMethodResolved()
    }

    /**
     * Invokes [.theMethod] using [.classTester]
     *
     * @param params the Parameters used for invoking
     * @return the Returned Value of the Method
     */
    operator fun <T> invoke(instance: Any?, vararg params: Any): T? {
        var instance = instance
        if (instance is Mocked) {
            instance = (instance as Mocked?).getActualObject()
        }
        for (i in params.indices) {
            if (params[i] is Mocked) {
                params[i] = (params[i] as Mocked).getActualObject()
            }
        }
        if (instance != null) {
            assertInvokeable()
        }
        assertDoesNotThrow({ theMethod!!.isAccessible = true }, "method could not be invoked")
        var returnValue: Any? = null
        try {
            returnValue = theMethod!!.invoke(instance, *params)
        } catch (e: IllegalAccessException) {
            if (e is InvocationTargetException && (e as InvocationTargetException).getTargetException() is RuntimeException) {
                throw (e as InvocationTargetException).getTargetException()
            }
            //            Arrays.stream(e.getStackTrace()).forEach(x -> Global.LOGGER.log(Level.WARN, x));
            fail("method could not be invoked", e)
        } catch (e: IllegalArgumentException) {
            if (e is InvocationTargetException && (e as InvocationTargetException).getTargetException() is RuntimeException) {
                throw (e as InvocationTargetException).getTargetException()
            }
            fail("method could not be invoked", e)
        } catch (e: InvocationTargetException) {
            if (e is InvocationTargetException && (e as InvocationTargetException).getTargetException() is RuntimeException) {
                throw (e as InvocationTargetException).getTargetException()
            }
            fail("method could not be invoked", e)
        }
        return returnValue as T?
    }

    fun <T> invokeStatic(vararg params: Any?): T {
        return invoke(null, *params)
    }

    /**
     * Gets the Invocations of the Method
     *
     * @return the Invocations of the Method
     */
    val invocations: List<Any>
        get() {
            assertMethodResolved()
            classTester!!.assertSpied()
            return classTester.getMockingDetails().getInvocations().stream().filter { x ->
                x.getMethod().getName().equals(
                    theMethod!!.name
                )
            }.collect(Collectors.toList())
        }

    // public boolean needsJavadoc() {
    // assertMethodResolved();
    // // theMethod.
    // }
    fun getInvocations(instance: Any): List<Invocation> {
        return invocations.stream().filter(Predicate { x: Invocation -> x.mock === instance })
            .collect(Collectors.toList<Any>())
    }

    /**
     * Gets the Invocation Count (How often Has the Method been invoked?)
     *
     * @return the Invocation Count
     */
    fun getInvocationCount(): Int {
        return invocations.size
    }

    fun assertInvoked() {
        if (getInvocationCount() == 0) {
            fail(wasNotCalledRecursively(methodIdentifier!!.identifierName))
        }
    }

    /**
     * Gets random Valid Parameter Values
     *
     * @return the Random Parameters
     */
    fun getRandomParams(): Array<Any> {
        return Arrays.stream<Parameter>(theMethod!!.parameters)
            .map<Any>(Function<Parameter, Any?> { x: Parameter -> ClassTester.Companion.getRandomValue(x.type) })
            .toArray()
    }

    /**
     * [.theMethod] using [.classTester] with Random parameters
     *
     * @return the Returned Value of the Method
     */
    fun invokeWithRandomParams(): Any {
        assertMethodResolved()
        return invoke<Any>(getRandomParams())!!
    }

    /**
     * Asserts the Return Value of an invokation with the given parameters
     *
     * @param expected          the expected Return value
     * @param additionalMessage an Additional Message Text
     * @param params            the Parameters used for invokation
     */
    fun assertReturnValueEquals(expected: Any?, additionalMessage: String, vararg params: Any?) {
        assertEquals(
            expected,
            invoke(params),
            "Falsche Rückgabe bei Methode" + methodIdentifier!!.identifierName + (if (params.size > 0) "mit Parameter(n):" + safeArrayToString(
                *params
            ) else "") + additionalMessage
        )
    }

    /**
     * Asserts that none of the Blacklisted Constructs were Used
     *
     * @param disallowedConstructs the Disallowed Constructs
     */
    fun assertConstructsNotUsed(disallowedConstructs: List<Class<out CtCodeElement?>?>) {
        val method: CtMethod<*> = assertCtMethodExists()
        val test: Unit = test()
        for (construct in disallowedConstructs) {
            if (!method.getElements(TypeFilter(construct)).isEmpty()) {
                test.add { fail(String.format("<%s> was used unexpectedly", construct.simpleName.substring(2))) }
            }
        }
        test.run()
    }

    fun assertConstructsUsed(disallowedConstructs: List<Class<out CtCodeElement?>?>) {
        val method: CtMethod<*> = assertCtMethodExists()
        val test: Unit = test()
        for (construct in disallowedConstructs) {
            if (method.getElements(TypeFilter(construct)).isEmpty()) {
                test.add { fail(String.format("<%s> was not used unexpectedly", construct.simpleName.substring(2))) }
            }
        }
        test.run()
    }

    fun assertCtMethodExists(): CtMethod<*> {
        assureResolved()
        val spoon: Launcher = assertDoesNotThrow(
            { classTester!!.assureSpoonLauncherModelsBuild().spoon },
            "Could not Create Spoon Launcher"
        )
        val type: CtType<*> = assertDoesNotThrow(
            { spoon.getModel().getAllTypes().stream().filter(CtType::isTopLevel).findFirst().orElseThrow() },
            "Could not resolve Class Source for Class " + classTester.getClassIdentifier().identifierName + "." + "available Class Sources:" + spoon.getModel()
                .getAllTypes().toString()
        )
        return assertDoesNotThrow({
            type.getMethodsByName(methodIdentifier!!.identifierName).stream().findFirst().orElseThrow()
        }, "Could not resolve Method Source for Method " + theMethod!!.name)
    }

    fun assertDirectlyRecursive() {
        assertRecursive(1)
    }

    fun assertRecursive(level: Int) {
        val m: CtMethod<*> = assertCtMethodExists()
        if (!isRecursive(m.directChildren, m, level)) {
            fail(String.format("method <%s> is not recursive", methodIdentifier!!.identifierName))
        }
    }

    fun assertNotDirectlyRecursive() {
        assertNotRecursive(1)
    }

    fun assertNotRecursive(level: Int) {
        val m: CtMethod<*> = assertCtMethodExists()
        if (isRecursive(m.directChildren, m, level)) {
            fail(String.format("method <%s> is recursive", methodIdentifier!!.identifierName))
        }
    }

    /**
     * Asserts the Return Value of an invokation with the given parameters
     *
     * @param expected the expected Return value
     * @param params   the Parameters used for invokation
     */
    fun assertReturnValueEquals(expected: Any?, vararg params: Any?) {
        assertReturnValueEquals(expected, "", *params)
    }

    /**
     * Asserts the actual access Modifier matches [.accessModifier]
     */
    fun assertAccessModifier() {
//        disabled for this submbission
//        if (accessModifier >= 0) {
//            TestUtils.assertModifier(accessModifier, theMethod);
//        }
    }
    /**
     * Resolve the Method with tolerances
     *
     * <br></br>
     * The Method is first searched by name using using
     * [TestUtils.similarity]. If Multiple overloads are found
     * then the function with the most matching parameters according to
     * [.countMatchingParameters] is
     * chosen.
     *
     * @param theClass        The Class to search in
     * @param methodName      The expected Method name
     * @param similarity      The minimum required similarity
     * @param parameters      The expected Parameters
     * @param allowSuperClass whether to search in Super classes as well
     * @return the resolved Method
     * @see TestUtils.similarity
     * @see .countMatchingParameters
     */
    /**
     * Resolve the Method with tolerances
     *
     * <br></br>
     * The Method is first searched by name using using
     * [TestUtils.similarity]. If Multiple overloads are found
     * then the function with the most matching parameters according to
     * [.countMatchingParameters] is
     * chosen.
     *
     * @param theClass   The Class to search in
     * @param methodName The expected Method name
     * @param similarity The minimum required similarity
     * @param parameters The expected Parameters
     * @return the resolved Method
     * @see TestUtils.similarity
     * @see .countMatchingParameters
     */
    @JvmOverloads
    fun resolveMethod(
        theClass: Class<*>?,
        methodName: String?,
        similarity: Double,
        parameters: java.util.ArrayList<ParameterMatcher>?,
        allowSuperClass: Boolean = false,
    ): Method {
        var similarity = similarity
        similarity = max(0.0, min(similarity, 1.0))
        ClassTester.Companion.assertClassNotNull(theClass, "zu Methode $methodName")
        val methods =
            if (allowSuperClass) getAllMethods(theClass) else java.util.ArrayList<Method>(listOf<Method>(*theClass!!.declaredMethods))
        var bestMatch = methods.stream().sorted { x: Method?, y: Method? ->
            java.lang.Double.valueOf(TestUtils.similarity(methodName, y!!.name))
                .compareTo(TestUtils.similarity(methodName, x!!.name))
        }
            .findFirst().orElse(null)
        assertMethodNotNull(bestMatch, methodName)
        val sim = TestUtils.similarity(bestMatch!!.name, methodName)
        assertTrue(
            sim >= similarity,
            methodNotFoundMessage + "Ähnlichster Methodenname:" + bestMatch.name + " with " + sim + " similarity."
        )
        if (parameters != null) {
            // Account for overloads
            val matches = methods.stream().filter { x: Method? -> TestUtils.similarity(methodName, x!!.name) == sim }
                .collect(
                    Collectors.toCollection<Method, java.util.ArrayList<Method>>(
                        Supplier<java.util.ArrayList<Method>> { ArrayList() })
                )
            if (matches.size > 1) {
                // Find Best match according to parameter options
                bestMatch = matches.stream().sorted { x: Method?, y: Method? ->
                    Integer.valueOf(countMatchingParameters(y, methodName, parameters, true)).compareTo(
                        countMatchingParameters(x, methodName, parameters, true)
                    )
                }
                    .findFirst().orElse(null)
            }
        }
        return bestMatch.also { theMethod = it }
    }

    /**
     * Resolve the Method with tolerances
     *
     * <br></br>
     * The Method is first searched by name using using
     * [TestUtils.similarity]. If Multiple overloads are found
     * then the function with the most matching parameters according to
     * [.countMatchingParameters] is
     * chosen.
     *
     * @return the resolved Method
     * @see TestUtils.similarity
     * @see .countMatchingParameters
     */
    fun resolveMethod(): Method {
        assertClassTesterNotNull()
        if (!classResolved()) {
            classTester!!.resolveClass()
        }
        return resolveMethod(
            classTester!!.theClass,
            methodIdentifier!!.identifierName,
            methodIdentifier!!.similarity,
            parameters,
            allowSuperClass
        )
    }

    /**
     * Assures that the Method has been resolved
     *
     * @return the Method
     */
    fun assureResolved(): MethodTester {
        if (!methodResolved()) {
            resolveMethod()
        }
        return this
    }
    /**
     * Gets Method Documentation for JavaDoc
     *
     * @param d the Source Documentation
     * @return the Method Documentation
     */
    //    public MethodDocumentation getMethodDocumentation(SourceDocumentation d) {
    //        try {
    //            classTester.assureClassResolved();
    //            var resolvedMethod = assureMethodResolved().getTheMethod();
    //            return d.forTopLevelType(classTester.getTheClass().getName()).forMethod(
    //                resolvedMethod.getName(), resolvedMethod.getParameterTypes());
    //        } catch (Throwable e) {
    //            return d.forTopLevelType("").forMethod("");
    //        }
    //    }
    /**
     * Resolve the Method with tolerances
     *
     * <br></br>
     * The Method is first searched by name using using
     * [TestUtils.similarity]. If Multiple overloads are found
     * then the function with the most matching parameters according to
     * [.countMatchingParameters] is
     * chosen.
     *
     * @param similarity The minimum required similarity
     * @return the resolved Method
     * @see TestUtils.similarity
     * @see .countMatchingParameters
     */
    fun resolveMethod(similarity: Double): Method {
        return resolveMethod(classTester!!.theClass, methodIdentifier!!.identifierName, similarity, parameters)
    }

    companion object {
        /**
         * Generates a Message for an invalid return type
         *
         * @param methodName the Method name
         * @return the generated Message
         */
        fun getInvalidReturnTypeMessage(methodName: String?): String {
            return String.format("falscher Rückgabetyp für Methode %s", methodName)
        }

        /**
         * Generates a Should Not Have Parameter Message
         *
         * @param methodName the Method name
         * @return the generated Message
         */
        fun getShouldNotHaveParameterMessage(methodName: String?): String {
            return String.format("Methode %s sollte keine Parameter haben.", methodName)
        }

        /**
         * Counts the matching Parameters
         *
         * @param expectedParametes the Expected Parameter List
         * @param actualParameters  the Actual Parameter List
         * @param ignoreNames       whether to ignore Parameter Names
         * @return the Amount of matching Parameters
         */
        fun countMatchingParameters(
            expectedParametes: java.util.ArrayList<ParameterMatcher>,
            actualParameters: java.util.ArrayList<Parameter>,
            ignoreNames: Boolean,
        ): Int {
            var count = 0
            for (i in expectedParametes.indices) {
                val matcher = expectedParametes[i]
                val param = actualParameters[i]
                if (param.type != matcher.parameterType) {
                    continue
                }
                if (!ignoreNames && matcher.identifierName != null && matcher.similarity > 0) {
                    if (TestUtils.similarity(matcher.identifierName, param.name) < matcher.similarity) {
                        continue
                    }
                }
                count++
            }
            return count
        }

        /**
         * Counts the matching Parameters
         *
         * @param m           The Method to verify
         * @param methodName  The expected Method name
         * @param parameters  the Expected Parameter List
         * @param ignoreNames whether to ignore Parameter Names
         * @return the Amount of matching Parameters
         */
        fun countMatchingParameters(
            m: Method?,
            methodName: String?,
            parameters: java.util.ArrayList<ParameterMatcher>?,
            ignoreNames: Boolean,
        ): Int {
            assertMethodNotNull(m, methodName)
            return if (parameters == null || parameters.isEmpty()) {
                0
            } else countMatchingParameters(
                parameters,
                java.util.ArrayList(listOf(*m!!.parameters)),
                ignoreNames
            )
        }

        /**
         * assert that the Method Parameters match
         *
         * @param expectedParameters the expected Parameter List
         * @param actualParamters    the actual Parameter List
         * @param ignoreNames        whether to ignore Parameter Names
         */
        fun assertParametersMatch(
            expectedParameters: java.util.ArrayList<ParameterMatcher>?,
            actualParamters: java.util.ArrayList<Parameter>?,
            ignoreNames: Boolean,
        ) {
            if (expectedParameters == null || expectedParameters.isEmpty()) {
                assertTrue(
                    actualParamters == null || actualParamters.isEmpty(),
                    "Es sollen keine Parameter vorhanden sein."
                )
            } else {
                for (i in expectedParameters.indices) {
                    val matcher = expectedParameters[i]
                    assertTrue(i < actualParamters!!.size, "Zu wenige Parameter.")
                    val param = actualParamters!![i]
                    // TODO fix assertions for sub-types
                    assertSame(matcher.parameterType, param.type, "Falscher Parametertyp an Index " + "i.")
                    if (!ignoreNames && param.isNamePresent && matcher.identifierName != null && matcher.similarity > 0) {
                        assertTrue(
                            TestUtils.similarity(matcher.identifierName, param.name) >= matcher.similarity,
                            "Falscher Parametername. Erwartet: " + matcher.identifierName + ", Erhalten: " + param.name
                        )
                    }
                }
                assertEquals(
                    actualParamters!!.size,
                    expectedParameters.size,
                    "Die folgenden Parameter waren nicht gefrdert:" + actualParamters.subList(
                        expectedParameters.size,
                        actualParamters.size
                    )
                )
            }
        }
        /**
         * assert that the Method Parameters match
         *
         * @param m           The Method to verify
         * @param methodName  The expected Method name
         * @param parameters  the Expected Parameter List
         * @param ignoreNames whether to ignore Parameter Names
         */
        /**
         * assert that the Method Parameters match with [.parameters]
         */
        @JvmOverloads
        fun assertParametersMatch(
            m: Method = theMethod,
            methodName: String? = methodIdentifier.identifierName,
            parameters: java.util.ArrayList<ParameterMatcher>? = parameters,
            ignoreNames: Boolean = false,
        ) {
            assertMethodNotNull(m, methodName)
            assertParametersMatch(parameters, java.util.ArrayList(listOf(*m.parameters)), ignoreNames)
        }

        /**
         * Generates a Method not found Message
         *
         * @param methodName the expecteed Method name
         * @return the generated Message
         */
        fun getMethodNotFoundMessage(methodName: String?): String {
            return String.format("Methode %s existiert nicht.", methodName)
        }

        /**
         * Assert that a given method is not `null`
         *
         * @param m    the Method
         * @param name the expected Method name
         */
        fun assertMethodNotNull(m: Method?, name: String?) {
            assertNotNull(m, getMethodNotFoundMessage(name))
        }

        /**
         * Generates a Class tester null message
         *
         * @param methodName the expected Method name
         * @return the generated message
         */
        fun getClassTesterNullMessage(methodName: String?): String {
            return String.format("Fehlerhafter Test für Methode %s: Kein Klassentester gegeben.", methodName)
        }

        fun safeArrayToString(vararg array: Any?): String {
            var paramsString = "[]"
            if (array != null) {
                try {
                    paramsString = array.contentToString()
                } catch (e: Exception) {
                    Arrays.stream<Any>(array)
                        .map<String>(Function { x: Any -> x.javaClass.name + "@" + Integer.toHexString(x.hashCode()) })
                        .collect(Collectors.joining(", ", "[", "]"))
                }
            }
            return paramsString
        }

        /**
         * Gets all Fields from a given Class and its superclasses recursively
         *
         * @param methods the fields so far (initially give it new ArrayList<>())
         * @param clazz   the Class to search
         * @return all Fields from a given Class and its superclasses recursively
         */
        private fun getAllMethods(
            methods: java.util.ArrayList<Method?>,
            clazz: Class<*>?,
        ): java.util.ArrayList<Method?> {
            methods.addAll(listOf<Method>(*clazz!!.declaredMethods))
            if (clazz.superclass != null) {
                getAllMethods(methods, clazz.superclass)
            }
            return methods
        }

        /**
         * Gets all Fields from a given Class and its superclasses recursively
         *
         * @param clazz the Class to search
         * @return all Fields from a given Class and its superclasses recursively
         */
        fun getAllMethods(clazz: Class<*>?): java.util.ArrayList<Method?> {
            return getAllMethods(java.util.ArrayList(), clazz)
        }

        fun isRecursive(elements: List<CtElement?>, methodToCall: CtMethod<*>, level: Int): Boolean {
            if (level <= 0) {
                return false
            }
            for (e in elements) {
                if (e is CtInvocation<*>) {
                    val method: CtInvocation<*> = e
                    if (method.executable.equals(methodToCall.reference)) {
                        return true
                    }
                    if (isRecursive(e.getDirectChildren(), methodToCall, level - 1)) {
                        return true
                    }
                } else if (isRecursive(e.getDirectChildren(), methodToCall, level)) {
                    return true
                }
            }
            return false
        }
    }
}
