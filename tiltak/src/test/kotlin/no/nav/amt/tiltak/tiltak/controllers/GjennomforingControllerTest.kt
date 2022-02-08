package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
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
	private val gjennomforingId = UUID.fromString("e68d54e2-47b5-11ec-81d3-0242ac130003")

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

	val statusConverterMock = fun (id: UUID) =
		listOf(
			DeltakerStatusDbo(
				deltakerId = id,
				status = Deltaker.Status.DELTAR,
				endretDato = LocalDateTime.now(),
				aktiv = true)
		)

	val tiltak = Tiltak(
		id = UUID.randomUUID(),
		navn = "tiltaksnavn",
		kode = "kode"
	)
	val gjennomforing = Gjennomforing(
		id = UUID.randomUUID(),
		tiltak = tiltak,
		arrangorId = UUID.randomUUID(),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		startDato = LocalDate.now(),
		registrertDato = LocalDateTime.now(),
		sluttDato = LocalDate.now(),
		status = Gjennomforing.Status.GJENNOMFORES,
	)

	@BeforeEach
	fun before() {
		MockitoAnnotations.openMocks(this)
	}

	companion object {
		private val server = MockOAuth2Server()

		init {
			server.start()
			System.setProperty("MOCK_TOKEN_X_DISCOVERY_URL", server.wellKnownUrl("tokenx").toString())
		}

		@AfterAll
		@JvmStatic
		fun cleanup() {
			server.shutdown()
		}
	}

	@Test
	fun `hentGjennomforingerByArrangorId() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing")
				.queryParam("arrangorId", "test")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforingerByArrangorId() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing")
				.queryParam("arrangorId", UUID.randomUUID().toString())
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentGjennomforing() should return 401 when not authenticated`() {

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing/$gjennomforingId")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforinger() should return 200 when authenticated`() {
		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId)).thenReturn(gjennomforing)
		val token = server.issueToken("tokenx", "test", "test").serialize()
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentDeltakere() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing/ID/deltakere")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentDeltakere() should return 200 when authenticated`() {
		val deltaker = DeltakerDbo(
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
		).toDeltaker(statusConverterMock)
		Mockito.`when`(deltakerService.hentDeltakerePaaGjennomforing(gjennomforingId)).thenReturn(listOf(deltaker))
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/gjennomforing/$gjennomforingId/deltakere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}
}
