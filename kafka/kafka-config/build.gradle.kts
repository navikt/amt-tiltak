dependencies {
    api(project(":core"))
    api(project(":common:json"))
    api(libs.nav.common.kafka) {
        exclude(group = "org.lz4", module = "lz4-java")
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "org.apache.avro", module = "avro")
        exclude(group = "io.confluent", module = "kafka-avro-serializer")
    }
    api("org.springframework.boot:spring-boot-starter")
    api(libs.shedlock.jdbc.template)
    api("io.micrometer:micrometer-core")
    testImplementation(libs.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(project(":test:database"))
}
