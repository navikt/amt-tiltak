package no.nav.amt.tiltak.clients.nom

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NomConfig {

	@Value("\${nom.url}")
	lateinit var url: String

	@Value("\${nom.scope}")
	lateinit var scope: String

	@Value("\${nom.mock:false}")
	var mock: Boolean = false

	@Bean
	open fun nomClient(machineToMachineTokenClient: MachineToMachineTokenClient) : NomClient {
		if (mock) {
			return NomClientMock()
		}

		return NomClientImpl(
			url = url,
			tokenSupplier = { machineToMachineTokenClient.createMachineToMachineToken(scope) }
		)
	}

}

