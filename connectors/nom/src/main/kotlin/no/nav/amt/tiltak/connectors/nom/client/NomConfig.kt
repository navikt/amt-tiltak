package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NomConfig {

	@Value("\${nom.url}")
	lateinit var url: String

	@Value("\${nom.scope}")
	lateinit var scope: String

	@Value("\${nom.mock:false}")
	var mock: Boolean = false

	@Bean
	open fun nomClient(scopedTokenProvider: ScopedTokenProvider, ) : NomClient {
		if (mock) {
			return NomClientMock()
		}

		return NomClientImpl(url = url, tokenSupplier = { scopedTokenProvider.getToken(scope) })
	}

}

