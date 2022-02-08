package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.tiltak.dto.*
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [TiltakDeltakerController::class])
class TiltakDeltakerControllerTest {

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

	private val deltakerId = UUID.fromString("0d5190f8-a6c2-48ff-84b9-6b835664c099")

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var tiltakDeltakerPresentationService: TiltakDeltakerPresentationService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-deltaker/$deltakerId")
		).andReturn().response

		Assertions.assertEquals(401, response.status)
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		Mockito.`when`(tiltakDeltakerPresentationService.getDeltakerDetaljerById(deltakerId))
			.thenReturn(TiltakDeltakerDetaljerDto(
				id = deltakerId,
				fornavn = "",
				mellomnavn = null,
				etternavn = "",
				fodselsnummer = "",
				telefonnummer = "",
				epost = "",
				navKontor = NavKontorDto(
					navn = "NAV Testheim",
				),
				navVeileder = null,
				startDato = null,
				sluttDato = null,
				registrertDato = LocalDateTime.now(),
				status = DeltakerStatusDto(Deltaker.Status.DELTAR, LocalDateTime.now()),
				gjennomforing = GjennomforingDto(
					id = UUID.randomUUID(),
					navn = "",
					startDato = null,
					sluttDato = null,
					status = null,
					tiltak = TiltakDto(
						tiltakskode = "",
						tiltaksnavn = ""
					)
				)
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-deltaker/$deltakerId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		Assertions.assertEquals(200, response.status)
	}

}
