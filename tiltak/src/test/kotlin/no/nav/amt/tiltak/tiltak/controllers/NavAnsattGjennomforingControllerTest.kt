package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavEnhetTilgang
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingerPaEnheterQuery
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
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
@WebMvcTest(controllers = [NavAnsattGjennomforingController::class])
class NavAnsattGjennomforingControllerTest {
	private val gjennomforingId = UUID.fromString("e68d54e2-47b5-11ec-81d3-0242ac130003")

	private val fnr = "fnr"

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var navAnsattService: NavAnsattService

	@MockBean
	private lateinit var gjennomforingService: GjennomforingService

	@MockBean
	private lateinit var gjennomforingerPaEnheterQuery: GjennomforingerPaEnheterQuery

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	val statusConverterMock = fun (id: UUID) =
		listOf(
			DeltakerStatusDbo(
				deltakerId = id,
				status = Deltaker.Status.DELTAR,
				endretDato = LocalDateTime.now(),
				aktiv = true)
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
		navKontorId = UUID.randomUUID(),
		sluttDato = LocalDate.now(),
		status = Gjennomforing.Status.GJENNOMFORES,
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

		Mockito.`when`(navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent))
			.thenReturn(emptyList())

		Mockito.`when`(gjennomforingerPaEnheterQuery.query(any()))
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

		Mockito.`when`(navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent))
			.thenReturn(listOf(NavEnhetTilgang(
				kontor = NavKontor(
					id = gjennomforing.navKontorId!!,
					enhetId = "1234",
					navn = "test"
				),
				temaer = listOf("TIL")
			)))

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId))
			.thenReturn(gjennomforing)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `hentGjennomforing() - skal returnere 403 hvis ikke tilgang til enhet`() {
		val token = azureAdToken("test", "test")
		val navIdent = "a12345"
		val gjennomforingId = UUID.randomUUID()

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker())
			.thenReturn(navIdent)

		Mockito.`when`(navAnsattService.hentTiltaksansvarligEnhetTilganger(navIdent))
			.thenReturn(listOf(NavEnhetTilgang(
				kontor = NavKontor(
					id = UUID.randomUUID(), // En annen id enn den på gjennomføringen
					enhetId = "1234",
					navn = "test"
				),
				temaer = listOf("TIL")
			)))

		Mockito.`when`(gjennomforingService.getGjennomforing(gjennomforingId))
			.thenReturn(gjennomforing)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing/$gjennomforingId")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(403, response.status)
		assertEquals("Ikke tilgang til enhet", response.errorMessage)
	}


}
