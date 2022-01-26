plugins {
    kotlin("jvm") version "1.6.10"
    id("org.sourcegrade.style") version "1.2.0"
}

group = "org.sourcegrade"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.mockito:mockito-core:4.2.0")
    implementation("fr.inria.gforge.spoon:spoon-core:10.0.0")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
}
