package no.nav.amt.tiltak.application

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("no.nav.amt.tiltak")
@EnableJwtTokenValidation
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
