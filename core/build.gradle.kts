plugins {
    id("default-conventions")
}

dependencies {
    implementation(libs.jackson.module.kotlin)
    // left out junit-jupiter-engine from POM because of no tests in this module
}