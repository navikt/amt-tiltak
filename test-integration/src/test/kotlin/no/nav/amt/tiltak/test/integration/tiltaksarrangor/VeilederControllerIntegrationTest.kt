package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import no.nav.amt.tiltak.test.utils.AsyncUtils
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class VeilederControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var arrangorVeilederService: ArrangorVeilederService

	private val lagAnsatt1Header =  { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}") }
	private val lagAnsatt2Header =  { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}") }

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/veiledere"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	internal fun `leggTilVeiledere - ansatt er ikke koordinator på gjennomføring - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt2Header(),
			body = lagOpprettVeilederJson(
				listOf(DELTAKER_1.id),
				listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				GJENNOMFORING_1.id
			).toJsonRequestBody(),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `leggTilVeiledere - ansatt som legges til har ikke rollen VEILEDER - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederJson(
				listOf(DELTAKER_1.id),
				listOf(Pair(ARRANGOR_ANSATT_1.id, false)),
				GJENNOMFORING_1.id
			).toJsonRequestBody(),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `leggTilVeiledere - ingen veiledere finnes fra før - legges inn riktig`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederJson(
				listOf(DELTAKER_1.id),
				listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				GJENNOMFORING_1.id
			).toJsonRequestBody(),
		)

		response.code shouldBe 200

		AsyncUtils.eventually {
			val veileder = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_1.id).first()

			veileder.ansattId shouldBe ARRANGOR_ANSATT_2.id
			veileder.deltakerId shouldBe DELTAKER_1.id
			veileder.erMedveileder shouldBe false
		}

	}

	@Test
	internal fun `leggTilVeiledere - veileder finnes fra før - overskrives av ny veileder`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederJson(
				listOf(DELTAKER_1.id),
				listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				GJENNOMFORING_1.id
			).toJsonRequestBody(),
		)

		response.code shouldBe 200

		AsyncUtils.eventually {
			val veiledere = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_1.id)
			veiledere shouldHaveSize 1

			val veileder = veiledere.first()
			veileder.ansattId shouldBe ARRANGOR_ANSATT_2.id
			veileder.deltakerId shouldBe DELTAKER_1.id
			veileder.erMedveileder shouldBe false
		}

	}

	private fun lagOpprettVeilederJson(
		deltakerIder: List<UUID>,
		veiledere: List<Pair<UUID, Boolean>>,
		gjennomforingId: UUID,
	) : String {
		val deltakerIderStr = deltakerIder.joinToString { "\"$it\"" }
		val veiledereStr = veiledere.joinToString {
			"""
				{"ansattId": "${it.first}", "erMedveileder": ${it.second}}
			""".trimIndent()
		}

		return """
			{
				"deltakerIder": [$deltakerIderStr],
				"veiledere": [$veiledereStr],
				"gjennomforingId": "$gjennomforingId"
			}
		""".trimIndent()
	}
}
