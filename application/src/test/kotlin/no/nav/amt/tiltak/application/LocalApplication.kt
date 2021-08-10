package no.nav.amt.tiltak.application

import no.nav.amt.tiltak.application.util.LocalPostgresDatabase.createPostgresContainer
import org.springframework.boot.SpringApplication

fun main(args: Array<String>) {
    val postgresContainer = createPostgresContainer()
    postgresContainer.start()

    System.setProperty("spring.datasource.url", postgresContainer.jdbcUrl)
    System.setProperty("spring.datasource.username", postgresContainer.username)
    System.setProperty("spring.datasource.password", postgresContainer.password)

    val application = SpringApplication(Application::class.java)
    application.setAdditionalProfiles("local")
    application.run(*args)
}
