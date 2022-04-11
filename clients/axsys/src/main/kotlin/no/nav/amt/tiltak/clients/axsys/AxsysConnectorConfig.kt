package no.nav.amt.tiltak.clients.axsys

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AxsysConnectorConfig(
	@Value("\${axsys.url}") val url: String,
	@Value("\${axsys.scope}") val scope: String
) {

	@Bean
	open fun axsysClient(machineToMachineTokenClient: MachineToMachineTokenClient): AxsysClient {
		val delegate = PlainAxsysClient(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)

		return CachedDelgatingAxsysClient(delegate)
	}

}
