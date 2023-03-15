package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2_VEILEDER_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_3
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
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
import java.util.*

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
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/veiledere"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/veiledere?deltakerId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/veiledere?gjennomforingId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/veiledere?deltakerId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/veiledere/tilgjengelig?gjennomforingId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/veileder/deltakerliste"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	internal fun `leggTilVeiledere - ansatt er ikke koordinator på gjennomføring - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt2Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `leggTilVeiledere - ansatt som legges til har ikke rollen VEILEDER - skal kaste 403`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_1.id, false)),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `leggTilVeiledere - ingen veiledere finnes fra før - legges inn riktig`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				gjennomforingId = GJENNOMFORING_1.id
			),
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
	internal fun `leggTilVeiledere - veileder finnes fra før - erstattes av ny veileder`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, false)),
				gjennomforingId = GJENNOMFORING_1.id
			),
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

	@Test
	internal fun `leggTilVeiledere - medveiledere finnes fra før - eldste overskrives av ny veileder`() {
		val medveiledere = opprettMedveiledereForDeltaker(3, DELTAKER_1.id)
		val eldsteMedveileder = medveiledere.last()

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, true)),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 200

		AsyncUtils.eventually {
			val veiledere = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_1.id)
			veiledere shouldHaveSize 3

			veiledere.any { it.id == eldsteMedveileder.id } shouldBe false
			veiledere.any { it.ansattId == ARRANGOR_ANSATT_2.id } shouldBe true
		}

	}

	@Test
	internal fun `leggTilVeiledere - flere deltakere med medveiledere fra før - eldste veiledere blir erstattet av nye`() {
		val medveiledere1 = opprettMedveiledereForDeltaker(3, DELTAKER_1.id)
		val medveiledere2 = opprettMedveiledereForDeltaker(2, DELTAKER_2.id)

		val arrangor3 = arrangorAnsattInput()

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id, DELTAKER_2.id),
				veiledere = listOf(Pair(ARRANGOR_ANSATT_2.id, true), Pair(arrangor3.id, true)),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 200

		AsyncUtils.eventually {
			val veiledereForDeltaker1 = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_1.id)
			veiledereForDeltaker1 shouldHaveSize 3

			veiledereForDeltaker1.any { it.ansattId == ARRANGOR_ANSATT_2.id } shouldBe true
			veiledereForDeltaker1.any { it.ansattId == arrangor3.id } shouldBe true
			veiledereForDeltaker1.any { it.ansattId == medveiledere1.first().ansattId } shouldBe true

			val veiledereForDeltaker2 = arrangorVeilederService.hentVeiledereForDeltaker(DELTAKER_2.id)
			veiledereForDeltaker2 shouldHaveSize 3

			veiledereForDeltaker2.any { it.ansattId == ARRANGOR_ANSATT_2.id } shouldBe true
			veiledereForDeltaker2.any { it.ansattId == arrangor3.id } shouldBe true
			veiledereForDeltaker2.any { it.ansattId == medveiledere2.first().ansattId } shouldBe true
		}

	}

	@Test
	internal fun `leggTilVeiledere - flere enn 3 medveiledere - skal kaste 400`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id, DELTAKER_2.id),
				veiledere = listOf(
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
					Pair(arrangorAnsattInput().id, true),
				),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 400
	}

	@Test
	internal fun `leggTilVeiledere - flere enn 1 veileder - skal kaste 400`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/veiledere",
			headers = lagAnsatt1Header(),
			body = lagOpprettVeilederBulkRequestBody(
				deltakerIder = listOf(DELTAKER_1.id, DELTAKER_2.id),
				veiledere = listOf(
					Pair(arrangorAnsattInput().id, false),
					Pair(arrangorAnsattInput().id, false),
				),
				gjennomforingId = GJENNOMFORING_1.id
			),
		)

		response.code shouldBe 400
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

	@Test
	internal fun `hentTilgjengeligeVeiledere - veiledere finnes - skal ha status 200 og returnerer veiledere`() {
		testDataRepository.insertArrangorAnsattRolle(
				ARRANGOR_ANSATT_1_ROLLE_1.copy(id = UUID.randomUUID(), rolle = "VEILEDER")
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere/tilgjengelig?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe """
			[{"ansattId":"6321c7dc-6cfb-47b0-b566-32979be5041f","fornavn":"Ansatt 1 fornavn","mellomnavn":"Ansatt 1 mellomnavn","etternavn":"Ansatt 1 etternavn"},{"ansattId":"a24e659c-2651-4fbb-baad-01cacb2412f0","fornavn":"Ansatt 2 fornavn","mellomnavn":null,"etternavn":"Ansatt 2 etternavn"}]
		""".trimIndent()
	}

	@Test
	internal fun `hentTilgjengeligeVeiledere - veiledere finnes ikke - skal ha status 200 og returnerer tom liste`() {
		testDataRepository.deleteAllArrangorAnsattRoller(ArrangorAnsattRolle.VEILEDER)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere/tilgjengelig?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe "[]"
	}

	@Test
	internal fun `hentTilgjengeligeVeiledere - har ikke tilgang - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere/tilgjengelig?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = lagAnsatt2Header(),
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `hentVeiledereForDeltaker - veiledere finnes - skal ha status 200 og returnerer veiledere`() {
		testDataRepository.insertArrangorAnsattRolle(
			ARRANGOR_ANSATT_1_ROLLE_1.copy(id = UUID.randomUUID(), rolle = "VEILEDER")
		)

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe """
			[{"id":"af238302-e96b-436a-8978-ec2aa5f2ee66","ansattId":"6321c7dc-6cfb-47b0-b566-32979be5041f","deltakerId":"dc600c70-124f-4fe7-a687-b58439beb214","erMedveileder":false,"fornavn":"Ansatt 1 fornavn","mellomnavn":"Ansatt 1 mellomnavn","etternavn":"Ansatt 1 etternavn"},{"id":"bbadfe46-eaf3-4ee8-bb53-2e9e15ea7ef0","ansattId":"a24e659c-2651-4fbb-baad-01cacb2412f0","deltakerId":"dc600c70-124f-4fe7-a687-b58439beb214","erMedveileder":true,"fornavn":"Ansatt 2 fornavn","mellomnavn":null,"etternavn":"Ansatt 2 etternavn"}]
		""".trimIndent()
	}

	@Test
	internal fun `hentVeiledereForDeltaker - veiledere finnes ikke - skal ha status 200 og returnerer tom liste`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe "[]"
	}

	@Test
	internal fun `hentVeiledereForDeltaker - har ikke tilgang - skal kaste 403`() {
		testDataRepository.deleteAllArrangorAnsattRoller()

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `hentAktiveVeiledereForGjennomforing - veiledere finnes - skal ha status 200 og returnerer veiledere`() {
		testDataRepository.insertArrangorAnsattRolle(
			ARRANGOR_ANSATT_1_ROLLE_1.copy(id = UUID.randomUUID(), rolle = "VEILEDER")
		)

		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_1_VEILEDER_1)
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe """
			[{"id":"af238302-e96b-436a-8978-ec2aa5f2ee66","ansattId":"6321c7dc-6cfb-47b0-b566-32979be5041f","deltakerId":"dc600c70-124f-4fe7-a687-b58439beb214","erMedveileder":false,"fornavn":"Ansatt 1 fornavn","mellomnavn":"Ansatt 1 mellomnavn","etternavn":"Ansatt 1 etternavn"},{"id":"bbadfe46-eaf3-4ee8-bb53-2e9e15ea7ef0","ansattId":"a24e659c-2651-4fbb-baad-01cacb2412f0","deltakerId":"dc600c70-124f-4fe7-a687-b58439beb214","erMedveileder":true,"fornavn":"Ansatt 2 fornavn","mellomnavn":null,"etternavn":"Ansatt 2 etternavn"}]
		""".trimIndent()
	}

	@Test
	internal fun `hentAktiveVeiledereForGjennomforing - veiledere finnes ikke - skal ha status 200 og returnerer tom liste`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = lagAnsatt1Header(),
		)

		response.code shouldBe 200

		response.body!!.string() shouldBe "[]"
	}

	@Test
	internal fun `hentAktiveVeiledereForGjennomforing - har ikke tilgang - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veiledere?deltakerId=${DELTAKER_1.id}",
			headers = lagAnsatt2Header(),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentDeltakerliste - har ikke veilederrolle - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veileder/deltakerliste",
			headers = lagAnsatt3Header(),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentDeltakerliste - er veileder - henter deltakerliste`() {
		testDataRepository.insertArrangorVeileder(ARRANGOR_ANSATT_2_VEILEDER_1)
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/veileder/deltakerliste",
			headers = lagAnsatt2Header(),
		)

		response.code shouldBe 200
		response.body!!.string() shouldBe """
			[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"deltakerliste":{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","type":"Tiltak1"},"erMedveilederFor":true,"aktiveEndringsmeldinger":[{"id":"9830e130-b18a-46b8-8e3e-6c06734d797e","innhold":{"oppstartsdato":"2022-11-11"},"type":"LEGG_TIL_OPPSTARTSDATO"},{"id":"07099997-e02e-45e3-be6f-3c1eaf694557","innhold":{"sluttdato":"2022-11-10","aarsak":{"type":"ANNET","beskrivelse":"Flyttet til utland"}},"type":"AVSLUTT_DELTAKELSE"}]}]
		""".trimIndent()
	}

	private fun lagOpprettVeiledereRequestBody(veiledere: List<Pair<UUID, Boolean>>): RequestBody {
		val veiledereStr = veiledere.joinToString {
			"""
				{"ansattId": "${it.first}", "erMedveileder": ${it.second}}
			""".trimIndent()
		}

		return """{ "veiledere": [$veiledereStr] }""".trimIndent().toJsonRequestBody()
	}

	private fun lagOpprettVeilederBulkRequestBody(
		deltakerIder: List<UUID>,
		veiledere: List<Pair<UUID, Boolean>>,
		gjennomforingId: UUID,
	): RequestBody {
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
		""".trimIndent().toJsonRequestBody()
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
