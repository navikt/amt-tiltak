package no.nav.amt.tiltak.clients.veilarboppfolging

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarboppfolgingConfig {

	@Value("\${veilarboppfolging.url}")
	lateinit var url: String

	@Value("\${veilarboppfolging.scope}")
	lateinit var veilarboppfolgingScope: String

	@Bean
	open fun veilarboppfolgingClient(machineToMachineTokenClient: MachineToMachineTokenClient): VeilarboppfolgingClient {
		return VeilarboppfolgingClientImpl(
			apiUrl = "$url/veilarboppfolging",
			veilarboppfolgingTokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(veilarboppfolgingScope) }
		)
	}
}
