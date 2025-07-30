plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Dependencies for the precompiled plugins.
    // These are included to ensure the precompiled plugin has access to necessary libraries.
    implementation(libs.kotlin.jvm)
    implementation(libs.springframework.boot)
    implementation(libs.spring.dependency.management)
    implementation(libs.kotlin.plugin.spring)
}