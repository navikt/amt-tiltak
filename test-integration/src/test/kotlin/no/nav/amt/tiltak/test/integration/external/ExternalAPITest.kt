package no.nav.amt.tiltak.test.integration.external

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.external.api.dto.HarAktiveDeltakelserResponse
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID


class ExternalAPITest: IntegrationTestBase() {
	val getTokenXAuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}") }
	val getAzureAdM2MToken = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueAzureAdM2MToken()}") }
	val getAzureAToken = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueAzureAdToken(ident = "", oid = UUID.randomUUID())}") }

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

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
	fun `hentDeltakelserForPerson - mangler token - returnerer 401`() {
		val response = sendRequest(
			method = "POST",
			body = """{"personIdent": "${TestData.BRUKER_1.personIdent}"}""".toJsonRequestBody(),
			url = "/api/external/deltakelser",
		)

		response.code shouldBe 401
	}

	@Test
	fun `hentDeltakelserForPerson - gyldig m2m token - returnerer 200`() {
		val response = sendRequest(
			method = "POST",
			body = """{"personIdent": "${TestData.BRUKER_1.personIdent}"}""".toJsonRequestBody(),
			url = "/api/external/deltakelser",
			headers =  getAzureAdM2MToken()
		)

		response.code shouldBe 200
	}

	@Test
	fun `hentDeltakelserForPerson - gyldig obo token - returnerer 401`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/external/deltakelser",
			body = """{"personIdent": "${TestData.BRUKER_1.personIdent}"}""".toJsonRequestBody(),
			headers =  getAzureAToken()
		)

		response.code shouldBe 401
	}

	@Test
	fun `harAktiveDeltakelser - mangler token - returnerer 401`() {
		val response = sendRequest(
			method = "POST",
			body = """{"personIdent": "${TestData.BRUKER_1.personIdent}"}""".toJsonRequestBody(),
			url = "/api/external/aktiv-deltaker",
		)

		response.code shouldBe 401
	}

	@Test
	fun `harAktiveDeltakelser - gyldig m2m token - returnerer 200`() {
		val response = sendRequest(
			method = "POST",
			body = """{"personIdent": "${TestData.BRUKER_1.personIdent}"}""".toJsonRequestBody(),
			url = "/api/external/aktiv-deltaker",
			headers =  getAzureAdM2MToken()
		)

		response.code shouldBe 200
		fromJsonString<HarAktiveDeltakelserResponse>(response.body?.string()!!).harAktiveDeltakelser shouldBe true
	}
}
