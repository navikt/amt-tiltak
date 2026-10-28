dependencies {
    api(project(":core"))
    api(project(":kafka:kafka-config"))
    api(project(":common:json"))
    api(libs.nav.common.kafka) {
        exclude(group = "org.lz4", module = "lz4-java")
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "org.apache.avro", module = "avro")
        exclude(group = "io.confluent", module = "kafka-avro-serializer")
    }
    api("org.springframework.boot:spring-boot-starter")
}
