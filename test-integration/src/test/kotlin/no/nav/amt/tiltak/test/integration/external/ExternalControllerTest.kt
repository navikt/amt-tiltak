package no.nav.amt.tiltak.test.integration.external

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.Test


class ExternalControllerTest: IntegrationTestBase() {
	val getTokenXAuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}") }
	val getAzureAdM2MToken = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueAzureAdM2MToken()}") }

	@Test
	fun `hentMineDeltakelser - mangler token - returnerer 401`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/external/mine-deltakelser"
		)
		response.code shouldBe 401
	}

	@Test
	fun `hentMineDeltakelser - gyldig token - returnerer 200`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/external/mine-deltakelser",
			headers = getTokenXAuthHeader()
		)

		response.code shouldBe 200
	}

	@Test
	fun `hentDeltakelserForPerson - gyldig token - returnerer 200`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/external/deltakelser?personIdent=${TestData.BRUKER_1.personIdent}",
			headers =  getAzureAdM2MToken()
		)

		response.code shouldBe 200
	}
}
