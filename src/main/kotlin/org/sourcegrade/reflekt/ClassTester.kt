package org.sourcegrade.reflekt

import net.bytebuddy.ByteBuddy
import org.junit.jupiter.api.Assertions.assertFalse
import java.lang.Exception
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Stream

/**
 * A Class Tester
 *
 * @author Ruben Deisenroth
 */
class ClassTester<T> @JvmOverloads constructor(
    packageName: String?,
    className: String?,
    similarity: Double = 1.0,
    accessModifier: Int = -1,
    superClass: Class<in T>? = null,
    implementsInterfaces: List<IdentifierMatcher>? = java.util.ArrayList(),
    classInstance: T? = null
) {
    /**
     * returns the Value of [.classIdentifier]
     *
     * @return the Value of [.classIdentifier]
     */
    /**
     * Sets [.classIdentifier] to the given Value
     *
     * @param classIdentifier the new Class Identifier
     */
    /**
     * The Class Identifier (Containing Name, Similarity)
     */
    var classIdentifier: IdentifierMatcher
    /**
     * Gets the Class if Already resolved
     *
     * @return the Class if Already resolved
     */
    /**
     * Sets the Class
     *
     * @param theClass the new Class
     */
    /**
     * The resolved Class that will be tested
     */
    var theClass: Class<T>? = null
    /**
     * Gets the Value of [.accessModifier]
     *
     * @return the Value of [.accessModifier]
     */
    /**
     * Sets [.accessModifier] to the given Modifier
     *
     * @param accessModifier the new Modifier
     */
    /**
     * The Expected Access Modifier
     */
    var accessModifier: Int
    /**
     * Gets the Value of [.classInstance]
     *
     * @return the Value of [.classInstance]
     */
    /**
     * The Class Instance of the Class Being Tested
     */
    var classInstance: T?
    /**
     * Gets the Super Class
     *
     * @return the super class
     */
    /**
     * Sets the Super Class
     *
     * @param superClass the Super Class
     */
    /**
     * The Expected Super class
     */
    var superClass: Class<in T>?
    /**
     * Gets the Implemented Interfaces
     *
     * @return the Implemented Interfaces
     */
    /**
     * Sets the Implemented Interfaces
     *
     * @param implementsInterfaces the new Implemented Interfaces
     */
    /**
     * Matchers for the Interfaces that are expected to be implemented
     */
    var implementsInterfaces: java.util.ArrayList<IdentifierMatcher>?

    /**
     * The Spoon Launcher
     */
    private var spoon: Launcher? = Launcher()
    /**
     * Creates a new [ClassTester]
     *
     * @param packageName          the Package Name of the Class
     * @param className            The Class Name
     * @param similarity           The Maximum Name Similarity for matching
     * @param accessModifier       The Expected Access Modifier
     * @param superClass           The Expected Super Class
     * @param implementsInterfaces Matchers for the Interfaces that are expected to
     * be implemented
     * @param classInstance        The Class Instance of the Class Being Tested
     */
    /**
     * Creates a new [ClassTester]
     *
     * @param packageName the Package Name of the Class
     * @param className   The Class Name
     */
    /**
     * Creates a new [ClassTester]
     *
     * @param packageName the Package Name of the Class
     * @param className   The Class Name
     * @param similarity  The Maximum Name Similarity for matching
     */
    /**
     * Creates a new [ClassTester]
     *
     * @param packageName    the Package Name of the Class
     * @param className      The Class Name
     * @param similarity     The Maximum Name Similarity for matching
     * @param accessModifier The Expected Access Modifier
     */
    /**
     * Creates a new [ClassTester]
     *
     * @param packageName          the Package Name of the Class
     * @param className            The Class Name
     * @param similarity           The Maximum Name Similarity for matching
     * @param accessModifier       The Expected Access Modifier
     * @param superClass           The Expected Super Class
     * @param implementsInterfaces Matchers for the Interfaces that are expected to
     * be implemented
     */
    init {
        classIdentifier = IdentifierMatcher(className, packageName, similarity)
        this.accessModifier = accessModifier
        this.superClass = superClass
        this.implementsInterfaces = java.util.ArrayList(implementsInterfaces)
        this.classInstance = classInstance
    }

    constructor(
        packageName: String?, className: String?, similarity: Double, accessModifier: Int,
        implementsInterfaces: List<IdentifierMatcher>?
    ) : this(packageName, className, similarity, accessModifier, Any::class.java, implementsInterfaces, null) {
    }

    constructor(clazz: Class<T>) : this(
        clazz.packageName,
        clazz.simpleName,
        0.8,
        clazz.modifiers,
        clazz.superclass,
        Arrays.stream<Class<*>>(clazz.interfaces)
            .map<IdentifierMatcher>(Function<Class<*>, IdentifierMatcher> { x: Class<*> ->
                IdentifierMatcher(
                    x.simpleName,
                    x.packageName,
                    0.8
                )
            })
            .collect<java.util.ArrayList<IdentifierMatcher>, Any>(
                Collectors.toCollection<IdentifierMatcher, java.util.ArrayList<IdentifierMatcher>>(
                    Supplier<java.util.ArrayList<IdentifierMatcher>> { ArrayList() })
            ),
        null
    ) {
        theClass = clazz
    }

    /**
     * Adds an Interface Matcher to the [.implementsInterfaces] List
     *
     * @param interfaceMatcher the InterfaceMatcher
     */
    fun addImplementsInterface(interfaceMatcher: IdentifierMatcher) {
        if (implementsInterfaces == null) {
            implementsInterfaces = java.util.ArrayList()
        }
        implementsInterfaces!!.add(interfaceMatcher)
    }
    /**
     * Adds an Interface Matcher to the [.implementsInterfaces] List
     *
     * @param interfaceName the InterfaceMatcher
     * @param similarity    the Maximum similarity allowed
     */
    /**
     * Adds an Interface Matcher to the [.implementsInterfaces] List
     *
     * @param interfaceName the InterfaceMatcher
     */
    @JvmOverloads
    fun addImplementsInterface(interfaceName: String?, similarity: Double? = null) {
        addImplementsInterface(IdentifierMatcher(interfaceName, similarity!!))
    }

    /**
     * Gets the Spoon Launcher
     *
     * @return the Spoon Launcher
     */
    fun getSpoon(): Launcher? {
        return spoon
    }

    /**
     * Sets [.spoon] to the given Value
     *
     * @param spoon the new Spoon Launcher
     */
    fun setSpoon(spoon: Launcher?) {
        this.spoon = spoon
    }

    fun assureSpoonLauncherModelsBuild(): ClassTester<T> {
        assureResolved()
        if (spoon == null) {
            spoon = Launcher()
        }
        val allTypes: Unit = spoon.getModel().getAllTypes()
        if (allTypes == null || allTypes.isEmpty()) {
            val cycle: Unit = TestCycleResolver.getTestCycle()
            val sourceFileName = theClass!!.name.replace('.', '/') + ".java"
            val vf: VirtualFile? = null
            if (cycle != null) {
                val sourceFile: SourceFile = cycle.getSubmission().getSourceFile(sourceFileName)
                if (sourceFile == null) {
                    fail(String.format("file %s does not exist", sourceFileName))
                }
                spoon.addInputResource(
                    VirtualFile(
                        Objects.requireNonNull<Any>(sourceFile).getContent(),
                        sourceFileName
                    )
                )
            } else {
                spoon.addInputResource("../solution/src/main/java/$sourceFileName")
            }
            spoon.buildModel()
        }
        return this
    }

    /**
     * Gets all Fields from [.theClass] and its superclasses recursively
     *
     * @return all Fields from from [.theClass] and its superclasses
     * recursively
     */
    val allFields: java.util.ArrayList<Field?>
        get() = getAllFields(java.util.ArrayList(), theClass)

    /**
     * Resolves An Attribute with a given [AttributeMatcher].
     *
     * @param matcher the [AttributeMatcher]
     * @return the Attribute-[Field]
     */
    fun resolveAttribute(matcher: AttributeMatcher): Field? {
        assertClassResolved()
        val fields =
            if (matcher.allowSuperClass) getAllFields(theClass) else java.util.ArrayList<Field>(Arrays.asList<Field>(*theClass!!.declaredFields))
        val bestMatch = fields.stream()
            .sorted { x: Field?, y: Field? ->
                java.lang.Double.valueOf(
                    TestUtils.similarity(
                        y!!.name, matcher.identifierName
                    )
                )
                    .compareTo(TestUtils.similarity(x!!.name, matcher.identifierName))
            }
            .findFirst().orElse(null)
        if (bestMatch == null) {
            fail(String.format("attribute <%s> does not exist", matcher.identifierName))
        }
        val sim = TestUtils.similarity(bestMatch!!.name, matcher.identifierName)
        if (sim < matcher.similarity) {
            fail(String.format("attribute <%s> does not exist", matcher.identifierName))
        }
        return bestMatch
    }

    /**
     * Asserts that a given attribute has a getter
     *
     * @param attribute the Attribute-[Field]
     */
    fun assertHasGetter(attribute: Field, vararg parameters: ParameterMatcher?) {
        assertNotNull(attribute)

        // Method Declaration
        val methodTester = MethodTester(
            this,
            String.format(
                "get%s%s",
                attribute.name.substring(0, 1).uppercase(Locale.getDefault()), attribute.name.substring(1)
            ),
            0.8,
            Modifier.PUBLIC,
            attribute.type,
            java.util.ArrayList<ParameterMatcher>(Arrays.asList<ParameterMatcher>(*parameters))
        )
        methodTester.resolveMethod()
        methodTester.assertAccessModifier()
        methodTester.assertParametersMatch()
        methodTester.assertReturnType()

        // test with Value
        assertDoesNotThrow(
            { attribute.isAccessible = true },
            "Konnte nicht auf Attribut zugreifen:" + attribute.name
        )
        resolveInstance()
        val expectedReturnValue = getRandomValue(attribute.type)
        assertDoesNotThrow { attribute[classInstance] = expectedReturnValue }
        val returnValue = methodTester
            .invoke<Any>(
                Arrays.stream<ParameterMatcher>(parameters)
                    .map<Any>(Function { x: ParameterMatcher -> getRandomValue(x.parameterType) }).toArray()
            )
        assertEquals(expectedReturnValue, returnValue, "Falsche Rückgabe der Getter-Metode.")
    }

    /**
     * Asserts that a given attribute has a getter
     *
     * @param attribute the Attribute-[Field]
     * @param testValue the TestValue
     */
    fun assertHasSetter(attribute: Field, testValue: Any?) {
        // Method Declaration
        val methodTester = MethodTester(
            this, String.format(
                "set%s%s",
                attribute.name.substring(0, 1).uppercase(Locale.getDefault()), attribute.name.substring(1)
            ), 0.8,
            Modifier.PUBLIC, Void.TYPE, java.util.ArrayList(
                listOf(
                    ParameterMatcher(attribute.name, 0.8, attribute.type)
                )
            )
        ).verify()

        // test with Value
        methodTester!!.invoke<Any>(testValue)
        assertFieldEquals(attribute, testValue, "Falscher Wert durch Setter-Methode.")
    }

    /**
     * Asserts that a given attribute has a getter
     *
     * @param attribute the Attribute-[Field]
     */
    fun assertHasSetter(attribute: Field) {
        assertHasSetter(attribute, getRandomValue(attribute.type))
    }
    /**
     * asserts that all the interfaces described by the given matchers are being
     * extended
     *
     * @param implementsInterfaces the Interface-Matchers
     */
    /**
     * asserts that all the interfaces described by [.implementsInterfaces]
     * are being extended
     */
    @JvmOverloads
    fun assertImplementsInterfaces(implementsInterfaces: List<IdentifierMatcher>? = this.implementsInterfaces) {
        assertClassResolved()
        val interfaces = java.util.ArrayList(listOf(*theClass!!.interfaces))
        if (implementsInterfaces == null || implementsInterfaces.isEmpty()) {
            assertTrue(interfaces == null || interfaces.isEmpty(), "Es sollen keine Interfaces implementiert werden.")
        } else {
            for (i in implementsInterfaces.indices) {
                val matcher = implementsInterfaces[i]
                assertFalse(interfaces.isEmpty(), getInterfaceNotImplementedMessage(matcher.identifierName))
                val bestMatch = interfaces.stream()
                    .sorted { x: Class<*>?, y: Class<*>? ->
                        java.lang.Double
                            .valueOf(TestUtils.similarity(matcher.identifierName, y!!.simpleName))
                            .compareTo(TestUtils.similarity(matcher.identifierName, x!!.simpleName))
                    }
                    .findFirst().orElse(null)
                assertNotNull(bestMatch, getInterfaceNotImplementedMessage(matcher.identifierName))
                val sim = TestUtils.similarity(bestMatch!!.simpleName, matcher.identifierName)
                assertTrue(
                    sim >= matcher.similarity, getInterfaceNotImplementedMessage(matcher.identifierName)
                            + "Ähnlichstes Interface:" + bestMatch.simpleName + " with " + sim + " similarity."
                )
                interfaces.remove(bestMatch)
            }
            assertTrue(
                interfaces.isEmpty(),
                "Die folgenden Interfaces sollten nicht implementiert werden:$interfaces"
            )
        }
    }

    /**
     * asserts that [.theClass] does not extend any interfaces
     */
    fun assertDoesNotImplementAnyInterfaces() {
        assertImplementsInterfaces(null)
    }

    /**
     * Returns true if [.theClass] is not null
     *
     * @return true if [.theClass] is not null
     */
    fun class_resolved(): Boolean {
        return theClass != null
    }

    /**
     * Gets the [MockingDetails] of [.theClass]
     *
     * @return the [MockingDetails] of [.theClass]
     */
    val mockingDetails: MockingDetails
        get() = mockingDetails(null)

    /**
     * Returns true if [.theClass] is Mocked
     *
     * @return true if [.theClass] is Mocked
     * @see MockingDetails.isMock
     */
    fun is_mock(): Boolean {
        return classInstanceResolved() && mockingDetails(classInstance).isMock()
    }

    /**
     * Returns true if [.theClass] is statically Mocked
     *
     * @return true if [.theClass] is statically Mocked
     * @see MockingDetails.isMock
     */
    fun is_static_mock(): Boolean {
        return classInstanceResolved() && mockingDetails(classInstance).isMock() && classInstance is MockedStatic
    }

    /**
     * Returns true if [.theClass] is a Spy
     *
     * @return true if [.theClass] is a Spy
     * @see MockingDetails.isSpy
     */
    fun is_spy(): Boolean {
        return classInstanceResolved() && mockingDetails(classInstance).isSpy()
    }

    /**
     * Makes the class a Spy if not done already
     *
     * @return this
     */
    fun assureSpied(): ClassTester<T> {
        assertClassInstanceResolved()
        if (!is_spy()) {
            setClassInstance(spy(classInstance))
        }
        return this
    }

    /**
     * Makes the class a Mock if not one already
     *
     * @return this
     */
    fun assureMocked(): ClassTester<T> {
        assertClassResolved()
        if (!is_mock()) {
            setClassInstance(resolveInstance())
        }
        return this
    }

    /**
     * Makes the class a Static Mock if not one already
     *
     * @return this
     */
    fun assureMockedStatic(): ClassTester<T> {
        assertClassResolved()
        return if (!is_static_mock()) {
            resolveStatic()
        } else this
    }

    /**
     * Makes the class a Spy if not done already
     *
     * @return this
     */
    fun assertSpied(): ClassTester<T> {
        assertClassInstanceResolved()
        assertTrue(is_spy(), "Faulty Test: Class was not spied on")
        return this
    }

    /**
     * Generates a class not found Message
     *
     * @return a class not found Message
     */
    val classNotFoundMessage: String
        get() = getClassNotFoundMessage(classIdentifier.identifierName)

    /**
     * Asserts that [.theClass] is not `null` and fails with the propper
     * message if not resolved
     */
    fun assertClassResolved() {
        assertClassNotNull(theClass, classIdentifier.identifierName)
    }

    /**
     * Asserts that the Class is declared correctly.
     *
     * @return [ClassTester] this
     */
    fun verify(): ClassTester<T> {
        if (!class_resolved()) {
            resolveClass()
        }
        if (accessModifier >= 0) {
            // Class Type
            if (Modifier.isInterface(accessModifier)) {
                assertIsInterface()
            } else if (accessModifier and TestUtils.ENUM != 0) {
                assertIsEnum()
            } else {
                assertIsPlainClass()
            }
            assertAccessModifier()
        }
        assertSuperclass()
        assertImplementsInterfaces()
        return this
    }

    /**
     * Asserts that the Class is declared correctly.
     *
     * @param minSimilarity the Minimum required Similarity
     * @return [ClassTester] this
     */
    fun verify(minSimilarity: Double): ClassTester<T> {
        val currSim = classIdentifier.similarity
        classIdentifier.similarity = minSimilarity
        verify()
        classIdentifier.similarity = currSim
        return this
    }

    /**
     * Assert tthat the Superclass of [.theClass] matches [.superClass]
     */
    fun assertSuperclass() {
        assertClassResolved()
        if (superClass == null) {
            if (accessModifier >= 0) {
                if (accessModifier and TestUtils.ENUM != 0) {
                    assertSame(Enum::class.java, theClass!!.superclass)
                } else if (Modifier.isInterface(accessModifier)) {
                    assertSame(null, theClass!!.superclass)
                } else {
                    assertSame(Any::class.java, theClass!!.superclass)
                }
            }
        } else {
            assertSame(superClass, theClass!!.superclass)
        }
    }

    /**
     * Asserts that the Access Modifier is correct, with propper Fail Message
     */
    fun assertAccessModifier() {
//        disabled for this submbission
//        if (accessModifier >= 0) {
//            TestUtils.assertModifier(accessModifier, theClass);
//        }
    }

    /**
     * Sets [.classInstance] to the given Value
     *
     * @param classInstance the new Class Instance
     */
    fun setClassInstance(classInstance: Any) {
        this.classInstance = classInstance as T
    }

    /**
     * Returns true if [.classInstance] is not `null`
     *
     * @return true if [.classInstance] is not `null`
     */
    fun classInstanceResolved(): Boolean {
        return classInstance != null
    }

    /**
     * Asserts that [.classInstance] is not `null`
     */
    fun assertClassInstanceResolved() {
        assertNotNull(classInstance, String.format("no instance found for class <%s>", classIdentifier.identifierName))
    }

    /**
     * Assert that enum Constants with the given names exist
     *
     * @param expectedConstants the enum Constants
     */
    fun assertEnumConstants(expectedConstants: Array<String>) {
        assertClassResolved()
        val enum_values = theClass!!.enumConstants
        for (n in expectedConstants) {
            assertTrue(
                Stream.of(*enum_values).anyMatch { x: T -> x.toString() == n },
                String.format("Enum-Konstante %s fehlt.", n)
            )
        }
    }

    /**
     * Gets a random Enum Constant
     *
     * @return the random Enum Constant
     */
    val randomEnumConstant: Enum<*>?
        get() {
            assertIsEnum()
            return getRandomEnumConstant(theClass as Class<Enum<*>>?, classIdentifier.identifierName)
        }
    /**
     * Resolves a Class With the given name and Similarity
     *
     * @param similarity The minimum required similarity
     * @return the resolved Class With the given name and similarity
     */
    /**
     * Resolves a Class With the current Class name and Similarity
     *
     * @return the resolved Class With the given name and similarity
     */
    @JvmOverloads
    fun findClass(
        packageName: String? = classIdentifier.packageName,
        className: String? = classIdentifier.identifierName,
        similarity: Double = classIdentifier.similarity
    ): Class<T> {
        // if (similarity >= 1) {
        // return theClass = (Class<T>) assertDoesNotThrow(
        // () -> Class.forName(String.format("%s.%s", packageName, className)),
        // getClassNotFoundMessage(className));
        // }
        val classes: Unit = assertDoesNotThrow { TestUtils.getClasses(packageName) }
        val bestMatch: Unit = Arrays.stream(classes)
            .sorted { x, y ->
                java.lang.Double.valueOf(TestUtils.similarity(className, y.getSimpleName()))
                    .compareTo(TestUtils.similarity(className, x.getSimpleName()))
            }
            .findFirst().orElse(null)
        val sim = TestUtils.similarity(bestMatch.getSimpleName(), className)
        assertNotNull(bestMatch, classNotFoundMessage)
        if (sim < similarity) {
            fail(String.format("class <%s> not found", className))
        }
        return bestMatch as Class<T>?. also { theClass = it }
    }

    /**
     * Resolves a Class With the given Similarity
     *
     * @param similarity The minimum required similarity
     * @return the resolved Class With the given name and similarity
     */
    fun findClass(similarity: Double): Class<T> {
        return findClass(classIdentifier.packageName, classIdentifier.identifierName, similarity)
    }

    /**
     * Finds The Class and stores it in [.theClass]
     *
     * @return this
     */
    fun resolveClass(): ClassTester<T> {
        theClass = findClass()
        return this
    }

    /**
     * Resolves the class if necessary (We do not care about fields fields being
     * made accessible here)
     *
     * @return this
     */
    fun assureResolved(): ClassTester<T> {
        if (!class_resolved()) {
            resolveClass()
        }
        return this
    }

    /**
     * Resolves the Class and Instance and stores them in [.theClass] and
     * [.classInstance]
     *
     * @return this
     */
    fun resolve(): ClassTester<T> {
        assureResolved()
        resolveInstance()
        return this
    }

    /**
     * Resolves the Class and Instance and stores them in [.theClass] and
     * [.classInstance]
     *
     * @return this
     */
    fun resolveReal(): ClassTester<T> {
        assureResolved()
        resolveRealInstance()
        return this
    }

    /**
     * Resolves the Class and Instance and stores them in [.theClass] and
     * [.classInstance]
     *
     * @return this
     */
    fun resolveStatic(): ClassTester<T> {
        assureResolved()
        resolveStaticInstance()
        return this
    }

    /**
     * Resolves an Instance of [.theClass] (even abstract)
     *
     * @return the instance
     */
    fun resolveInstance(): T {
        return findInstance(theClass, classIdentifier.identifierName).also { classInstance = it }
    }

    /**
     * / * Resolves a static Instance of [.theClass] (even abstract)
     *
     * @return the instance
     */
    fun resolveStaticInstance(): T {
        return findStaticInstance(theClass, classIdentifier.identifierName).also { classInstance = it }
    }

    /**
     * Resolves an Instance of [.theClass] (even abstract)
     *
     * @return the instance
     */
    val newInstance: T
        get() {
            val instance = findInstance(theClass, classIdentifier.identifierName)
            resolveInstance()
            return instance
        }

    /**
     * Resolves an Instance of [.theClass] (even abstract)
     *
     * @return the instance
     */
    val newRealInstance: T
        get() = legacyFindInstance(theClass, classIdentifier.identifierName)

    /**
     * Resolve a real Instance
     *
     * @return the real instance
     */
    fun resolveRealInstance(): ClassTester<T> {
        setClassInstance(legacyFindInstance<T>(theClass, classIdentifier.identifierName))
        return this
    }

    /**
     * Resolves a Constructor with the given parameters
     *
     * @param parameters the Expected Parameters
     * @return the best Match
     */
    fun resolveConstructor(parameters: java.util.ArrayList<ParameterMatcher>?): Constructor<T>? {
        assertClassResolved()
        val constructors = assertDoesNotThrow { theClass!!.declaredConstructors } as Array<Constructor<T>>
        assertTrue(constructors.size > 0, "Keine Konstruktoren gefunden.")
        var bestMatch: Constructor<T>? = null
        bestMatch = if (parameters != null && !parameters.isEmpty()) {
            // Find Best match according to parameter options
            Arrays.stream<Constructor<T>>(constructors)
                .sorted(java.util.Comparator { x: Constructor<T>, y: Constructor<T>? ->
                    Integer
                        .valueOf(
                            MethodTester.Companion.countMatchingParameters(
                                parameters,
                                java.util.ArrayList<Parameter>(Arrays.asList<Parameter>(*x.parameters)), true
                            )
                        )
                        .compareTo(
                            MethodTester.Companion.countMatchingParameters(
                                parameters,
                                java.util.ArrayList<Parameter>(Arrays.asList<Parameter>(*x.parameters)), true
                            )
                        )
                })
                .findFirst().orElse(null)
        } else {
            Arrays.stream<Constructor<T>>(constructors).filter(Predicate { x: Constructor<T> -> x.parameterCount == 0 })
                .findFirst().orElse(null)
        }
        assertNotNull(bestMatch, "Der Passende Konstruktor wurde nicht gefunden")
        return bestMatch
    }

    /**
     * Resolves a Constructor with the given parameters
     *
     * @param parameters the Expected Parameters
     * @return the best Match
     */
    fun resolveConstructor(vararg parameters: ParameterMatcher?): Constructor<T> {
        return resolveConstructor(java.util.ArrayList<ParameterMatcher>(Arrays.asList<ParameterMatcher>(*parameters)))
    }

    /**
     * Asserts that a [Constructor] was declared correctly
     *
     * @param constructor    the [Constructor]
     * @param accessModifier the expected access Modifier
     * @param parameters     the expected Parameters
     */
    fun assertConstructorValid(
        constructor: Constructor<T>, accessModifier: Int,
        parameters: java.util.ArrayList<ParameterMatcher>?
    ) {
        assertNotNull(constructor, "Der Passende Konstruktor wurde nicht gefunden")
        TestUtils.assertModifier(accessModifier, constructor)
        MethodTester.Companion.assertParametersMatch(
            parameters, java.util.ArrayList<Parameter>(Arrays.asList<Parameter>(*constructor.parameters)),
            true
        )
    }

    /**
     * Asserts that a [Constructor] was declared correctly
     *
     * @param constructor    the [Constructor]
     * @param accessModifier the expected access Modifier
     * @param parameters     the expected Parameters
     */
    fun assertConstructorValid(
        constructor: Constructor<T>?, accessModifier: Int,
        vararg parameters: ParameterMatcher?
    ) {
        assertConstructorValid(
            constructor,
            accessModifier,
            java.util.ArrayList<ParameterMatcher>(Arrays.asList<ParameterMatcher>(*parameters))
        )
    }

    //    /**
    //     * Gets Method Documentation for JavaDoc
    //     *
    //     * @param d the Source Documentation
    //     * @return the Method Documentation
    //     */
    //    public MethodDocumentation getConstructorDocumentation(SourceDocumentation d, ParameterMatcher... parameters) {
    //        try {
    //            assureClassResolved();
    //            var constructor = resolveConstructor(parameters);
    //            return d.forTopLevelType(getTheClass().getName()).forConstructor(
    //                constructor.getParameterTypes());
    //        } catch (Throwable e) {
    //            return d.forTopLevelType("").forConstructor();
    //        }
    //    }
    fun `when`() {}

    /**
     * Sets a field to [.classInstance]
     *
     * @param field the Field to modify
     * @param value the new Value
     */
    fun setField(field: Field, value: Any?) {
        setField(classInstance, field, value!!)
    }

    /**
     * Sets a field of [.classInstance] to a random Value Supported by its
     * type
     *
     * @param field the Field to set
     * @return the random Value
     */
    fun setFieldRandom(field: Field): Any? {
        assertNotNull(field, "Das Feld wurde nicht gefunden.")
        val value = getRandomValue(field.type)
        setField(field, value)
        return value
    }

    /**
     * Gets the Value of a given field of [.classInstance]
     *
     * @param field the Field to get
     * @return the Value
     */
    fun getFieldValue(field: Field): Any {
        assertClassInstanceResolved()
        if (!field.canAccess(if (Modifier.isStatic(field.modifiers)) null else classInstance)) {
            assertDoesNotThrow { field.isAccessible = true }
        }
        return assertDoesNotThrow { field[classInstance] }
    }
    /**
     * Asserts that a given field has a certain value
     *
     * @param field             the field
     * @param expected          the expected Value
     * @param additionalMessage an Addition Error Message
     */
    /**
     * Asserts that a given field has a certain value
     *
     * @param field    the field
     * @param expected the expected Value
     */
    @JvmOverloads
    fun assertFieldEquals(field: Field, expected: Any?, additionalMessage: String? = "") {
        assertNotNull(field, "Fehlerhafter Test:Das Attribut konnte nicht gefunden werden.")
        val message = ("Das Attribut " + field.name + " hat den falschen Wert."
                + if (additionalMessage == null) "" else """

     $additionalMessage
     """.trimIndent())
        if (field.type is Class<*>) {
            val actual = getFieldValue(field)
            if (expected == null && actual != null || expected != null && expected != actual) {
                fail(
                    if (message + "Expected: [" +
                        expected == null
                    ) null else expected!!.javaClass.name + "@" + Integer.toHexString(expected.hashCode())
                            + "], but got: ["
                            +
                            (if (actual == null) null else actual.javaClass.name + "@"
                                    + Integer.toHexString(actual.hashCode()))
                            + "]"
                )
            }
        } else {
            assertEquals(expected, getFieldValue(field), message)
        }
    }

    /**
     * Gets a specific Enum-Value
     *
     * @param expectedName the expected Enum Class Name
     * @param similarity   the min Similarity
     * @return the specific Enum-Value
     */
    fun getEnumValue(expectedName: String, similarity: Double): Enum<*> {
        return getEnumValue<Any>(theClass as Class<Enum<*>>?, expectedName, similarity)
    }

    fun assertCorrectDeclaration() {
        Utils.TestCollection.test()
            .add { assertAccessModifier() }
            .run()
    }

    companion object {
        /**
         * Gets all Fields from a given Class and its superclasses recursively
         *
         * @param fields the fields so far (initially give it new ArrayList<>())
         * @param clazz  the Class to search
         * @return all Fields from a given Class and its superclasses recursively
         */
        private fun getAllFields(fields: java.util.ArrayList<Field?>, clazz: Class<*>?): java.util.ArrayList<Field?> {
            fields.addAll(Arrays.asList<Field>(*clazz!!.declaredFields))
            if (clazz.superclass != null) {
                getAllFields(fields, clazz.superclass)
            }
            return fields
        }

        /**
         * Gets all Fields from a given Class and its superclasses recursively
         *
         * @param clazz the Class to search
         * @return all Fields from a given Class and its superclasses recursively
         */
        fun getAllFields(clazz: Class<*>?): java.util.ArrayList<Field?> {
            return getAllFields(java.util.ArrayList(), clazz)
        }

        /**
         * Generates a class not found Message
         *
         * @param className the Class Name
         * @return a class not found Message
         */
        fun getClassNotFoundMessage(className: String?): String {
            return String.format("Klasse %s existiert nicht.", className)
        }

        /**
         * Generates a Interface not found Message
         *
         * @param interfaceName the Interface Name
         * @return a Interface not found Message
         */
        fun getInterfaceNotImplementedMessage(interfaceName: String?): String {
            return String.format("Interface %s wird nicht erweitert.", interfaceName)
        }

        /**
         * Asserts that a given Class is not `null` and fails with the propper
         * message if not
         *
         * @param theClass  the [Class]
         * @param className the Class Name for the error Message
         */
        fun assertClassNotNull(theClass: Class<*>?, className: String?) {
            if (theClass == null) {
                fail(String.format("class %s does not exist", className))
            }
        }

        /**
         * Generates a Message for a Missing enum constant
         *
         * @param constantName the Constant name
         * @return the generated Message
         */
        fun getEnumConstantMissingMessage(constantName: String?): String {
            return String.format("Enum-Konstante %s fehlt.", constantName)
        }

        /**
         * Gets a random Enum Constant
         *
         * @param enumClass     the [Enum]-[Class]
         * @param enumClassName the expected [Enum]-[Class]-Name
         * @return the random Enum Constant
         */
        fun getRandomEnumConstant(enumClass: Class<Enum<*>>?, enumClassName: String?): Enum<*>? {
            assertIsEnum(enumClass, enumClassName)
            val enumConstants = enumClass!!.enumConstants
            return if (enumConstants.size == 0) {
                null
            } else enumConstants[ThreadLocalRandom.current().nextInt(enumConstants.size)]
        }

        /**
         * Returns the Default Value for the given Type
         *
         * @param type the Type Class
         * @return the Default Value for the given Type
         */
        fun getDefaultValue(type: Class<*>?): Any? {
            return if (type == null) {
                null
            } else if (type == Short::class.javaPrimitiveType || type == Short::class.java) {
                0.toShort()
            } else if (type == Int::class.javaPrimitiveType || type == Int::class.java) {
                0
            } else if (type == Long::class.javaPrimitiveType || type == Long::class.java) {
                0L
            } else if (type == Float::class.javaPrimitiveType || type == Float::class.java) {
                0f
            } else if (type == Double::class.javaPrimitiveType || type == Double::class.java) {
                0.0
            } else if (type == Char::class.javaPrimitiveType || type == Char::class.java) {
                'a'
            } else if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) {
                false
            } else {
                null
            }
        }

        /**
         * Returns the Random Value for the given Type
         *
         * @param type the Type Class
         * @return the Random Value for the given Type
         */
        fun getRandomValue(type: Class<*>?): Any? {
            if (type == null) {
                return null
            }
            return if (type == Byte::class.javaPrimitiveType || type == Byte::class.java) {
                ThreadLocalRandom.current().nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE).toByte()
            } else if (type == Short::class.javaPrimitiveType || type == Short::class.java) {
                ThreadLocalRandom.current().nextInt(Short.MIN_VALUE, Short.MAX_VALUE).toShort()
            } else if (type == Int::class.javaPrimitiveType || type == Int::class.java) {
                ThreadLocalRandom.current().nextInt(Int.MIN_VALUE, Int.MAX_VALUE)
            } else if (type == Long::class.javaPrimitiveType || type == Long::class.java) {
                ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
            } else if (type == Float::class.javaPrimitiveType || type == Float::class.java) {
                ThreadLocalRandom.current().nextDouble(Float.MIN_VALUE, Float.MAX_VALUE)
                    .toFloat()
            } else if (type == Double::class.javaPrimitiveType || type == Double::class.java) {
                ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)
            } else if (type == Char::class.javaPrimitiveType || type == Char::class.java) {
                ThreadLocalRandom.current().nextInt(Character.MIN_VALUE.code, Character.MAX_VALUE.code)
                    .toChar()
            } else if (type == Boolean::class.javaPrimitiveType) {
                ThreadLocalRandom.current().nextBoolean()
            } else if (type.isEnum) {
                getRandomEnumConstant(type as Class<Enum<*>>?, type.name)
            } else {
                findInstance(type, type.name + "Impl" + ThreadLocalRandom.current().nextInt(1000, 10000))
            }
        }

        /**
         * Generates A derived Class from a given Class
         *
         * @param <T>              The Generic Class Type
         * @param clazz            The source class
         * @param className        the source Class Name
         * @param derivedClassName the name for the derived Class
         * @return the derived Class
        </T> */
        fun <T> generateDerivedClass(
            clazz: Class<T>?, className: String?,
            derivedClassName: String?
        ): Class<out T> {
            assertClassNotNull(clazz, className)
            return ByteBuddy()
                .subclass(clazz)
                .make()
                .load(clazz!!.classLoader)
                .getLoaded()
        }

        /**
         * Resolves an Instance of a given class (even abstract)
         *
         * @param <T>       The Instance type
         * @param clazz     The class to generate the Instance from
         * @param className the Class Name
         * @return the instance
        </T> */
        fun <T> findInstance(clazz: Class<in T>?, className: String?): T {
            assertClassNotNull(clazz, className)
            return mock(clazz, CALLS_REAL_METHODS) as T
        }

        /**
         * Resolves a static Instance of a given class (even abstract)
         *
         * @param <T>       The Instance type
         * @param clazz     The class to generate the Instance from
         * @param className the Class Name
         * @return the instance
        </T> */
        fun <T> findStaticInstance(clazz: Class<in T>?, className: String?): T {
            assertClassNotNull(clazz, className)
            return mockStatic(clazz, CALLS_REAL_METHODS) as T
        }

        /**
         * Resolves an Instance of a given class (even abstract)
         *
         * @param <T>       The Instance type
         * @param clazz     The class to generate the Instance from
         * @param className the Class Name
         * @return the instance
        </T> */
        fun <T> legacyFindInstance(clazz: Class<in T>?, className: String?): T? {
            var clazz = clazz
            assertClassNotNull(clazz, className)
            if (Modifier.isAbstract(clazz!!.modifiers)) {
                clazz = generateDerivedClass(
                    clazz, className,
                    className + ThreadLocalRandom.current().nextInt(1000, 10000)
                ) as Class<T>
            }
            assertFalse(Modifier.isAbstract(clazz.modifiers), "Kann keine Abstrakten Klasssen instanzieren.")
            val constructors = clazz.declaredConstructors
            var instance: T? = null
            for (c in constructors) {
                try {
                    c.isAccessible = true
                    val params = c.parameters
                    val constructorArgs: Array<Any> = Arrays.stream<Parameter>(params).map<Any>(
                        Function { x: Parameter -> getDefaultValue(x.type) }).toArray()
                    instance = c.newInstance(*constructorArgs) as T
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }
            }
            assertNotNull(instance, "Could not create Instance.")
            return instance
        }

        @Throws(IllegalArgumentException::class, IllegalAccessException::class)
        fun setFieldTyped(field: Field?, obj: Any?, value: Any) {
            if (field == null) {
                return
            }
            val type = field.type
            if (type == Byte::class.javaPrimitiveType || type == Byte::class.java) {
                field.setByte(obj, value as Byte)
            } else if (type == Short::class.javaPrimitiveType || type == Short::class.java) {
                field.setShort(obj, value as Short)
            } else if (type == Int::class.javaPrimitiveType || type == Int::class.java) {
                field.setInt(obj, value as Int)
            } else if (type == Long::class.javaPrimitiveType || type == Long::class.java) {
                field.setLong(obj, value as Long)
            } else if (type == Float::class.javaPrimitiveType || type == Float::class.java) {
                field.setFloat(obj, value as Float)
            } else if (type == Double::class.javaPrimitiveType || type == Double::class.java) {
                field.setDouble(obj, value as Double)
            } else if (type == Char::class.javaPrimitiveType || type == Char::class.java) {
                field.setChar(obj, value as Char)
            } else if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) {
                field.setBoolean(obj, value as Boolean)
            } else {
                field[obj] = value
            }
        }

        /**
         * Sets a field to a given Class
         *
         * @param instance the Instance to set the field
         * @param field    the Field to modify
         * @param value    the new Value
         */
        fun setField(instance: Any?, field: Field, value: Any) {
            assertNotNull(field, "Das Feld wurde nicht gefunden.")
            assertDoesNotThrow({
                field.isAccessible = true
                setFieldTyped(field, instance, value)
            }, "Konnte nicht auf Attribut " + field.name + " zugreifen.")
        }

        /**
         * Gets the Value of a given field of a given Instance
         *
         * @param instance the Class Instance
         * @param field    the Field to get
         * @return the Value
         */
        fun getFieldValue(instance: Any?, field: Field): Any {
            assertNotNull(field, "Das Feld wurde nicht gefunden.")
            assertNotNull(instance, "Es wurde keine Klassen-Instanz gefunden.")
            return assertDoesNotThrow { field[instance] }
        }

        /**
         * Gets a specific Enum-Value
         *
         * @param <T>          The Generic Enum-Type
         * @param enumClass    the Enum Class
         * @param expectedName the expected Enum Class Name
         * @param similarity   the min Similarity
         * @return the specific Enum-Value
        </T> */
        fun <T> getEnumValue(enumClass: Class<Enum<*>>?, expectedName: String, similarity: Double): Enum<*> {
            val enumConstants = enumClass!!.enumConstants
            val bestMatch: Enum<out Enum<*>> =
                Arrays.stream<Enum<*>>(enumConstants).sorted(java.util.Comparator { x: Enum<*>, y: Enum<*> ->
                    java.lang.Double.valueOf(
                        TestUtils.similarity(expectedName, y.name)
                    ).compareTo(TestUtils.similarity(expectedName, x.name))
                })
                    .findFirst().orElse(null)
            assertNotNull(bestMatch, "Enum-Wert$expectedName existiert nicht.")
            val sim = TestUtils.similarity(expectedName, bestMatch.name)
            assertTrue(
                sim >= similarity,
                "Enum-Wert" + expectedName + " existiert nicht. Ähnlichte Konstante:" + bestMatch.name
            )
            return bestMatch
        }
        /**
         * asserts a given Class is an Interface
         *
         * @param theClass  the class to check
         * @param className the expected Class Name
         */
        /**
         * asserts [.theClass] is an Interface
         */
        @JvmOverloads
        fun assertIsInterface(theClass: Class<*> = theClass, className: String? = classIdentifier.identifierName) {
            assertClassNotNull(theClass, className)
            assertTrue(theClass.isInterface, String.format("%s ist kein Interface.", className))
        }
        /**
         * asserts a given Class is an Enum
         *
         * @param theClass  the class to check
         * @param className the expected Class Name
         */
        /**
         * asserts [.theClass] is an Enum
         */
        @JvmOverloads
        fun assertIsEnum(theClass: Class<*>? = this.theClass, className: String? = classIdentifier.identifierName) {
            assertClassNotNull(theClass, className)
            assertTrue(theClass!!.isEnum, String.format("%s ist kein Enum.", className))
        }
        /**
         * asserts a given Class is a Plain Class
         *
         * @param theClass  the class to check
         * @param className the expected Class Name
         */
        /**
         * asserts [.theClass] is a Plain Class
         */
        @JvmOverloads
        fun assertIsPlainClass(theClass: Class<*> = this.theClass, className: String? = classIdentifier.identifierName) {
            assertClassNotNull(theClass, className)
            assertFalse(theClass.isInterface, String.format("%s sollte kein Interface sein.", className))
            assertFalse(theClass.isEnum, String.format("%s sollte kein Enum sein.", className))
        }
    }
}
