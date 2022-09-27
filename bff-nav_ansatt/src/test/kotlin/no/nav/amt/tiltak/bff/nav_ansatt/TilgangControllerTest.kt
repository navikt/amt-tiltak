package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [TilgangController::class])
class TilgangControllerTest {

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
	private lateinit var tiltaksansvarligTilgangService: TiltaksansvarligTilgangService

	@Test
	fun `giTilgangTilGjennomforing() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `giTilgangTilGjennomforing() - skal returnere 200 og gi tilgang til gjennomføring`() {
		val gjennomforingId = UUID.randomUUID()

		val navAnsattIdent = "Z1234"
		val navAnsattId = UUID.randomUUID()

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		Mockito.`when`(navAnsattService.getNavAnsatt(navAnsattIdent))
			.thenReturn(NavAnsatt(
				navAnsattId,
				navAnsattIdent,
				"",
				"",
				""
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		assertEquals(200, response.status)

		verify(tiltaksansvarligTilgangService, times(1)).giTilgangTilGjennomforing(navAnsattId, gjennomforingId)
	}

	@Test
	fun `stopTilgangTilGjennomforing() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/tiltaksansvarlig/gjennomforing-tilgang/stop?gjennomforingId=${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `stopTilgangTilGjennomforing() - skal returnere 200 og stoppe tilgang til gjennomføring`() {
		val gjennomforingId = UUID.randomUUID()

		val navAnsattIdent = "Z1234"
		val navAnsattId = UUID.randomUUID()

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		Mockito.`when`(navAnsattService.getNavAnsatt(navAnsattIdent))
			.thenReturn(NavAnsatt(
				navAnsattId,
				navAnsattIdent,
				"",
				"",
				""
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/tiltaksansvarlig/gjennomforing-tilgang/stop?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		assertEquals(200, response.status)

		verify(tiltaksansvarligTilgangService, times(1)).stopTilgangTilGjennomforing(navAnsattId, gjennomforingId)
	}

}
