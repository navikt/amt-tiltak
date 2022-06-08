package no.nav.amt.tiltak.tiltak.controllers

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
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
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
		registrertDato = LocalDateTime.now()
	)

	val tiltak = Tiltak(
		id = UUID.randomUUID(),
		navn = "tiltaksnavn",
		kode = "kode"
	)
	val gjennomforing = Gjennomforing(
		id = UUID.randomUUID(),
		tiltak = tiltak,
		arrangor = Arrangor(UUID.randomUUID(), "", "", "", ""),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		startDato = LocalDate.now(),
		registrertDato = LocalDateTime.now(),
		navEnhetId = null,
		sluttDato = LocalDate.now(),
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
	fun `hentGjennomforingerByArrangorId() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing")
				.queryParam("arrangorId", "test")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforingerByArrangorId() should return 200 when authenticated`() {
		val token = tokenXToken("test", "test")

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing")
				.queryParam("arrangorId", UUID.randomUUID().toString())
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentGjennomforing() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforing() should return 200 when authenticated`() {
		val token = tokenXToken("test", "test")

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId)).thenReturn(gjennomforing)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
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
	fun `hentDeltakere() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/gjennomforing/ID/deltakere")
		).andReturn().response

		assertEquals(401, response.status)
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

		assertEquals(200, response.status)
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
