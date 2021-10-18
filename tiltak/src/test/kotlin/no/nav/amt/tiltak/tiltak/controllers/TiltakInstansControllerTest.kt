package no.nav.amt.tiltak.tiltak.controllers

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@ActiveProfiles("test")
@WebMvcTest(controllers = [TiltakInstansController::class])
class TiltakInstansControllerTest {

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
	fun `hentTiltakInstans() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID")
		).andReturn().response

		assertEquals(401, response.status)
	}

	@Test
	fun `hentTiltakInstans() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		Assertions.assertEquals(200, response.status)
	}

	@Test
	fun `hentDeltagere() should return 401 when not authenticated`() {
		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID/deltagere")
		).andReturn().response

		Assertions.assertEquals(401, response.status)
	}

	@Test
	fun `hentDeltagere() should return 200 when authenticated`() {
		val token = server.issueToken("tokenx", "test", "test").serialize()

		val response = mockMvc.perform(
			MockMvcRequestBuilders.get("/api/tiltak-instans/ID/deltagere")
				.header("Authorization", "Bearer $token")
		).andReturn().response

		Assertions.assertEquals(200, response.status)
	}

}
