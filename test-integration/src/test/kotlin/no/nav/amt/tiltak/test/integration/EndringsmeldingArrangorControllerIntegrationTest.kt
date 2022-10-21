package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*

class EndringsmeldingArrangorControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var endringsmeldingRepository: EndringsmeldingRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/endringsmelding?deltakerId=${UUID.randomUUID()}"
		)

		Assertions.assertEquals(401, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med korrekt respons`() {
		val endringsmeldingId = UUID.randomUUID()

		testDataRepository.insertEndringsmelding(
			EndringsmeldingInput(
				id = endringsmeldingId,
				deltakerId = DELTAKER_1.id,
				startDato = LocalDate.parse("2022-09-05"),
				sluttDato = null,
				aktiv = false,
				opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/endringsmelding?deltakerId=${DELTAKER_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		val expectedJson = """
			[{"id":"$endringsmeldingId","startDato":"2022-09-05","sluttDato":null,"aktiv":false}]
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `registrerStartDato() - skal returnere 200 og opprette ny endringsmelding`() {
		poaoTilgangClient.addErSkjermetResponse(mapOf(BRUKER_1.fodselsnummer to false))
		val token = oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)
		val startDato = LocalDate.now()

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/endringsmelding/deltaker/${DELTAKER_1.id}/startdato?startDato=$startDato",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody(),
		)

		response.code shouldBe 200


		val endringsmeldinger = endringsmeldingRepository.getByDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		endringsmeldinger[0].startDato shouldBe startDato
		endringsmeldinger[0].sluttDato shouldBe null
	}

	@Test
	fun `registrerSluttDato() - skal returnere 200 og opprette ny endringsmelding`() {
		poaoTilgangClient.addErSkjermetResponse(mapOf(BRUKER_1.fodselsnummer to false))
		val token = oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)
		val sluttDato = LocalDate.now()

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/endringsmelding/deltaker/${DELTAKER_1.id}/sluttdato?sluttDato=$sluttDato",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody(),
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingRepository.getByDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		endringsmeldinger[0].startDato shouldBe null
		endringsmeldinger[0].sluttDato shouldBe sluttDato
	}
}
