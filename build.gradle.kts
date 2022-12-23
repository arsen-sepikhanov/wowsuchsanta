// For `KotlinCompile` task below
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21" // Kotlin version to use
    application // Application plugin. Also see 1️⃣ below the code
}

group = "org.example" // A company name, for example, `org.jetbrains`
version = "1.0-SNAPSHOT" // Version to assign to the built artifact

repositories { // Sources of dependencies. See 2️⃣
    mavenCentral() // Maven Central Repository. See 3️⃣
}

dependencies { // All the libraries you want to use. See 4️⃣
    // Copy dependencies' names after you find them in a repository
    testImplementation(kotlin("test")) // The Kotlin test library
    implementation("com.squareup.okhttp3:okhttp:3.2.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
}

tasks.test { // See 5️⃣
    useJUnitPlatform() // JUnitPlatform for tests. See 6️⃣
}

tasks.withType<KotlinCompile> { // Settings for `KotlinCompile` tasks
    // Kotlin compiler options
    kotlinOptions.jvmTarget = "1.8" // Target version of generated JVM bytecode
}

application {
    mainClass.set("MainKt") // The main class of the application
}