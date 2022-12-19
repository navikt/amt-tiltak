package no.nav.amt.tiltak.clients.mr_arena_adapter_client

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MrArenaAdapterClientConfig {

	@Value("\${mr-arena-adapter.url}")
	lateinit var url: String

	@Value("\${mr-arena-adapter.scope}")
	lateinit var scope: String

	@Bean
	open fun mrArenaAdapterClient(machineToMachineTokenClient: MachineToMachineTokenClient): MrArenaAdapterClient {
		return MrArenaAdapterClientImpl(
			baseUrl = url,
			tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(scope) },
		)
	}

}
