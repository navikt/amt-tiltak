package no.nav.amt.tiltak.clients.poao_tilgang

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PoaoTilgangConfig {

	@Value("\${poao-tilgang.url}")
	lateinit var poaoTilgangUrl: String

	@Value("\${poao-tilgang.scope}")
	lateinit var poaoTilgangScope: String

	@Bean
	open fun poaoTilgangClient(machineToMachineTokenClient: MachineToMachineTokenClient): PoaoTilgangClient {
		return PoaoTilgangClientImpl(
			baseUrl = poaoTilgangUrl,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(poaoTilgangScope) },
		)
	}

}
