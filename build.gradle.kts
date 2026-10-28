import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

group = "no.nav.amt-tiltak"
version = "1.0-SNAPSHOT"

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun catalogVersion(name: String) = versionCatalog.findVersion(name).get().requiredVersion
val lz4Dependency = versionCatalog.findLibrary("lz4").get()

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

configure(subprojects.filter { it.buildFile.exists() }) {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "io.spring.dependency-management")

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${catalogVersion("spring-boot")}")
        }
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    dependencies {
        add("api", lz4Dependency)
        add("api", "org.jetbrains.kotlin:kotlin-stdlib:${catalogVersion("kotlin")}")
        add("api", "org.jetbrains.kotlinx:kotlinx-coroutines-core")
        add("api", "org.jetbrains.kotlin:kotlin-reflect:${catalogVersion("kotlin")}")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(catalogVersion("kotlin"))
                because("Keep Kotlin dependencies aligned with the compiler version")
            }
        }
        resolutionStrategy.force(
            "at.yawk.lz4:lz4-java:${catalogVersion("lz4")}",
            "com.fasterxml.jackson.core:jackson-annotations:${catalogVersion("jackson-annotations")}",
            "com.nimbusds:oauth2-oidc-sdk:${catalogVersion("nimbus")}",
            "com.squareup.okhttp3:okhttp:${catalogVersion("okhttp")}",
            "com.squareup.okhttp3:okhttp-jvm:${catalogVersion("okhttp")}",
            "io.getunleash:unleash-client-java:${catalogVersion("unleash")}",
            "io.kotest:kotest-assertions-core-jvm:${catalogVersion("kotest")}",
            "io.kotest:kotest-assertions-json-jvm:${catalogVersion("kotest")}",
            "io.kotest:kotest-framework-concurrency-jvm:${catalogVersion("kotest")}",
            "io.kotest:kotest-runner-junit5-jvm:${catalogVersion("kotest")}",
            "io.mockk:mockk-jvm:${catalogVersion("mockk")}",
            "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:${catalogVersion("shedlock")}",
            "net.javacrumbs.shedlock:shedlock-spring:${catalogVersion("shedlock")}",
            "net.logstash.logback:logstash-logback-encoder:${catalogVersion("logstash")}",
            "no.nav.amt.lib:models:${catalogVersion("amt-lib")}",
            "no.nav.common:audit-log:${catalogVersion("nav-common")}",
            "no.nav.common:job:${catalogVersion("nav-common")}",
            "no.nav.common:kafka:${catalogVersion("nav-common")}",
            "no.nav.common:log:${catalogVersion("nav-common")}",
            "no.nav.common:rest:${catalogVersion("nav-common")}",
            "no.nav.common:token-client:${catalogVersion("nav-common")}",
            "no.nav.common:types:${catalogVersion("nav-common")}",
            "no.nav.poao-tilgang:client:${catalogVersion("poao-tilgang")}",
            "no.nav.security:mock-oauth2-server:${catalogVersion("mock-oauth2-server")}",
            "no.nav.security:token-validation-spring:${catalogVersion("token-support")}",
            "org.apache.kafka:kafka-clients:${catalogVersion("kafka-clients")}",
            "org.apache.logging.log4j:log4j-api:${catalogVersion("log4j")}",
            "org.mockito.kotlin:mockito-kotlin:${catalogVersion("mockito-kotlin")}",
            "org.postgresql:postgresql:${catalogVersion("postgresql")}",
            "org.testcontainers:junit-jupiter:${catalogVersion("testcontainers")}",
            "org.testcontainers:kafka:${catalogVersion("testcontainers")}",
            "org.testcontainers:postgresql:${catalogVersion("testcontainers")}",
            "org.testcontainers:testcontainers:${catalogVersion("testcontainers")}",
            "tools.jackson.core:jackson-core:3.2.4",
            "tools.jackson.core:jackson-databind:3.2.4",
            "tools.jackson.module:jackson-module-kotlin:3.2.4",
        )
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
        jvmArgs("-Xshare:off", "-XX:+EnableDynamicAgentLoading")
        testLogging {
            events("failed", "skipped")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
