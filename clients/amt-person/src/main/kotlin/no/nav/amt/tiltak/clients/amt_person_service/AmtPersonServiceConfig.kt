package no.nav.amt.tiltak.clients.amt_person_service

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class AmtPersonServiceConfig {

	@Value("\${amt-person-service.url}")
	lateinit var url: String

	@Value("\${amt-person-service.scope}")
	lateinit var scope: String

	@Bean
	open fun amtPersonServiceClient(machineToMachineTokenClient: MachineToMachineTokenClient): AmtPersonServiceClient {
		return AmtPersonServiceClientImpl(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
