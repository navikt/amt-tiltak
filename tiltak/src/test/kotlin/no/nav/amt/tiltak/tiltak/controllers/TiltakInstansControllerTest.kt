package no.nav.amt.tiltak.tiltak.controllers
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
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
@WebMvcTest(controllers = [TiltakInstansController::class])
class TiltakInstansControllerTest {
	private val tiltakInstansId = UUID.fromString("e68d54e2-47b5-11ec-81d3-0242ac130003")

	@MockBean
	private lateinit var tiltakService: TiltakService;

	@Autowired
	private lateinit var mockMvc: MockMvc

	val tiltakInstans = TiltakInstans(
		id = UUID.randomUUID(),
		tiltakId = UUID.randomUUID(),
		tiltaksarrangorId = UUID.randomUUID(),
		navn = "tiltaksnavn",
		fremmoteDato = LocalDateTime.now(),
		oppstartDato = LocalDate.now(),
		registrertDato = LocalDateTime.now(),
		sluttDato = LocalDate.now(),
		status = TiltakInstans.Status.GJENNOMFORES,
	)
	val tiltak = Tiltak(
		id = tiltakInstans.tiltakId,
		navn = "tiltaksnavn",
		kode = "kode"
	)

	@BeforeEach
	fun before () {
		MockitoAnnotations.openMocks(this);
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
	fun `hentTiltakInstans() should return 401 when not authenticated`() {

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/$tiltakInstansId")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentTiltakInstans() should return 200 when authenticated`() {
		Mockito.`when`(tiltakService.getTiltakInstans(tiltakInstansId)).thenReturn(tiltakInstans)
		Mockito.`when`(tiltakService.getTiltak(tiltakInstans.tiltakId)).thenReturn(tiltak)

		val token = server.issueToken("tokenx", "test", "test").serialize()
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/$tiltakInstansId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentDeltagere() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID/deltagere")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentDeltagere() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID/deltagere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}
}
