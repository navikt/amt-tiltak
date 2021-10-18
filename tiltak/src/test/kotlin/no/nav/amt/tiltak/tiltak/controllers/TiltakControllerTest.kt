package no.nav.amt.tiltak.tiltak.controllers

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@ActiveProfiles("test")
@WebMvcTest(controllers = [TiltakController::class])
class TiltakControllerTest {

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

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun `hentAlleTiltak() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak")
				.queryParam("tiltaksleverandorId", "test")
		).andReturn().response

		Assertions.assertEquals(401, response.status)
	}

	@Test
	fun `hentAlleTiltak() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak")
				.queryParam("tiltaksleverandorId", "test")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		Assertions.assertEquals(200, response.status)
	}

}
