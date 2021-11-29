package no.nav.amt.tiltak.common.auth

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException


class AuthServiceTest {

	companion object {
		private val server = MockOAuth2Server()

		init {
			server.start()
		}

		@AfterAll
		@JvmStatic
		fun cleanup() {
			server.shutdown()
		}
	}

	@Test
	fun `hentPersonligIdentTilInnloggetBruker skal kaste ResponseStatusException hvis bruker ikke har token`() {
		val contextHolder = object : TokenValidationContextHolder {
			override fun getTokenValidationContext(): TokenValidationContext {
				return TokenValidationContext(
					mapOf()
				)
			}
			override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
				throw NotImplementedError()
			}
		}

		val authService = AuthService(contextHolder)

		try {
		    authService.hentPersonligIdentTilInnloggetBruker()
			fail("")
		} catch (e: ResponseStatusException) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.status)
			assertEquals("""401 UNAUTHORIZED "User is not authorized, valid token is missing"""", e.message)
		}
	}

	@Test
	fun `hentPersonligIdentTilInnloggetBruker skal kaste ResponseStatusException hvis token ikke har "pid"-claim`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val contextHolder = object : TokenValidationContextHolder {
			override fun getTokenValidationContext(): TokenValidationContext {
				return TokenValidationContext(
					mapOf(
						"test" to JwtToken(token)
					)
				)
			}
			override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
				throw NotImplementedError()
			}
		}

		val authService = AuthService(contextHolder)

		try {
			authService.hentPersonligIdentTilInnloggetBruker()
			fail("")
		} catch (e: ResponseStatusException) {
			assertEquals(HttpStatus.UNAUTHORIZED, e.status)
			assertEquals("""401 UNAUTHORIZED "PID is missing or is not a string"""", e.message)
		}
	}

	@Test
	fun `hentPersonligIdentTilInnloggetBruker skal returnere personlig ident til bruker fra token`() {
		val token = server.issueToken("tokenx", "test", "test", mapOf("pid" to "12345678")).serialize()

		val contextHolder = object : TokenValidationContextHolder {
			override fun getTokenValidationContext(): TokenValidationContext {
				return TokenValidationContext(
					mapOf(
						"test" to JwtToken(token)
					)
				)
			}
			override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
				throw NotImplementedError()
			}
		}

		val authService = AuthService(contextHolder)

		assertEquals("12345678", authService.hentPersonligIdentTilInnloggetBruker())
	}

}
