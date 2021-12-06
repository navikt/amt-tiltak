package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Ansatt
import no.nav.amt.tiltak.core.port.TiltaksarrangorService
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [AnsattController::class])
class AnsattControllerTest {

	companion object {
		private val server = MockOAuth2Server()

		init {
			server.start()
			System.setProperty("MOCK_TOKEN_X_DISCOVERY_URL", server.wellKnownUrl("tokenx").toString())
		}

		@AfterAll
		@JvmStatic
		fun cleanup() {
			server.shutdown()
		}
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var tiltaksarrangorService: TiltaksarrangorService

	@MockBean
	private lateinit var authService: AuthService

	@Test
	fun `getInnloggetAnsatt() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/ansatt/meg")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test", mapOf("pid" to "12345678")).serialize()

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker()).thenReturn("12345678")

		Mockito.`when`(tiltaksarrangorService.getAnsattByPersonligIdent("12345678"))
			.thenReturn(Ansatt(
				id = UUID.randomUUID(),
				personligIdent = "",
				fornavn = "",
				etternavn = "",
				telefonnummer = "",
				epost = "",
				arrangorer = emptyList()
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/ansatt/meg")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

}
