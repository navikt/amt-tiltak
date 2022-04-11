package no.nav.amt.tiltak.clients.amt_enhetsregister

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AmtEnhetsregisterConfig {

	@Value("\${amt-enhetsregister.url}")
	lateinit var url: String

	@Value("\${amt-enhetsregister.scope}")
	lateinit var scope: String

	@Bean
	open fun enhetsregiserClient(machineToMachineTokenClient: MachineToMachineTokenClient): EnhetsregisterClient {
		return AmtEnhetsregisterClient(
			url = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
