package no.nav.amt_tiltak.clients.amt_altinn_acl

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AmtAltinnAclConfig {

	@Value("\${amt-altinn-acl.url}")
	lateinit var url: String

	@Value("\${amt-altinn-acl.scope}")
	lateinit var scope: String

	@Bean
	open fun amtAltinnAclClient(machineToMachineTokenClient: MachineToMachineTokenClient): AmtAltinnAclClient {
		return AmtAltinnAclClientImpl(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
