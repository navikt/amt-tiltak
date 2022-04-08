package no.nav.amt.tiltak.clients.veilarbarena

import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarbarenaConfig {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var poaoGcpProxyScope: String

	@Value("\${veilarbarena.scope}")
	lateinit var veilarbarenaScope: String

	@Bean
	open fun veilarbarenaClient(machineToMachineTokenClient: MachineToMachineTokenClient): VeilarbarenaClient {
		return VeilarbarenaClientImpl(
			url = "$url/proxy/veilarbarena",
			proxyTokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(poaoGcpProxyScope) },
			veilarbarenaTokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(veilarbarenaScope) },
		)
	}

}
