package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.time.ZonedDateTime
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [NavAnsattArrangorAnsattTilgangInvitasjonController::class])
class NavAnsattArrangorAnsattTilgangInvitasjonControllerTest {

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	private val objectMapper = jacksonObjectMapper()

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var tilgangInvitasjonService: TilgangInvitasjonService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var veilederService: VeilederService

	@MockBean
	private lateinit var tiltaksansvarligTilgangService: TiltaksansvarligTilgangService

	@Test
	fun `hentUbrukteInvitasjoner() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon/ubrukt?gjennomforingId=${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentUbrukteInvitasjoner() - skal returnere 200 med riktig json`() {
		val gjennomforingId = UUID.randomUUID()
		val invitasjonId = UUID.fromString("885ede1b-c940-4b2b-9023-4fc5992888e7")
		val opprettetDato = ZonedDateTime.parse("2022-04-05T09:06:07.650476+02:00")
		val gyldigTilDato = ZonedDateTime.parse("2022-04-08T09:06:07.650476+02:00")
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		`when`(tilgangInvitasjonService.hentUbrukteInvitasjoner(gjennomforingId))
			.thenReturn(listOf(
				UbruktInvitasjonDbo(
					id = invitasjonId,
					opprettetAvNavIdent = "Z1234",
					opprettetDato = opprettetDato,
					gyldigTilDato = gyldigTilDato,
				)
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon/ubrukt?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		val expectedJson = """
			[{"id":"885ede1b-c940-4b2b-9023-4fc5992888e7","opprettetAvNavIdent":"Z1234","opprettetDato":"2022-04-05T09:06:07.650476+02:00","gyldigTilDato":"2022-04-08T09:06:07.650476+02:00"}]
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

	@Test
	fun `opprettInvitasjon() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `opprettInvitasjon() - skal returnere 204 og opprette invitasjon`() {
		val gjennomforingId = UUID.randomUUID()
		val navAnsattId = UUID.randomUUID()
		val navAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navAnsattIdent)

		`when`(veilederService.getOrCreateVeileder(navAnsattIdent))
			.thenReturn(NavAnsatt(
				id = navAnsattId,
				navIdent = navAnsattIdent,
				epost = "",
				navn = "",
				telefonnummer = ""
			))

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navAnsattIdent, gjennomforingId))
			.thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon")
				.header("Authorization", "Bearer ${azureAdToken()}")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					NavAnsattArrangorAnsattTilgangInvitasjonController.OpprettInvitasjonRequest(gjennomforingId))
				)
		).andReturn().response

		verify(tilgangInvitasjonService, times(1)).opprettInvitasjon(gjennomforingId, navAnsattId)

		assertEquals(201, response.status)
	}

	@Test
	fun `slettInvitasjon() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.delete("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon/${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `slettInvitasjon() - skal returnere 200 og slette invitasjon`() {
		val invitasjonId = UUID.randomUUID()
		val gjennomforingId = UUID.randomUUID()
		val innloggetNavAnsattIdent = "Z1234"

		`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(innloggetNavAnsattIdent)

		`when`(tilgangInvitasjonService.hentInvitasjon(invitasjonId))
			.thenReturn(TilgangInvitasjonDbo(
				id = UUID.randomUUID(),
				gjennomforingId = gjennomforingId,
				gyldigTil = ZonedDateTime.now(),
				opprettetAvNavAnsattId = UUID.randomUUID(),
				erBrukt = false,
				tidspunktBrukt = null,
				tilgangForesporselId = null,
				createdAt = ZonedDateTime.now()
			))

		`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(innloggetNavAnsattIdent, gjennomforingId))
			.thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.delete("/api/nav-ansatt/arrangor-ansatt-tilgang/invitasjon/${invitasjonId}")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		verify(tilgangInvitasjonService, times(1)).slettInvitasjon(invitasjonId)

		assertEquals(200, response.status)
	}

}
