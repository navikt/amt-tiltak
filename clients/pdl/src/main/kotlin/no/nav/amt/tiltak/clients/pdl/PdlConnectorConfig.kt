package no.nav.amt.tiltak.clients.pdl

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PdlConnectorConfig {

	@Value("\${pdl.url}")
	lateinit var url: String

	@Value("\${pdl.scope}")
	lateinit var scope: String

	@Bean
	open fun pdlClient(machineToMachineTokenClient: MachineToMachineTokenClient): PdlClient {
		return PdlClientImpl(
			pdlUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
