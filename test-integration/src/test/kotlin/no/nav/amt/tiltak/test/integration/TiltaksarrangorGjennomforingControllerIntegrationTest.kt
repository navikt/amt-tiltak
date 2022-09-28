package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class TiltaksarrangorGjennomforingControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes ikke - skal kaste 403 - FORBIDDEN`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}
}
