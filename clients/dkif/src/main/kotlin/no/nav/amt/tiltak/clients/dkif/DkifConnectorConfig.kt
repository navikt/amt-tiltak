package no.nav.amt.tiltak.clients.dkif

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DkifConnectorConfig {

	@Value("\${digdir-krr-proxy.url}")
	lateinit var url: String

	@Value("\${digdir-krr-proxy.scope}")
	lateinit var scope: String

	@Bean
	open fun dkifClient(machineToMachineTokenClient: MachineToMachineTokenClient): DkifClient {
		return DkifClientImpl(
			url = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
