package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.test.mock_oauth_server.MockOAuthServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

@ActiveProfiles("test")
@WebMvcTest(controllers = [InternalNavKontorController::class])
class InternalNavKontorControllerTest {

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
	private lateinit var authService: AuthService

	@MockBean
	private lateinit var publiserNavKontorService: PubliserNavKontorService

	@BeforeEach
	fun before() {
		MockitoAnnotations.openMocks(this)
	}

	@Test
	fun `publiserAlleEnheter() should be unprotected`() {
		`when`(authService.isInternalRequest(any())).thenReturn(true)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/internal/nav-kontor/publiser-alle-enheter")
		).andReturn().response

		assertEquals(200, response.status)
	}

	@Test
	fun `publiserAlleEnheter() should return 401 if not internal`() {
		`when`(authService.isInternalRequest(any())).thenReturn(false)

		val response = mockMvc.perform(
			MockMvcRequestBuilders.post("/internal/nav-kontor/publiser-alle-enheter")
		).andReturn().response

		assertEquals(401, response.status)
	}

}
