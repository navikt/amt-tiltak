plugins {
    id("default-conventions")
}

dependencies {
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
}