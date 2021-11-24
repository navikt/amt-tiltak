package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.port.NomConnector
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NomConfig {

	@Bean
	open fun nomConnector(
		@Value("nom.url") nomUrl: String,
		@Value("nom.scope") nomScope: String,
		scopedTokenProvider: ScopedTokenProvider,
	) : NomConnector {
		return NomClient(url = nomUrl, tokenSupplier = { scopedTokenProvider.getToken(nomScope) })
	}

}

