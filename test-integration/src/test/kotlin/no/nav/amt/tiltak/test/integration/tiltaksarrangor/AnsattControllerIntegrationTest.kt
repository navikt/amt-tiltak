package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.ansatt.AnsattDto
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class AnsattControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var arrangorAnsattRepository: ArrangorAnsattRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}


	@Test
	internal fun `skal teste token autentisering`() {

		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/ansatt/meg"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated and be logged in DB`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val arrangorAnsatt = arrangorAnsattRepository.get(ARRANGOR_ANSATT_1.id)
		arrangorAnsatt shouldNotBe null
		arrangorAnsatt!!.sistVelykkedeInnlogging.truncatedTo(ChronoUnit.HOURS) shouldBe LocalDateTime.now()
			.truncatedTo(ChronoUnit.HOURS)


		response.code shouldBe 200
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 and no arrangorer when only VEILEDER`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_2.personligIdent)}")
		)

		response.code shouldBe 200
		val responseBody: AnsattDto? = response.body?.string()?.let { JsonUtils.fromJsonString(it) }

		responseBody shouldNotBe null
		responseBody!!.arrangorer.isEmpty() shouldBe true
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 and only arrangorer with KOORDINATOR roller`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200
		val responseBody: AnsattDto? = response.body?.string()?.let { JsonUtils.fromJsonString(it) }

		responseBody shouldNotBe null
		responseBody!!.arrangorer.none { it.roller.contains(ArrangorAnsattRolle.VEILEDER.name) } shouldBe true
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when ansatt is not previously stored`() {
		val ident = "012345678912"

		mockAmtAltinnAclHttpServer.addRoller(
			norskIdent = ident,
			orgNr = ARRANGOR_1.organisasjonsnummer,
			roller = listOf("KOORDINATOR")
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		val expectedJson = """
			{"fornavn":"Integrasjon","etternavn":"Test","arrangorer":[{"id":"8a37bce6-3bc1-11ec-8d3d-0242ac130003","navn":"Tiltaksarrangør 1","organisasjonsnummer":"111111111","overordnetEnhetOrganisasjonsnummer":"911111111","overordnetEnhetNavn":"Org Tiltaksarrangør 1","roller":["KOORDINATOR"]}]}
		""".trimIndent()

		response.code shouldBe 200

		response.body?.string() shouldBe expectedJson

	}

	@Test
	internal fun `getRoller - Har ikke roller - Returnerer tomt set`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			norskIdent = ident,
			orgNr = ARRANGOR_1.organisasjonsnummer,
			roller = listOf()
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		response.body?.string() shouldBe "[]"
	}

	@Test
	internal fun `getRoller - Har flere koordinator roller - Returnerer KOORDINATOR`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			ident,
			mapOf(
				ARRANGOR_1.organisasjonsnummer to listOf("KOORDINATOR"),
				ARRANGOR_2.organisasjonsnummer to listOf("KOORDINATOR")
			)
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		val responseBody = JsonUtils.fromJsonString<Set<String>>(response.body?.string()!!)

		responseBody shouldBe setOf("KOORDINATOR")
	}

	@Test
	internal fun `getRoller - Har koordinator og veileder roller - Returnerer KOORDINATOR, VEILEDER`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			ident,
			mapOf(
				ARRANGOR_1.organisasjonsnummer to listOf("KOORDINATOR"),
				ARRANGOR_2.organisasjonsnummer to listOf("VEILEDER")
			)
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		val responseBody = JsonUtils.fromJsonString<Set<String>>(response.body?.string()!!)

		responseBody shouldBe setOf("KOORDINATOR", "VEILEDER")
	}

}
