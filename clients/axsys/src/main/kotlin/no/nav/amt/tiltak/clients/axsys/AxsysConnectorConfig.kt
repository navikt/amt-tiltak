package no.nav.amt.tiltak.clients.axsys

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AxsysConnectorConfig(
	@Value("\${axsys.scope}") val axsysScope: String,
	@Value("\${poao-gcp-proxy.url}") val proxyUrl: String,
	@Value("\${poao-gcp-proxy.scope}") val proxyScope: String,
) {

	@Bean
	open fun axsysClient(machineToMachineTokenClient: MachineToMachineTokenClient): AxsysClient {
		val delegate = PlainAxsysClient(
			baseUrl = proxyUrl,
			proxyTokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(proxyScope) },
			axsysTokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(axsysScope) },
		)

		return CachedDelgatingAxsysClient(delegate)
	}

}
