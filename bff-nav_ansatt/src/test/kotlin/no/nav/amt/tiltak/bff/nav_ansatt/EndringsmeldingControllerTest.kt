package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
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
@WebMvcTest(controllers = [EndringsmeldingController::class])
class EndringsmeldingControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var arrangorAnsattService: ArrangorAnsattService

	@MockBean
	private lateinit var tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService

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

	val bruker = Bruker(
		id = BRUKER_1.id,
		fornavn = BRUKER_1.fornavn,
		mellomnavn = BRUKER_1.mellomnavn,
		etternavn = BRUKER_1.etternavn,
		telefonnummer = BRUKER_1.telefonnummer,
		epost = BRUKER_1.epost,
		fodselsnummer = BRUKER_1.fodselsnummer,
		navEnhet = null,
		navVeilederId = UUID.randomUUID(),
	)

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding")
		).andReturn().response

		assertEquals(401, response.status)
	}

//  Reimplementer denne testen som en integrasjonstest slik at vi får testet at tilgangskontroll fungerer
//	@Test
//	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {
//		val gjennomforingId = UUID.randomUUID()
//		val navIdent = "a12345"
//
//		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
//			.thenReturn(navIdent)
//
//		Mockito.`when`(tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId))
//			.thenReturn(false)
//
//		val response = mockMvc.perform(
//			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding?gjennomforingId=$gjennomforingId")
//				.header("Authorization", "Bearer ${azureAdToken("test", "test")}")
//		).andReturn().response
//
//		assertEquals(403, response.status)
//	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med riktig response`() {
		val gjennomforingId = UUID.randomUUID()
		val endringsmeldingId = UUID.randomUUID()
		val navIdent = "a12345"
		val deltakerId = UUID.randomUUID()
		val startDato = LocalDate.parse("2022-05-05")

		val deltaker = Deltaker(
			id = UUID.randomUUID(),
			gjennomforingId = UUID.randomUUID(),
			bruker = bruker,
			startDato = null,
			sluttDato = null,
			status = DeltakerStatus(
				id = UUID.randomUUID(),
				type = Deltaker.Status.DELTAR,
				gyldigFra = LocalDateTime.now().plusDays(1),
				opprettetDato = LocalDateTime.now().minusDays(1),
				aktiv = true,
			),
			registrertDato = LocalDateTime.now().minusDays(2),
		    dagerPerUke = null,
			prosentStilling = null,
			innsokBegrunnelse = null,
		)

		val ansatt = Ansatt(
			id = UUID.randomUUID(),
			personligIdent = "123",
			fornavn = "Ansatt",
			mellomnavn = null,
			etternavn = "Ettersatt",
			arrangorer = emptyList(),
		)

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(deltakerService.hentDeltaker(deltakerId))
			.thenReturn(deltaker)

		Mockito.`when`(arrangorAnsattService.getAnsatt(ansatt.id))
			.thenReturn(ansatt)

		Mockito.`when`(endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId))
			.thenReturn(listOf(Endringsmelding(
				id = endringsmeldingId,
				deltakerId = deltakerId,
				startDato = startDato,
				ferdiggjortAvNavAnsattId = null,
				ferdiggjortTidspunkt = null,
				aktiv = true,
				opprettetAvArrangorAnsattId = ansatt.id,
				opprettet = LocalDateTime.parse("2022-05-05T08:52:18.314953")
			)))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/endringsmelding?gjennomforingId=$gjennomforingId")
				.header("Authorization", "Bearer ${azureAdToken("test", "test")}")
		).andReturn().response

		val expectedJson = """
			[{"id":"$endringsmeldingId","bruker":{"fornavn":"${bruker.fornavn}","mellomnavn":${bruker.mellomnavn},"etternavn":"${bruker.etternavn}","fodselsnummer":"${bruker.fodselsnummer}"},"startDato":"$startDato","aktiv":true,"godkjent":false,"arkivert":false,"opprettetAvArrangorAnsatt":{"fornavn":"${ansatt.fornavn}","mellomnavn":${ansatt.mellomnavn},"etternavn":"${ansatt.etternavn}"},"opprettetDato":"2022-05-05T08:52:18.314953"}]
			""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)

		verify(tiltaksansvarligAutoriseringService, times(1)).verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)
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
			.thenReturn(Endringsmelding(
				id = endringsmeldingId,
				deltakerId = deltakerId,
				startDato = null,
				ferdiggjortAvNavAnsattId = null,
				ferdiggjortTidspunkt = null,
				aktiv = true,
				opprettetAvArrangorAnsattId = UUID.randomUUID(),
				opprettet = LocalDateTime.now(),
			))

		Mockito.`when`(deltakerService.hentDeltaker(deltakerId))
			.thenReturn(Deltaker(
				id = UUID.randomUUID(),
				gjennomforingId = gjennomforingId,
				bruker = bruker,
				startDato = null,
				sluttDato = null,
				status = DeltakerStatus(UUID.randomUUID(), Deltaker.Status.DELTAR, LocalDateTime.now(), LocalDateTime.now(), true),
				registrertDato = LocalDateTime.now(),
				dagerPerUke = null,
				prosentStilling = null,
			))

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
		verify(tiltaksansvarligAutoriseringService, times(1)).verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)
	}

}
