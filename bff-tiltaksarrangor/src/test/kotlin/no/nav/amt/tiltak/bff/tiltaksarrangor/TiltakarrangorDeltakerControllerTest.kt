package no.nav.amt.tiltak.bff.tiltaksarrangor

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.bff.tiltaksarrangor.dto.*
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
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
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [DeltakerController::class])
class TiltakarrangorDeltakerControllerTest {

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var controllerService: ControllerService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	private val deltakerId = UUID.fromString("0d5190f8-a6c2-48ff-84b9-6b835664c099")

	private val gjennomforingId = UUID.fromString("7187e487-bdb4-43bc-9d17-a3ad0f400897")

	private val deltakerDetaljerDto = DeltakerDetaljerDto(
		id = deltakerId,
		fornavn = "Test",
		mellomnavn = null,
		etternavn = "Testersen",
		fodselsnummer = "12344543",
		telefonnummer = "9083423423",
		epost = "test@test.test",
		navEnhet = NavEnhetDto(
			navn = "NAV Testheim",
		),
		navVeileder = null,
		erSkjermetPerson = false,
		startDato = null,
		sluttDato = null,
		registrertDato = LocalDateTime.parse("2022-06-20T07:02:35.269658"),
		status = DeltakerStatusDto(Deltaker.Status.DELTAR, LocalDateTime.parse("2022-06-20T07:02:35.269658")),
		innsokBegrunnelse = "begrunnelse",
		gjennomforing = GjennomforingDto(
			id = gjennomforingId,
			navn = "",
			startDato = null,
			sluttDato = null,
			status = Gjennomforing.Status.GJENNOMFORES,
			tiltak = TiltakDto(
				tiltakskode = "",
				tiltaksnavn = ""
			),
			arrangor = ArrangorDto(
				virksomhetNavn = "Virksomhet AS",
				organisasjonNavn = null
			),
		),
		fjernesDato = null
	)

	@BeforeEach
	fun before() {
		MockitoAnnotations.openMocks(this)
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/tiltak-deltaker/$deltakerId")
		).andReturn().response

		response.status shouldBe 401
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should perform authorization check`() {
		val token = tokenXToken("test", "test")

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker())
			.thenReturn("fnr")

		Mockito.`when`(controllerService.getDeltakerDetaljerById(deltakerId))
			.thenReturn(deltakerDetaljerDto)

		mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/tiltak-deltaker/$deltakerId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		verify(arrangorAnsattTilgangService).verifiserTilgangTilGjennomforing(
			eq("fnr"), eq(deltakerDetaljerDto.gjennomforing.id)
		)
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 200 when authenticated`() {
		val token = tokenXToken("test", "test")

		Mockito.`when`(controllerService.getDeltakerDetaljerById(deltakerId))
			.thenReturn(deltakerDetaljerDto)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/tiltak-deltaker/$deltakerId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		val expectedJson = """
			{"id":"0d5190f8-a6c2-48ff-84b9-6b835664c099","fornavn":"Test","mellomnavn":null,"etternavn":"Testersen","fodselsnummer":"12344543","telefonnummer":"9083423423","epost":"test@test.test","navEnhet":{"navn":"NAV Testheim"},"navVeileder":null,"erSkjermetPerson":false,"startDato":null,"sluttDato":null,"registrertDato":"2022-06-20T07:02:35.269658","status":{"type":"DELTAR","endretDato":"2022-06-20T07:02:35.269658"},"gjennomforing":{"id":"7187e487-bdb4-43bc-9d17-a3ad0f400897","navn":"","startDato":null,"sluttDato":null,"status":"GJENNOMFORES","tiltak":{"tiltakskode":"","tiltaksnavn":""},"arrangor":{"virksomhetNavn":"Virksomhet AS","organisasjonNavn":null}},"fjernesDato":null,"innsokBegrunnelse":"begrunnelse"}
		""".trimIndent()

		response.status shouldBe 200
		response.contentAsString shouldBe expectedJson
	}

}
