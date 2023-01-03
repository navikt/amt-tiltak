package no.nav.amt.tiltak.clients.mulighetsrommet_api_client

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MulighetsrommetApiClientConfig {

	@Value("\${mulighetsrommet-api.url}")
	lateinit var url: String

	@Value("\${mulighetsrommet-api.scope}")
	lateinit var scope: String

	@Bean
	open fun mulighetsrommetApiClient(machineToMachineTokenClient: MachineToMachineTokenClient): MulighetsrommetApiClient {
		return MulighetsrommetApiClientImpl(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
