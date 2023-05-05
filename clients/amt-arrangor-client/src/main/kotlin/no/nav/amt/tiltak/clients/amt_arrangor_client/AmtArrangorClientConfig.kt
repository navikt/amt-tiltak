package no.nav.amt.tiltak.clients.amt_arrangor_client

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AmtArrangorClientConfig {

	@Value("\${amt-arrangor.url}")
	lateinit var url: String

	@Value("\${amt-arrangor.scope}")
	lateinit var scope: String


	@Bean
	open fun amtArrangorClient(machineToMachineTokenClient: MachineToMachineTokenClient): AmtArrangorClient {
		return AmtArrangorClient(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) }
		)
	}

}
