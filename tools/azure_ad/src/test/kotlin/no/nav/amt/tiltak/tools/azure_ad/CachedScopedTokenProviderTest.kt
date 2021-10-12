package no.nav.amt.tiltak.tools.azure_ad

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class CachedScopedTokenProviderTest {

	@Test
	fun `should cache tokens with same scope`() {
		val accessToken = PlainJWT(
			JWTClaimsSet.Builder().expirationTime(Date(System.currentTimeMillis() + 100000)).build()
		).serialize()

		val provider = mockk<ScopedTokenProvider>()
		val cachedProvider = CachedScopedTokenProvider(provider)

		every {
			provider.getToken(any())
		} returns accessToken

		val scope1 = "SCOPE_1"
		val scope2 = "SCOPE_2"

		cachedProvider.getToken(scope1)
		cachedProvider.getToken(scope1)
		cachedProvider.getToken(scope2)

		verify (exactly = 1) {
			provider.getToken(eq(scope1))
		}

		verify (exactly = 1) {
			provider.getToken(eq(scope2))
		}
	}

	@Test
	fun `should not cache expired token`() {
		val accessToken = PlainJWT(
			JWTClaimsSet.Builder().expirationTime(Date(System.currentTimeMillis() + 59000)).build()
		).serialize()

		val provider = mockk<ScopedTokenProvider>()

		every {
			provider.getToken(any())
		} returns accessToken

		val cachedProvider = CachedScopedTokenProvider(provider)
		val scope = "SCOPE"

		cachedProvider.getToken(scope)
		cachedProvider.getToken(scope)

		verify (exactly = 2) {
			provider.getToken(eq(scope))
		}
	}
}
