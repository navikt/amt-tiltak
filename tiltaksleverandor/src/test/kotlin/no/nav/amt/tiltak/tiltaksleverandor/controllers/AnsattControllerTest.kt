package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.core.port.Tiltaksleverandor
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.AfterClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@ActiveProfiles("test")
@WebMvcTest(controllers = [AnsattController::class])
class AnsattControllerTest {

	companion object {
		private val server = MockOAuth2Server()

		init {
			server.start()
			System.setProperty("MOCK_TOKEN_X_DISCOVERY_URL", server.wellKnownUrl("tokenx").toString())
		}
	}

	@AfterClass
	fun teardown() {
		server.shutdown()
	}

	@Autowired
	private lateinit var mockMvc: MockMvc

	@MockBean
	private lateinit var tiltaksleverandor: Tiltaksleverandor

	@Test
	fun `getInnloggetAnsatt() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksleverandor/ansatt/meg")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltaksleverandor/ansatt/meg")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		assertEquals(200, response.status)
	}

}
