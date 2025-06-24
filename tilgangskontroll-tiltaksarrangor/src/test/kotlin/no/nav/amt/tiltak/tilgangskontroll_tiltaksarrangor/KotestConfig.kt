package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringAutowireConstructorExtension
import io.kotest.extensions.spring.SpringExtension
import org.springframework.core.env.AbstractEnvironment

object KotestConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "test")
    }

    override fun extensions() = listOf(
        SpringExtension,
        SpringAutowireConstructorExtension
    )
}
