package no.nav.amt.tiltak.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("no.nav.amt.tiltak")
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}



