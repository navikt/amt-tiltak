package no.nav.amt.tiltak.tiltak.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
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
@WebMvcTest(controllers = [NavGjennomforingController::class])
class NavGjennomforingControllerTest {
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
			System.setProperty("MOCK_AZURE_AD_DISCOVERY_URL", server.wellKnownUrl("azuread").toString())
		}

		@AfterAll
		@JvmStatic
		fun cleanup() {
			server.shutdown()
		}
	}

	@Test
	fun `hentGjennomforingerForNavAnsatte() - sender med tokenx-token - skal returnere 401`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentGjennomforingerForNavAnsatte() - sender med azure ad-token - skal returnere 200`() {
		val token = server.issueToken("azuread", "test", "test").serialize()
		val navIdent = "ab12345"

		Mockito.`when`(authService.hentNavIdentTilInnloggetBruker()).thenReturn(navIdent)

		Mockito.`when`(navAnsattService.getNavAnsatt(navIdent)).thenReturn(NavAnsatt(navIdent = navIdent, navn = "Navn Navnesen"))


		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/nav-ansatt/gjennomforing")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

}
