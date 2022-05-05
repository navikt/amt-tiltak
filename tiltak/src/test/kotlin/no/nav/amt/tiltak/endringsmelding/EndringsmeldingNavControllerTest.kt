package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [EndringsmeldingNavController::class])
class EndringsmeldingNavControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var tiltaksansvarligTilgangService: TiltaksansvarligTilgangService

	@MockBean
	private lateinit var endringsmeldingService: EndringsmeldingService

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {
		val gjennomforingId = UUID.randomUUID()
		val navIdent = "a12345"

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId))
			.thenReturn(false)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken("test", "test")}")
		).andReturn().response

		assertEquals(403, response.status)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med riktig response`() {
		val gjennomforingId = UUID.randomUUID()
		val endringsmeldingId = UUID.randomUUID()
		val navIdent = "a12345"

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId))
			.thenReturn(true)

		Mockito.`when`(endringsmeldingService.hentEndringsmeldinger(gjennomforingId))
			.thenReturn(listOf(Endringsmelding(
				id = endringsmeldingId,
				bruker = Bruker(
					id = UUID.randomUUID(),
					fornavn = "Fornavn",
					mellomnavn = null,
					etternavn = "Etternavn",
					fodselsnummer = "487329",
					navEnhet = null
				),
				startDato = LocalDate.parse("2022-05-05"),
				aktiv = true,
				godkjent = false,
				arkivert = false,
				opprettetAvArrangorAnsatt = Ansatt(
					id = UUID.randomUUID(),
					personligIdent = "1234567890",
					fornavn = "Test",
					etternavn = "Test",
					arrangorer = emptyList()
				),
				opprettetDato = LocalDateTime.parse("2022-05-05T08:52:18.314953")
			)))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken("test", "test")}")
		).andReturn().response

		val expectedJson = """
			[{"id":"$endringsmeldingId","bruker":{"fornavn":"Fornavn","mellomnavn":null,"etternavn":"Etternavn","fodselsnummer":"487329"},"startDato":"2022-05-05","aktiv":true,"godkjent":false,"arkivert":false,"opprettetAvArrangorAnsatt":{"fornavn":"Test","etternavn":"Test"},"opprettetDato":"2022-05-05T08:52:18.314953"}]
		""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)
	}

}
