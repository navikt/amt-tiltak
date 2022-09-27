package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.Person
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@WebMvcTest(controllers = [AnsattController::class])
class AnsattControllerTest {

	companion object {
		private val server = MockOAuthServer()

		@AfterAll
		@JvmStatic
		fun cleanup() {
			server.shutdownMockServer()
		}
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var arrangorAnsattService: ArrangorAnsattService

	@MockBean
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var personService: PersonService

	@MockBean
	private lateinit var arrangorAnsattTilgangService: ArrangorAnsattTilgangService

	@Test
	fun `getInnloggetAnsatt() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/ansatt/meg")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated`() {
		val ident = "12345678"
		val token = server.tokenXToken(claims = mapOf("pid" to ident))

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker()).thenReturn(ident)

		Mockito.`when`(arrangorAnsattService.getAnsattByPersonligIdent(ident))
			.thenReturn(Ansatt(
				id = UUID.randomUUID(),
				personligIdent = "",
				fornavn = "",
				mellomnavn = null,
				etternavn = "",
				arrangorer = emptyList()
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/ansatt/meg")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		Mockito.verify(arrangorAnsattTilgangService, times(1))
			.synkroniserRettigheterMedAltinn(ident)

		assertEquals(200, response.status)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when ansatt is not previously stored`() {
		val personligIdent = "12345678"
		val token = server.tokenXToken(claims = mapOf("pid" to personligIdent))

		Mockito.`when`(authService.hentPersonligIdentTilInnloggetBruker()).thenReturn(personligIdent)

		Mockito.`when`(arrangorAnsattService.getAnsattByPersonligIdent(personligIdent))
			.thenReturn(null)

		Mockito.`when`(personService.hentPerson(personligIdent)).thenReturn(
			Person(
				fornavn = "Test",
				mellomnavn = null,
				etternavn = "Testersen",
				telefonnummer = null,
				diskresjonskode = null,
			))

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksarrangor/ansatt/meg")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		val expectedJson = """
			{"fornavn":"Test","etternavn":"Testersen","arrangorer":[]}
		""".trimIndent()

		assertEquals(200, response.status)
		assertEquals(expectedJson, response.contentAsString)
	}

}
