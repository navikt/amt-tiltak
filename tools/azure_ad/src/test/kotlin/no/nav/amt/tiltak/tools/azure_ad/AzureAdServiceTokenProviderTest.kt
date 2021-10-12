package no.nav.amt.tiltak.tools.azure_ad

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class AzureAdServiceTokenProviderTest {

	@Test
	fun `skal lage riktig scope`() {
		val scopedTokenProvider = mockk<ScopedTokenProvider>()
		val serviceTokenProvider = AzureAdServiceTokenProvider(scopedTokenProvider)

		every {
			scopedTokenProvider.getToken(any())
		} returns "TOKEN"

		serviceTokenProvider.getServiceToken("my-app", "test-namespace", "test-cluster")

		verify(exactly = 1) {
			scopedTokenProvider.getToken("api://test-cluster.test-namespace.my-app/.default")
		}
	}

}
