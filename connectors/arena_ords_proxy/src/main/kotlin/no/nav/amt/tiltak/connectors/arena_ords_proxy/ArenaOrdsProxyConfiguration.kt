package no.nav.amt.tiltak.connectors.arena_ords_proxy

import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ArenaOrdsProxyConfiguration {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var scope: String

    @Bean
    open fun arenaOrdsProxyConnector(
		scopedTokenProvider: ScopedTokenProvider
	): ArenaOrdsProxyConnector {
        return ArenaOrdsProxyConnectorImpl(
            tokenProvider = { scopedTokenProvider.getToken(scope) },
            arenaOrdsProxyUrl = "$url/proxy/amt-arena-ords-proxy"
        )
    }

}
