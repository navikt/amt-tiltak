package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class EndringsmeldingControllerIntegrationTest : IntegrationTestBase() {

	val createAnsatt1AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}") }
	val createAnsatt2AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}") }


	@Autowired
	lateinit var endringsmeldingRepository: EndringsmeldingRepository

	@Autowired
	lateinit var deltakerService: DeltakerService

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/endringsmelding/aktiv?deltakerId=${UUID.randomUUID()}"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/endringsmelding/${UUID.randomUUID()}/tilbakekall/"),
		)

		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `hentAktiveEndringsmeldinger() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/endringsmelding/aktiv?deltakerId=${DELTAKER_1.id}",
			headers = createAnsatt2AuthHeader(),
		)

		response.code shouldBe 403
	}


	@Test
	fun `hentAktiveEndringsmeldinger() - skal returnere 200 med korrekt respons`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/endringsmelding/aktiv?deltakerId=${DELTAKER_1.id}",
			headers = createAnsatt1AuthHeader(),
		)

		val expectedJson = """
			[{"id":"9830e130-b18a-46b8-8e3e-6c06734d797e","innhold":{"oppstartsdato":"2022-11-11"},"type":"LEGG_TIL_OPPSTARTSDATO"},{"id":"07099997-e02e-45e3-be6f-3c1eaf694557","innhold":{"sluttdato":"2022-11-10","aarsak":{"type":"ANNET","beskrivelse":"Flyttet til utland"}},"type":"AVSLUTT_DELTAKELSE"}]
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentAktiveEndringsmeldinger() - skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/endringsmelding/aktiv?deltakerId=${opprettSkjultDeltaker()}",
			headers = createAnsatt1AuthHeader(),
		)

		response.code shouldBe 400
	}

	@Test
	fun `tilbakekallEndringsmelding() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/endringsmelding/${ENDRINGSMELDING_1_DELTAKER_1.id}/tilbakekall/",
			headers = createAnsatt2AuthHeader(),
			body = emptyRequest(),
		)

		response.code shouldBe 403
	}


	@Test
	fun `tilbakekallEndringsmelding() - skal returnere 200 om endringsmeldingen ble tilbakekalt`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/endringsmelding/${ENDRINGSMELDING_1_DELTAKER_1.id}/tilbakekall/",
			headers = createAnsatt1AuthHeader(),
			body = emptyRequest(),
		)

		response.code shouldBe 200

		val oppdatertMelding = endringsmeldingRepository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		oppdatertMelding.status shouldBe Endringsmelding.Status.TILBAKEKALT
	}

	@Test
	fun `tilbakekallEndringsmelding() - skal returnere 400 om endringsmeldingen ikke ble tilbakekalt`() {
		val utfortEndringsmelding = ENDRINGSMELDING_1_DELTAKER_1.copy(id = UUID.randomUUID(), status = Endringsmelding.Status.UTFORT)
		testDataRepository.insertEndringsmelding(utfortEndringsmelding)

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/endringsmelding/${utfortEndringsmelding.id}/tilbakekall/",
			headers = createAnsatt1AuthHeader(),
			body = emptyRequest(),
		)

		response.code shouldBe 400

		val melding = endringsmeldingRepository.get(utfortEndringsmelding.id)

		melding.status shouldBe Endringsmelding.Status.UTFORT
	}

	private fun opprettSkjultDeltaker(): UUID {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId, gjennomforingId = TestData.GJENNOMFORING_1.id))
		testDataRepository.insertDeltakerStatus(TestData.DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "IKKE_AKTUELL"))

		deltakerService.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)

		return deltakerId
	}

}
