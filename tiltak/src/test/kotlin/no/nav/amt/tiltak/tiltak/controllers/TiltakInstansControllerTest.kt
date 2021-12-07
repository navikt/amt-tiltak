package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.TiltakInstansService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
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
@WebMvcTest(controllers = [no.nav.amt.tiltak.tiltak.controllers.TiltakInstansController::class])
class TiltakInstansControllerTest {
	private val tiltakInstansId = UUID.fromString("e68d54e2-47b5-11ec-81d3-0242ac130003")

	@MockBean
	private lateinit var tiltakInstansService: TiltakInstansService

	@MockBean
	private lateinit var deltakerService: DeltakerService

	@Autowired
	private lateinit var mockMvc: MockMvc

	val tiltak = Tiltak(
		id = UUID.randomUUID(),
		navn = "tiltaksnavn",
		kode = "kode"
	)
	val tiltakInstans = TiltakInstans(
		id = UUID.randomUUID(),
		tiltak = tiltak,
		arrangorId = UUID.randomUUID(),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		oppstartDato = LocalDate.now(),
		registrertDato = LocalDateTime.now(),
		sluttDato = LocalDate.now(),
		status = TiltakInstans.Status.GJENNOMFORES,
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
	fun `hentTiltakInstanserByArrangorId() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans")
				.queryParam("arrangorId", "test")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentTiltakInstanserByArrangorId() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans")
				.queryParam("arrangorId", UUID.randomUUID().toString())
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentTiltakInstans() should return 401 when not authenticated`() {

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/$tiltakInstansId")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentTiltakInstans() should return 200 when authenticated`() {
		Mockito.`when`(tiltakInstansService.getTiltakInstans(tiltakInstansId)).thenReturn(tiltakInstans)
		val token = server.issueToken("tokenx", "test", "test").serialize()
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/$tiltakInstansId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentDeltakere() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID/deltakere")
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
			tiltakInstansId = tiltakInstansId,
			startDato = LocalDate.now(),
			sluttDato = LocalDate.now(),
			arenaStatus = "status",
			dagerPerUke = 1,
			prosentStilling = 10.343f,
			status = Deltaker.Status.GJENNOMFORES,
			createdAt = LocalDateTime.now(),
			modifiedAt = LocalDateTime.now()
		).toDeltaker()
		Mockito.`when`(deltakerService.hentDeltakerePaaTiltakInstans(tiltakInstansId)).thenReturn(listOf(deltaker))
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/$tiltakInstansId/deltakere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}
}
