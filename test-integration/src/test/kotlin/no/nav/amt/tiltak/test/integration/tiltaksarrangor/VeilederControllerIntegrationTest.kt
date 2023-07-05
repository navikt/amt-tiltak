package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_3
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorVeilederDboInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import no.nav.amt.tiltak.test.utils.AsyncUtils
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime
import java.util.Random
import java.util.UUID

class VeilederControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var arrangorVeilederService: ArrangorVeilederService

	private val lagAnsatt1Header =
		{ mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}") }
	private val lagAnsatt2Header =
		{ mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}") }
	private val lagAnsatt3Header =
		{ mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_3.personligIdent)}") }

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/veiledere?deltakerId=${UUID.randomUUID()}")
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	internal fun `tildelVeiledereForDeltaker - ansatt er ikke koordinator på gjennomføring - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt2Header(),
			body = lagOpprettVeiledereRequestBody(
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
			),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `tildelVeiledereForDeltaker - ansatt som legges til har ikke rollen VEILEDER - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeiledereRequestBody(
				veiledere = listOf(Pair(ARRANGOR_ANSATT_1.id, false)),
			),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `tildelVeiledereForDeltaker - flere veiledere fra før - veiledere blir erstattet av nye`() {
		opprettMedveiledereForDeltaker(3, DELTAKER_1.id)

		val ansatt3 = arrangorAnsattInput()
		val ansatt4 = arrangorAnsattInput()

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeiledereRequestBody(
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, true), Pair(ansatt3.id, true), Pair(ansatt4.id, false)),
			),
		)

		response.code shouldBe 200

		AsyncUtils.eventually {
			val veiledereForDeltaker1 = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_1.id)
			veiledereForDeltaker1 shouldHaveSize 3

			veiledereForDeltaker1.any { it.ansattId == ARRANGOR_ANSATT_2.id } shouldBe true
			veiledereForDeltaker1.any { it.ansattId == ansatt3.id } shouldBe true
			veiledereForDeltaker1.any { it.ansattId == ansatt4.id } shouldBe true
		}

	}

	@Test
	internal fun `tildelVeiledereForDeltaker - request med for mange medveiledere - skal kaste 400`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeiledereRequestBody(
				veiledere = listOf(
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
				),
			),
		)
		response.code shouldBe 400
	}

	private fun lagOpprettVeiledereRequestBody(veiledere: List<Pair<UUID, Boolean>>): RequestBody {
		val veiledereStr = veiledere.joinToString {
			"""
				{"ansattId": "${it.first}", "erMedveileder": ${it.second}}
			""".trimIndent()
		}

		return """{ "veiledere": [$veiledereStr] }""".trimIndent().toJsonRequestBody()
	}

	/**
	 * Oppretter *n* medveiledere for deltaker. Returnerer liste med veiledere sortert på gyldigFra synkende.
	 */
	private fun opprettMedveiledereForDeltaker(antallVeiledere: Int, deltakerId: UUID): List<ArrangorVeilederDboInput> {
		val veiledere = mutableListOf<ArrangorVeilederDboInput>()

		repeat(antallVeiledere) { i ->
			val ansatt = arrangorAnsattInput()

			val veilederInput = ARRANGOR_ANSATT_1_VEILEDER_1.copy(
				id = UUID.randomUUID(),
				ansattId = ansatt.id,
				deltakerId = deltakerId,
				erMedveileder = true,
				gyldigFra = ZonedDateTime.now().minusWeeks((20L + i))
			)
			testDataRepository.insertArrangorVeileder(veilederInput)

			veiledere.add(veilederInput)
		}

		return veiledere
	}

	private fun arrangorAnsattInput(): ArrangorAnsattInput {
		val ident = Random().nextLong(10_00_00_00000, 90_00_00_00000).toString()
		val ansatt = ARRANGOR_ANSATT_1.copy(id = UUID.randomUUID(), personligIdent = ident)
		val rolle = ARRANGOR_ANSATT_1_ROLLE_1.copy(id = UUID.randomUUID(), ansattId = ansatt.id, rolle = "VEILEDER")
		testDataRepository.insertArrangorAnsatt(ansatt)
		testDataRepository.insertArrangorAnsattRolle(rolle)
		return ansatt
	}

}
