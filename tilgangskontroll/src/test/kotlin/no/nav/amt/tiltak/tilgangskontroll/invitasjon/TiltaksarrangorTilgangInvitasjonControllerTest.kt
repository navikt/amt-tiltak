package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.AuthService
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
@WebMvcTest(controllers = [TiltaksarrangorTilgangInvitasjonController::class])
class TiltaksarrangorTilgangInvitasjonControllerTest {

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
	private lateinit var tilgangInvitasjonService: TilgangInvitasjonService

	@MockBean
	private lateinit var authService: AuthService

	@Test
	fun `hentInvitasjonInfo() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/tilgang/invitasjon/${UUID.randomUUID()}/info")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentInvitasjonInfo() - skal returnere 200 og riktig json`() {
		val invitasjonId = UUID.randomUUID()
		val gyldigTil = ZonedDateTime.parse("2022-04-05T09:40:51.81556+02:00")

		`when`(tilgangInvitasjonService.hentInvitasjonInfo(invitasjonId))
			.thenReturn(InvitasjonInfoDbo(
				overordnetEnhetNavn = "Enhet AS",
				gjennomforingNavn = "Gjennomforing",
				erBrukt = true,
				gyldigTil = gyldigTil
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/tilgang/invitasjon/$invitasjonId/info")
				.header("Authorization", "Bearer ${tokenXToken()}")
		).andReturn().response

		val expectedJson = """
			{"overordnetEnhetNavn":"Enhet AS","gjennomforingNavn":"Gjennomforing","erBrukt":true,"gyldigTil":"2022-04-05T09:40:51.81556+02:00"}
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

	@Test
	fun `aksepterInvitasjon() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/tiltaksarrangor/tilgang/invitasjon/${UUID.randomUUID()}/aksepter")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `aksepterInvitasjon() - skal returnere 200 og akseptere invitasjon`() {
		val personligIdent = "1235435646"
		val invitasjonId = UUID.randomUUID()

		`when`(authService.hentPersonligIdentTilInnloggetBruker()).thenReturn(personligIdent)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/tiltaksarrangor/tilgang/invitasjon/$invitasjonId/aksepter")
				.header("Authorization", "Bearer ${tokenXToken()}")
		).andReturn().response

		verify(tilgangInvitasjonService, times(1)).aksepterInvitasjon(invitasjonId, personligIdent)

		assertEquals(200, response.status)
	}

}
