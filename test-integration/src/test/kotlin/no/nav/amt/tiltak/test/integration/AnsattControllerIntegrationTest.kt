package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnsattControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	fun `getInnloggetAnsatt() should return 401 when not authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg"
		)

		response.code shouldBe 401
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when ansatt is not previously stored`() {
		val ident = "012345678912"

		altinnAclHttpClient.addRoller(ident, altinnAclHttpClient.createRollerForSingleOrg(ARRANGOR_1.organisasjonsnummer, listOf("KOORDINATOR")))
		pdlHttpClient.addPdlBruker(ident, "Integrasjon", "Test")

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ident)}")
		)

		val expectedJson = """
			{"fornavn":"Integrasjon","etternavn":"Test","arrangorer":[]}
		""".trimIndent()

		response.code shouldBe 200

		response.body?.string() shouldBe expectedJson

	}
}
