plugins {
    id("default-conventions")
}

dependencies {
    implementation(libs.caffeine)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}