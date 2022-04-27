package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.GjennomforingTilgang
import no.nav.amt.tiltak.core.port.NavAnsattTilgangService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
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
@WebMvcTest(controllers = [NavAnsattArrangorAnsattTilgangController::class])
class NavAnsattArrangorAnsattTilgangControllerTest {

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

	@MockBean
	private lateinit var navAnsattTilgangService: NavAnsattTilgangService

	@MockBean
	private lateinit var hentArrangorAnsattTilgangerQuery: HentArrangorAnsattTilgangerQuery

	@MockBean
	private lateinit var gjennomforingTilgangService: GjennomforingTilgangService

	@Test
	fun `hentTilganger() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang?gjennomforingId=${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentTilganger() - skal returnere 200 med korrekt response`() {
		val gjennomforingId = UUID.randomUUID()
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		`when`(navAnsattTilgangService.harTiltaksansvarligTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		`when`(hentArrangorAnsattTilgangerQuery.query(gjennomforingId))
			.thenReturn(listOf(HentArrangorAnsattTilgangerQuery.ArrangorAnsattGjennomforingTilgangDbo(
				id = UUID.fromString("c6f33ed2-62b8-4c63-b496-318040ab193c"),
				fornavn = "Fornavn",
				mellomnavn = null,
				etternavn = "Etternavn",
				opprettetDato = ZonedDateTime.parse("2022-04-26T10:32:55.804095+02:00"),
				opprettetAvNavIdent = "Z98765"
			)))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		val expectedJson = """
			[{"id":"c6f33ed2-62b8-4c63-b496-318040ab193c","fornavn":"Fornavn","mellomnavn":null,"etternavn":"Etternavn","opprettetDato":"2022-04-26T10:32:55.804095+02:00","opprettetAvNavIdent":"Z98765"}]
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

	@Test
	fun `stopTilgang() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/${UUID.randomUUID()}/stop")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `stopTilgang() - skal returnere 200 og stoppe tilgang`() {
		val navAnsattIdent = "Z1234"
		val tilgangId = UUID.randomUUID()

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		`when`(gjennomforingTilgangService.hentTilgang(tilgangId))
			.thenReturn(GjennomforingTilgang(
				id = tilgangId,
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				createdAt = ZonedDateTime.now(),
			))

		`when`(navAnsattTilgangService.harTiltaksansvarligTilgangTilGjennomforing(navAnsattIdent, GJENNOMFORING_1.id))
			.thenReturn(true)

		`when`(veilederService.getOrCreateVeileder(navAnsattIdent))
			.thenReturn(NavAnsatt(
				id = NAV_ANSATT_1.id,
				navIdent = navAnsattIdent,
				navn = "",
				epost = null,
				telefonnummer = null,
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/arrangor-ansatt-tilgang/$tilgangId/stop")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		assertEquals(200, response.status)

		verify(gjennomforingTilgangService, times(1)).stopTilgang(tilgangId, NAV_ANSATT_1.id)
	}

}
