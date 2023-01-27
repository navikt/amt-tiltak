package no.nav.amt.tiltak.bff.internal

import io.kotest.assertions.throwables.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

class RepubliseringControllerTest {

	@Test
	fun `republiserDeltakere - skal republisere deltakere på kafka hvis request er internt`() {
		val deltakerService = mockk<DeltakerService>()
		val servletRequest = mockk<HttpServletRequest>()

		val controller = RepubliseringController(deltakerService)

		every { servletRequest.remoteAddr }.returns("127.0.0.1")

		controller.republiserDeltakere(servletRequest)

		AsyncUtils.eventually {
			verify(exactly = 1) { deltakerService.republiserAlleDeltakerePaKafka(500) }
		}
	}

	@Test
	fun `republiserDeltakere - skal kaste exception hvis request ikke er internt`() {
		val deltakerService = mockk<DeltakerService>()
		val servletRequest = mockk<HttpServletRequest>()

		val controller = RepubliseringController(deltakerService)

		every { servletRequest.remoteAddr }.returns("192.168.0.42")

		shouldThrowExactly<ResponseStatusException> {
			controller.republiserDeltakere(servletRequest)
		}
	}

}
