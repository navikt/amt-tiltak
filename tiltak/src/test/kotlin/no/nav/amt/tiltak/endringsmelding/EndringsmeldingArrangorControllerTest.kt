package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
@WebMvcTest(controllers = [EndringsmeldingArrangorController::class])
class EndringsmeldingArrangorControllerTest {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var arrangorTilgangService: ArrangorAnsattTilgangService

	@MockBean
	private lateinit var endringsmeldingService: EndringsmeldingService

	@MockBean
	private lateinit var deltakerService: DeltakerService

	@MockBean
	private lateinit var skjermetPersonService: SkjermetPersonService

	companion object : MockOAuthServer() {
		@AfterAll
		@JvmStatic
		fun cleanup() {
			shutdownMockServer()
		}
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/endringsmelding?deltakerId=${UUID.randomUUID()}")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med korrekt respons`() {
		val endringsmeldingId = UUID.fromString("42f4c78c-4f70-489f-aa13-4b13a755d05b")
		val deltakerId = UUID.randomUUID()
		val ansattPersonligIdent = "1234567"

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker())
			.thenReturn(ansattPersonligIdent)

		Mockito.`when`(endringsmeldingService.hentEndringsmeldingerForDeltaker(deltakerId))
			.thenReturn(listOf(EndringsmeldingDbo(
				id = endringsmeldingId,
				deltakerId = deltakerId,
				startDato = LocalDate.parse("2022-09-05"),
				godkjentAvNavAnsatt = null,
				godkjentTidspunkt = null,
				aktiv = false,
				opprettetAvId = ARRANGOR_ANSATT_1.id,
				createdAt = LocalDateTime.now(),
				modifiedAt = LocalDateTime.now()
			)))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/endringsmelding?deltakerId=$deltakerId")
				.header("Authorization", "Bearer ${tokenXToken()}")
		).andReturn().response

		verify(arrangorTilgangService, times(1)).verifiserTilgangTilDeltaker(ansattPersonligIdent, deltakerId)

		val expectedJson = """
			[{"id":"42f4c78c-4f70-489f-aa13-4b13a755d05b","startDato":"2022-09-05","aktiv":false}]
		""".trimIndent()

		assertEquals(expectedJson, response.contentAsString)
		assertEquals(200, response.status)
	}

}
