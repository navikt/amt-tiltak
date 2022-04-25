package no.nav.amt.tiltak.tilgangskontroll.autentisering

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [NavAnsattAutentiseringController::class])
class NavAnsattAutentiseringControllerTest {

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var veilederService: VeilederService

	@Test
	fun `meg() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/autentisering/meg")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `meg() - skal returnere 200 med korrekt response`() {
		val navIdent = "Z1234"
		val navn = "Veileder Veiledersen"

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		`when`(veilederService.getOrCreateVeileder(navIdent))
			.thenReturn(NavAnsatt(
				UUID.randomUUID(),
				navIdent,
				navn,
				"",
				""
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/autentisering/meg")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		val expectedJson = """
			{"navIdent":"Z1234","navn":"Veileder Veiledersen"}
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

}
