package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.clients.poao_tilgang.AdGruppe
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGruppeService
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
@WebMvcTest(controllers = [AutentiseringController::class])
class AutentiseringControllerTest {

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
	private lateinit var navAnsattService: NavAnsattService

	@MockBean
	private lateinit var adGruppeService: AdGruppeService

	@Test
	fun `meg() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/autentisering/meg")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `meg() - skal returnere 200 med korrekt response`() {
		val azureId = UUID.randomUUID()
		val navIdent = "Z1234"
		val navn = "Veileder Veiledersen"

		`when`(authService.hentAzureIdTilInnloggetBruker())
			.thenReturn(azureId)

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		`when`(navAnsattService.getNavAnsatt(navIdent))
			.thenReturn(NavAnsatt(
				UUID.randomUUID(),
				navIdent,
				navn,
				"",
				""
			))

		`when`(adGruppeService.hentAdGrupper(azureId))
			.thenReturn(listOf(
				AdGruppe(UUID.randomUUID(), "0000-GA-TILTAK-ANSVARLIG"),
				AdGruppe(UUID.randomUUID(), "0000-GA-TILTAK-ENDRINGSMELDING"),
				AdGruppe(UUID.randomUUID(), "0000-GA-123-ABC"),
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/autentisering/meg")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		val expectedJson = """
			{"navIdent":"Z1234","navn":"Veileder Veiledersen","tilganger":["FLATE","ENDRINGSMELDING"]}
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

}
