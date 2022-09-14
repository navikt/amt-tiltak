package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class EndringsmeldingArrangorControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
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

		db.insertEndringsmelding(
			EndringsmeldingInput(
				id = endringsmeldingId,
				deltakerId = DELTAKER_1.id,
				startDato = LocalDate.parse("2022-09-05"),
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
			[{"id":"$endringsmeldingId","startDato":"2022-09-05","aktiv":false}]
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


}
