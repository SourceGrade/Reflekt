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
}
