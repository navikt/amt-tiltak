package no.nav.amt.tiltak.clients.amt_person

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class AmtPersonConfig {

	@Value("\${amt-person.url}")
	lateinit var url: String

	@Value("\${amt-person.scope}")
	lateinit var scope: String

	@Bean
	open fun amtPersonClient(machineToMachineTokenClient: MachineToMachineTokenClient): AmtPersonClient {
		return AmtPersonClientImpl(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
