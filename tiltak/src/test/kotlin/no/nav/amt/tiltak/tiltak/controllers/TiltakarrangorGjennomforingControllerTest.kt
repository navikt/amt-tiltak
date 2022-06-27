package no.nav.amt.tiltak.tiltak.controllers

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import no.nav.amt.tiltak.tiltak.repositories.HentGjennomforingerFraArrangorerQuery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
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
@WebMvcTest(controllers = [TiltakarrangorGjennomforingController::class])
class TiltakarrangorGjennomforingControllerTest {
	private val gjennomforingId = UUID.fromString("e68d54e2-47b5-11ec-81d3-0242ac130003")

	private val fnr = "fnr"

	@MockBean
	private lateinit var gjennomforingService: GjennomforingService

	@MockBean
	private lateinit var deltakerService: DeltakerService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	@MockBean
	private lateinit var hentGjennomforingerFraArrangorerQuery: HentGjennomforingerFraArrangorerQuery

	@Autowired
	private lateinit var mockMvc: MockMvc

	val status = DeltakerStatus(
				id = UUID.randomUUID(),
				type = Deltaker.Status.DELTAR,
				gyldigFra = LocalDateTime.now(),
				aktiv = true,
				opprettetDato = LocalDateTime.now()
			)

	val deltakerDbo = DeltakerDbo(
		id = UUID.randomUUID(),
		brukerId = UUID.randomUUID(),
		brukerFodselsnummer = "12129312375",
		brukerFornavn = "Fornavn",
		brukerEtternavn = "Etternavn",
		gjennomforingId = gjennomforingId,
		startDato = LocalDate.now(),
		sluttDato = LocalDate.now(),
		dagerPerUke = 1,
		prosentStilling = 10.343f,
		createdAt = LocalDateTime.now(),
		modifiedAt = LocalDateTime.now(),
		registrertDato = LocalDateTime.now(),
		innsokBegrunnelse = null
	)

	val tiltak = Tiltak(
		id = UUID.randomUUID(),
		navn = "tiltaksnavn",
		kode = "kode"
	)
	val gjennomforing = Gjennomforing(
		id = gjennomforingId,
		tiltak = tiltak,
		arrangor = Arrangor(UUID.randomUUID(), "", "", "", ""),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		startDato = LocalDate.parse("2022-06-27"),
		registrertDato = LocalDateTime.now(),
		navEnhetId = null,
		sluttDato = LocalDate.parse("2022-06-28"),
		status = Gjennomforing.Status.GJENNOMFORES,
		lopenr = 123,
		opprettetAar = 2020
	)

	@BeforeEach
	fun before() {
		MockitoAnnotations.openMocks(this)

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker())
			.thenReturn(fnr)
	}

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	@Test
	fun `should return 401 for all endpoints when not authenticated`() {
		val requests = listOf(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing")
				.queryParam("arrangorId", "test"),

			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId"),

			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/ID/deltakere")
		)

		requests
			.map { mockMvc.perform(it).andReturn().response }
			.forEach { it.status shouldBe 401 }
	}

	@Test
	fun `hentGjennomforingerByArrangorId() should return 200 when authenticated`() {
		val token = tokenXToken("test", "test")

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing")
				.queryParam("arrangorId", UUID.randomUUID().toString())
				.header("Authorization", "Bearer $token")
		).andReturn().response

		response.status shouldBe 200
	}

	@Test
	fun `hentTilgjengeligeGjennomforinger() should return 200 with correct response`() {
		val token = tokenXToken("test", "test")

		val virksomhetsnummere = listOf("123543")

		Mockito.`when`(
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(fnr)
		).thenReturn(virksomhetsnummere)

		Mockito.`when`(
			hentGjennomforingerFraArrangorerQuery.query(virksomhetsnummere)
		).thenReturn(listOf(gjennomforingId))

		Mockito.`when`(
			gjennomforingService.getGjennomforinger(listOf(gjennomforingId))
		).thenReturn(listOf(gjennomforing))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/tilgjengelig")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		val expectedJson = """
			[{"id":"e68d54e2-47b5-11ec-81d3-0242ac130003","navn":"tiltaksnavn","startDato":"2022-06-27","sluttDato":"2022-06-28","status":"GJENNOMFORES","tiltak":{"tiltakskode":"kode","tiltaksnavn":"tiltaksnavn"},"arrangor":{"virksomhetNavn":"","organisasjonNavn":""}}]
		""".trimIndent()

		response.status shouldBe 200
		response.contentAsString shouldBe expectedJson
	}

	@Test
	fun `opprettTilgangTilGjennomforing() should return 200`() {
		val token = tokenXToken("test", "test")

		val virksomhetsnummere = listOf("123543")

		Mockito.`when`(
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(fnr)
		).thenReturn(virksomhetsnummere)

		Mockito.`when`(
			hentGjennomforingerFraArrangorerQuery.query(virksomhetsnummere)
		).thenReturn(listOf(gjennomforingId))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/tiltaksarrangor/gjennomforing/$gjennomforingId/tilgang")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		response.status shouldBe 200
		response.contentAsString shouldBe ""

		verify(arrangorAnsattTilgangService, times(1)).opprettTilgang(fnr, gjennomforingId)
	}

	@Test
	fun `opprettTilgangTilGjennomforing() should return 403 if not authorized`() {
		val token = tokenXToken("test", "test")

		val virksomhetsnummere = listOf("123543")

		Mockito.`when`(
			arrangorAnsattTilgangService.hentVirksomhetsnummereMedKoordinatorRettighet(fnr)
		).thenReturn(virksomhetsnummere)

		Mockito.`when`(
			hentGjennomforingerFraArrangorerQuery.query(virksomhetsnummere)
		).thenReturn(emptyList())

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/api/tiltaksarrangor/gjennomforing/$gjennomforingId/tilgang")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		response.status shouldBe 403
		response.contentAsString shouldBe ""
	}

	@Test
	fun `hentGjennomforing() should return 200 when authenticated`() {
		val token = tokenXToken("test", "test")

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId)).thenReturn(gjennomforing)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		response.status shouldBe 200
	}

	@Test
	fun `hentGjennomforing() should perform authorization check`() {
		val token = tokenXToken("test", "test")

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId)).thenReturn(gjennomforing)

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		verify(arrangorAnsattTilgangService).verifiserTilgangTilGjennomforing(
			eq(fnr), eq(gjennomforingId)
		)
	}

	@Test
	fun `hentDeltakere() should return 200 when authenticated`() {
		val deltaker = deltakerDbo.toDeltaker(status)

		val token = tokenXToken("test", "test")

		Mockito.`when`(deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)).thenReturn(listOf(deltaker))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId/deltakere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		response.status shouldBe 200
	}

	@Test
	fun `hentDeltakere() should perform authorization check`() {
		val deltaker = deltakerDbo.toDeltaker(status)

		val token = tokenXToken("test", "test")

		Mockito.`when`(deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)).thenReturn(listOf(deltaker))

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId/deltakere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		verify(arrangorAnsattTilgangService).verifiserTilgangTilGjennomforing(
			eq(fnr), eq(gjennomforingId)
		)
	}
}
