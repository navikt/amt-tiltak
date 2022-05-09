package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.*
import no.nav.amt.tiltak.core.port.DeltakerService
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

	@MockBean
	private lateinit var navAnsattService: NavAnsattService

	@MockBean
	private lateinit var deltakerService: DeltakerService

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
					mellomnavn = null,
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
			[{"id":"$endringsmeldingId","bruker":{"fornavn":"Fornavn","mellomnavn":null,"etternavn":"Etternavn","fodselsnummer":"487329"},"startDato":"2022-05-05","aktiv":true,"godkjent":false,"arkivert":false,"opprettetAvArrangorAnsatt":{"fornavn":"Test","mellomnavn":null,"etternavn":"Test"},"opprettetDato":"2022-05-05T08:52:18.314953"}]
		""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)
	}

	@Test
	fun `markerFerdig() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/endringsmelding/${UUID.randomUUID()}/ferdig")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `markerFerdig() - skal returnere 200 og markere som ferdig`() {
		val endringsmeldingId = UUID.randomUUID()
		val gjennomforingId = UUID.randomUUID()
		val deltakerId = UUID.randomUUID()
		val navAnsattId = UUID.randomUUID()
		val navIdent = "a12345"

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(endringsmeldingService.hentEndringsmelding(endringsmeldingId))
			.thenReturn(EndringsmeldingDbo(
				id = endringsmeldingId,
				deltakerId = deltakerId,
				startDato = null,
				godkjentAvNavAnsatt = null,
				aktiv = true,
				opprettetAvId = UUID.randomUUID(),
				createdAt = LocalDateTime.now(),
				modifiedAt = LocalDateTime.now()
			))

		Mockito.`when`(deltakerService.hentDeltaker(deltakerId))
			.thenReturn(Deltaker(
				id = UUID.randomUUID(),
				gjennomforingId = gjennomforingId,
				bruker = null,
				startDato = null,
				sluttDato = null,
				statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(Deltaker.Status.DELTAR))),
				registrertDato = LocalDateTime.now(),
				dagerPerUke = null,
				prosentStilling = null,
			))

		Mockito.`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId))
			.thenReturn(true)

		Mockito.`when`(navAnsattService.getNavAnsatt(navIdent))
			.thenReturn(NavAnsatt(
				id = navAnsattId,
				navIdent = navIdent,
				navn = "",
				epost = null,
				telefonnummer = null,
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.patch("/api/nav-ansatt/endringsmelding/$endringsmeldingId/ferdig")
				.header("Authorization", "Bearer ${azureAdToken()}")
		).andReturn().response

		assertEquals(200, response.status)

		verify(endringsmeldingService, times(1)).markerSomFerdig(endringsmeldingId, navAnsattId)
	}

}
