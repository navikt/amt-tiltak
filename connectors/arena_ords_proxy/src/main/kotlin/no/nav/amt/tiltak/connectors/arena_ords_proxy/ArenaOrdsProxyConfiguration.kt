package no.nav.amt.tiltak.connectors.arena_ords_proxy

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Supplier

@Configuration
open class ArenaOrdsProxyConfiguration {

    private val tokenProvider: Supplier<String> = Supplier { UUID.randomUUID().toString() }
    private val arenaOrdsProxyUrl = ""

    @Bean
    open fun arenaOrdsProxyConnector(): ArenaOrdsProxyConnector {
        return ArenaOrdsProxyConnectorImpl(
            tokenProvider = tokenProvider,
            arenaOrdsProxyUrl = arenaOrdsProxyUrl
        )
    }

}
