package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
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
@WebMvcTest(controllers = [GjennomforingController::class])
class GjennomforingControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var endringsmeldingService: EndringsmeldingService

	@MockBean
	private lateinit var gjennomforingService: GjennomforingService

	@MockBean
	private lateinit var tiltaksansvarligTilgangService: TiltaksansvarligTilgangService

	@MockBean
	private lateinit var tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	val tiltak = Tiltak(
		id = UUID.randomUUID(),
		navn = "tiltaksnavn",
		kode = "kode"
	)
	val gjennomforing = Gjennomforing(
		id = GJENNOMFORING_1.id,
		tiltak = tiltak,
		arrangor = Arrangor(ARRANGOR_1.id,
			"Navn",
			"1234",
			"5678",
			"Orgnavn"
		),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		startDato = LocalDate.parse("2022-05-03"),
		registrertDato = LocalDateTime.now(),
		navEnhetId = NAV_ENHET_1.id,
		sluttDato = LocalDate.parse("2022-05-03"),
		status = Gjennomforing.Status.GJENNOMFORES,
		lopenr = 123,
		opprettetAar = 2020
	)

	@BeforeEach
	fun before() {
		MockitoAnnotations.openMocks(this)
	}

	@Test
	fun `hentGjennomforinger() - sender med tokenx-token - skal returnere 401`() {
		val token = tokenXToken("test", "test")

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforinger() - sender med azure ad-token - skal returnere 200`() {
		val token = azureAdToken("test", "test")
		val navIdent = "a12345"

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(tiltaksansvarligTilgangService.hentAktiveTilganger(navIdent))
			.thenReturn(emptyList())

		Mockito.`when`(gjennomforingService.getGjennomforinger(any()))
			.thenReturn(emptyList())

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentGjennomforing() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing/${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforing() - skal returnere 200`() {
		val token = azureAdToken("test", "test")
		val navIdent = "a12345"
		val gjennomforingId = UUID.randomUUID()

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId))
			.thenReturn(gjennomforing)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		val expectedJson = """
			{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"tiltaksnavn","tiltakNavn":"tiltaksnavn","startDato":"2022-05-03","sluttDato":"2022-05-03","arrangor":{"virksomhetNavn":"Navn","organisasjonNavn":"Orgnavn"},"lopenr":123,"opprettetAr":2020}
		""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)

		verify(tiltaksansvarligAutoriseringService, times(1)).verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)
	}


//	Reimplementer denne testen som en integrasjonstest slik at vi får testet at tilgangskontroll fungerer
//	@Test
//	fun `hentGjennomforing() - skal returnere 403 hvis ikke tilgang til enhet`() {
//		val token = azureAdToken("test", "test")
//		val navIdent = "a12345"
//		val gjennomforingId = UUID.randomUUID()
//
//		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
//			.thenReturn(navIdent)
//
//		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId))
//			.thenReturn(gjennomforing)
//
//		val response = mockMvc.perform(
//			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing/$gjennomforingId")
//				.header("Authorization", "Bearer $token")
//		).andReturn().response
//
//		assertEquals(403, response.status)
//	}

	@Test
	fun `hentGjennomforingerMedLopenr() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing?lopenr=123")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforingerMedLopenr() - skal returnere 200 og gjennomføringer med matchende løpenummer`() {
		val lopenr = GJENNOMFORING_1.lopenr
		Mockito.`when`(gjennomforingService.getAktiveByLopenr(lopenr))
			.thenReturn(listOf(GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing?lopenr=$lopenr")
				.header("Authorization", "Bearer ${azureAdToken("test", "test")}")
		).andReturn().response

		val expectedJson = """
			[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","lopenr":123,"opprettetAr":2020,"arrangorNavn":"Org TiltaksarrangÃ¸r 1"}]
		""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)
	}
}
