import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import util.LibrariesUtils.getLibraryValue

/**
 * This precompiled plugin sets up a Kotlin JVM project with standardized configurations,
 * such as Java version compatibility, repository settings, and test framework usage.
 *
 * ## Features:
 * - Configures repositories to include Maven Central and additional custom repositories.
 * - Applies the Kotlin JVM plugin to enable Kotlin support.
 * - Ensures a consistent JVM toolchain version (default: Java 21).
 * - Configures test tasks to use the JUnit Platform for test execution.
 *
 * ## Usage:
 * To apply this plugin, include it in your Gradle project:
 * ```kotlin
 * plugins {
 *     id("default-conventions")
 * }
 * ```
 *
 * ## Customization:
 * - The `javaVersion` variable can be updated to change the targeted Java version.
 * - Add or modify repository URLs in the `repositories` block as needed.
 *
 * ## Dependencies:
 * This plugin requires:
 * - Kotlin JVM Plugin
 * - JUnit Platform (for test execution)
 *
 * @property javaVersion The Java version to target for the JVM toolchain. Default is 21.
 */
val javaVersion = 21

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

plugins {
    kotlin("jvm")
    id("io.spring.dependency-management") apply false
}

configure<DependencyManagementExtension> {
    dependencies {
        dependency(getLibraryValue("nav.common.kafka").toString()) {
            exclude("org.xerial.snappy:snappy-java")
        }
    }
}

configure<KotlinJvmProjectExtension> {
    jvmToolchain(javaVersion)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
            "-Xwarning-level=IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE:disabled",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "-Xshare:off",
        "-XX:+EnableDynamicAgentLoading",
        "-Dkotest.framework.classpath.scanning.autoscan.disable=true",
    )
}