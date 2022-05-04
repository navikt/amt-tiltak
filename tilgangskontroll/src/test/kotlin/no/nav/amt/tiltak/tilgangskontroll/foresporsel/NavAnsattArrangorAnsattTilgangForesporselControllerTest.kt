package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [NavAnsattArrangorAnsattTilgangForesporselController::class])
class NavAnsattArrangorAnsattTilgangForesporselControllerTest {

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
	private lateinit var tilgangForesporselService: TilgangForesporselService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var veilederService: VeilederService

	@MockBean
	private lateinit var tiltaksansvarligTilgangService: TiltaksansvarligTilgangService

	@Test
	fun `hentUbesluttedeForesporsler() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/ubesluttet")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentUbesluttedeForesporsler() - skal returnere 200 med data`() {
		val foresporselId = UUID.fromString("9e84392d-4b8d-463d-8509-216e573f975d")
		val gjennomforingId = UUID.randomUUID()
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker()).thenReturn(navAnsattIdent)

		`when`(tilgangForesporselService.hentUbesluttedeForesporsler(gjennomforingId))
			.thenReturn(listOf(TilgangForesporselDbo(
				id = foresporselId,
				personligIdent = "1234",
				fornavn = "Test",
				mellomnavn = null,
				etternavn = "Testersen",
				gjennomforingId = gjennomforingId,
				beslutningAvNavAnsattId = UUID.randomUUID(),
				tidspunktBeslutning = ZonedDateTime.now(),
				beslutning = Beslutning.GODKJENT,
				gjennomforingTilgangId = UUID.randomUUID(),
				createdAt = ZonedDateTime.parse("2022-04-04T17:30:46.114332+02:00")
			)))

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/ubesluttet?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		val expectedJson = """
			[{"id":"9e84392d-4b8d-463d-8509-216e573f975d","fornavn":"Test","mellomnavn":null,"etternavn":"Testersen","fodselsnummer":"1234","opprettetDato":"2022-04-04T17:30:46.114332+02:00"}]
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

	@Test
	fun `godkjennForesporsel() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/${UUID.randomUUID()}/godkjenn")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `godkjennForesporsel() - skal godkjenne foresporsel og returnere 200`() {
		val foresporselId = UUID.randomUUID()
		val gjennomforingId = UUID.randomUUID()
		val navAnsattId = UUID.randomUUID()
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker()).thenReturn(navAnsattIdent)

		`when`(veilederService.getOrCreateVeileder(navAnsattIdent))
			.thenReturn(NavAnsatt(
				id = navAnsattId,
				navIdent = navAnsattIdent,
				epost = "",
				navn = "",
				telefonnummer = ""
			))

		`when`(tilgangForesporselService.hentForesporsel(foresporselId))
			.thenReturn(TilgangForesporselDbo(
				id = foresporselId,
				personligIdent = "",
				fornavn = "",
				mellomnavn = "",
				etternavn = "",
				gjennomforingId = gjennomforingId,
				beslutningAvNavAnsattId = null,
				tidspunktBeslutning = null,
				beslutning = null,
				gjennomforingTilgangId = null,
				createdAt = ZonedDateTime.now(),
			))

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/$foresporselId/godkjenn")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		verify(tilgangForesporselService, times(1)).godkjennForesporsel(foresporselId, navAnsattId)

		assertEquals(200, response.status)
	}

	@Test
	fun `avvisForesporsel() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/${UUID.randomUUID()}/avvis")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `avvisForesporsel() - skal avvise foresporsel og returnere 200`() {
		val foresporselId = UUID.randomUUID()
		val gjennomforingId = UUID.randomUUID()
		val navAnsattId = UUID.randomUUID()
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker()).thenReturn(navAnsattIdent)

		`when`(veilederService.getOrCreateVeileder(navAnsattIdent))
			.thenReturn(NavAnsatt(
				id = navAnsattId,
				navIdent = navAnsattIdent,
				epost = "",
				navn = "",
				telefonnummer = ""
			))

		`when`(tilgangForesporselService.hentForesporsel(foresporselId))
			.thenReturn(TilgangForesporselDbo(
				id = foresporselId,
				personligIdent = "",
				fornavn = "",
				mellomnavn = "",
				etternavn = "",
				gjennomforingId = gjennomforingId,
				beslutningAvNavAnsattId = null,
				tidspunktBeslutning = null,
				beslutning = null,
				gjennomforingTilgangId = null,
				createdAt = ZonedDateTime.now(),
			))

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel/$foresporselId/avvis")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		verify(tilgangForesporselService, times(1)).avvisForesporsel(foresporselId, navAnsattId)

		assertEquals(200, response.status)
	}

}
