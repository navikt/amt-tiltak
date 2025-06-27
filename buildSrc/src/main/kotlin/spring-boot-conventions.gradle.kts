import org.springframework.boot.gradle.dsl.SpringBootExtension
import util.LibrariesUtils.getLibraryValue

/**
 * A Gradle build script configuration that sets up common dependencies and plugins for a Kotlin-based Spring Boot project.
 *
 * ## Features:
 * - Applies necessary plugins for Kotlin, Spring Boot, and dependency management:
 *   - `kotlin("jvm")`: Enables Kotlin support for JVM.
 *   - `org.jetbrains.kotlin.plugin.spring`: Adds Spring-specific features to Kotlin.
 *   - `org.springframework.boot`: Configures Spring Boot-specific functionality.
 *   - `io.spring.dependency-management`: Simplifies dependency management in Spring projects.
 *
 * - Defines and applies common dependencies for the project:
 *   - Dynamically retrieves library versions using the `getLibraryValue` utility function.
 *   - Includes Spring Boot test-related dependencies such as:
 *     - `kotest.runner.junit5`
 *     - `kotest.assertions.core`
 *     - `mockk`
 *
 * ## Usage:
 * Include this configuration in your build file to standardize dependency and plugin management:
 * ```kotlin
 * plugins {
 *     id("spring-boot-conventions")
 * }
 * ```
 *
 * ## Dependencies:
 * - The `util.LibrariesUtils.getLibraryValue` utility function is required for dynamically resolving library versions.
 * - Kotlin, Spring Boot, and related frameworks must be compatible with the applied plugins.
 *
 * ## Customization:
 * - Add additional dependencies to the `springBootTestDependencies` set for consistent test configurations.
 * - Modify or add plugins based on the project requirements.
 *
 * ## Key Components:
 * springBootTestDependencies A set of library identifiers for commonly used Spring Boot test dependencies.
 * - Can be extended to include other libraries as needed.
 */
val springBootTestDependencies = setOf(
    "kotest.runner.junit5",
    "kotest.assertions.core",
    "mockk"
)

plugins {
    kotlin("jvm") apply false
}

apply(plugin = "org.jetbrains.kotlin.plugin.spring")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(getLibraryValue("jackson.module.kotlin"))
    springBootTestDependencies.forEach {
        testImplementation(getLibraryValue(it))
    }
}

configure<SpringBootExtension> {
    mainClass = "no.nav.amt.tiltak.application.ApplicationKt"
}