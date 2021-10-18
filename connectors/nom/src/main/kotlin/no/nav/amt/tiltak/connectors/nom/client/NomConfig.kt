package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.port.NomConnector
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@Configuration
open class NomConfig {

	@Bean("nomApiTokenSupplier")
	open fun tokenSupplier (
		tokenProvider: ScopedTokenProvider,
		@Value("nom.scope") scope: String
	) : Supplier<String> {
		return Supplier { tokenProvider.getToken(scope) }
	}

	@Bean
	open fun nomConnector(
		@Value("nom.api") nomApi: String,
		@Qualifier("nomApiTokenSupplier") tokenSupplier : Supplier<String>
	) : NomConnector {
		return NomClient(nomApiUrl = nomApi, tokenSupplier = tokenSupplier)
	}
}

