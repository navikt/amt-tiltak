package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattGjennomforingTilgangInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class GjennomforingControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/"),
			Request.Builder().get()
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/koordinatorer"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/tilgjengelig"),
			Request.Builder().post(emptyRequest())
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
			Request.Builder().delete()
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/deltakeroversikt"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `hentGjennomforinger() skal returnere 403 om Ansatt kun er veileder`() {

		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			ArrangorAnsattGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_2.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigFra = ZonedDateTime.now().minusDays(1),
				gyldigTil = ZonedDateTime.now().plusDays(1)
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}")
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes ikke - skal kaste 404`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 404
	}

	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes har ikke tilgang - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentGjennomforing - skal returnere 200 med korrekt respons`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson =
			"""{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1","virksomhetOrgnr":"111111111"}}"""
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentGjennomforinger - skal hente gjennomforinger med status Gjennomfores eller status Avsluttet med sluttdato tom 14 dager`() {
		val skalVareSynligId = UUID.fromString("b49a95b9-6bde-481f-9712-c212a7a046e1")
		val skalIkkeVareSynligId = UUID.fromString("ab23909a-7512-42d1-8abd-ca4047fe2ecb")

		testDataRepository.insertGjennomforing(
			GJENNOMFORING_1.copy(
				id = skalVareSynligId,
				status = Gjennomforing.Status.AVSLUTTET.name,
				navn = "Avsluttet gjennomforing",
				sluttDato = LocalDate.now().minusDays(14)
			)
		)

		testDataRepository.insertGjennomforing(
			GJENNOMFORING_1.copy(
				id = skalIkkeVareSynligId,
				status = Gjennomforing.Status.AVSLUTTET.name,
				navn = "Avsluttet gjennomforing - skal ikke vises",
				sluttDato = LocalDate.now().minusDays(15)
			)
		)

		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			ArrangorAnsattGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = skalVareSynligId,
				gyldigFra = ZonedDateTime.now().minusHours(1),
				gyldigTil = ZonedDateTime.now().plusYears(1)
			)
		)

		testDataRepository.insertArrangorAnsattGjennomforingTilgang(
			ArrangorAnsattGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				ansattId = ARRANGOR_ANSATT_1.id,
				gjennomforingId = skalIkkeVareSynligId,
				gyldigFra = ZonedDateTime.now().minusHours(1),
				gyldigTil = ZonedDateTime.now().plusYears(1)
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200

		val jsonBody = response.body!!.string()

		JsonPath.parse(jsonBody).read<Int>("$.length()") shouldBe 2
		JsonPath.parse(jsonBody).read<String>("$.[0].id") shouldBe GJENNOMFORING_1.id.toString()
		JsonPath.parse(jsonBody).read<String>("$.[1].id") shouldBe skalVareSynligId.toString()

	}

	@Test
	fun `hentTilgjengeligeGjennomforinger() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/tilgjengelig",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}")
		)

		response.code shouldBe 403
	}


	@Test
	fun `hentTilgjengeligeGjennomforinger - skal hente gjennomforinger med status Gjennomfores eller status Avsluttet med sluttdato tom 14 dager`() {
		val skalVareSynligId = UUID.fromString("b49a95b9-6bde-481f-9712-c212a7a046e1")

		testDataRepository.insertGjennomforing(
			GJENNOMFORING_1.copy(
				id = skalVareSynligId,
				status = Gjennomforing.Status.AVSLUTTET.name,
				navn = "Avsluttet gjennomforing",
				sluttDato = LocalDate.now().minusDays(14)
			)
		)

		testDataRepository.insertGjennomforing(
			GJENNOMFORING_1.copy(
				id = UUID.randomUUID(),
				status = Gjennomforing.Status.AVSLUTTET.name,
				navn = "Avsluttet gjennomforing - skal ikke vises",
				sluttDato = LocalDate.now().minusDays(15)
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/tilgjengelig",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200

		val jsonBody = response.body!!.string()

		JsonPath.parse(jsonBody).read<Int>("$.length()") shouldBe 2
		JsonPath.parse(jsonBody).read<String>("$.[0].id") shouldBe GJENNOMFORING_1.id.toString()
		JsonPath.parse(jsonBody).read<String>("$.[1].id") shouldBe skalVareSynligId.toString()

	}

	@Test
	fun `opprettTilgangTilGjennomforing() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `opprettTilgangTilGjennomforing - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `opprettTilgangTilGjennomforing - skal ha status 200 og opprette tilgang`() {
		testDataRepository.deleteAllArrangorAnsattGjennomforingTilganger()

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		response.code shouldBe 200
		val tilganger = testDataRepository.getArrangorAnsattGjennomforingTilganger(ARRANGOR_ANSATT_1.id)
		tilganger.any {
			it.gjennomforingId == GJENNOMFORING_1.id && it.gyldigFra.isAfter(ZonedDateTime.now().minusSeconds(10))
		} shouldBe true
	}

	@Test
	internal fun `opprettTilgangTilGjennomforing - skal ha status 403 hvis gjennomforing er avsluttet for 14 dager siden`() {
		testDataRepository.deleteAllArrangorAnsattGjennomforingTilganger()

		val id = UUID.fromString("b49a95b9-6bde-481f-9712-c212a7a046e1")
		testDataRepository.insertGjennomforing(
			GJENNOMFORING_1.copy(
				id = id,
				status = Gjennomforing.Status.AVSLUTTET.name,
				navn = "Avsluttet gjennomforing",
				sluttDato = LocalDate.now().minusDays(15)
			)
		)

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/$id/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		response.code shouldBe 403
	}

	@Test
	fun `fjernTilgangTilGjennomforing() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "DELETE",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}"),
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `fjernTilgangTilGjennomforing - skal ha status 200 og fjerne tilgang`() {
		val response = sendRequest(
			method = "DELETE",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		response.code shouldBe 200

		val tilganger = testDataRepository.getArrangorAnsattGjennomforingTilganger(ARRANGOR_ANSATT_1.id)
		tilganger.any {
			it.gjennomforingId == GJENNOMFORING_1.id && it.gyldigTil.isBefore(ZonedDateTime.now().plusSeconds(10))
		} shouldBe true
	}

	@Test
	fun `hentKoordinatorerPaGjennomforing() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/koordinatorer",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}")
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `hentKoordinatorerPaGjennomforing - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/koordinatorer",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentKoordinatorerPaGjennomforing - skal ha status 200 og returnere koordinatorer`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/koordinatorer",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson =
			"""[{"fornavn":"Ansatt 1 fornavn","mellomnavn":"Ansatt 1 mellomnavn","etternavn":"Ansatt 1 etternavn"}]""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}
	
	@Test
	fun `hentDeltakeroversikt() - ansatt er veileder - skal returnere deltakeroversikt med veilederinfo`() {
		testDataRepository.insertArrangorVeileder(TestData.ARRANGOR_ANSATT_2_VEILEDER_1)
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltakeroversikt",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}")
		)

		val expectedJson =
			"""{"veilederInfo":{"veilederFor":0,"medveilederFor":1},"koordinatorInfo":null}""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentDeltakeroversikt() - ansatt er veileder og koordinator - skal returnere deltakeroversikt med veileder- og koordinatorinfo`() {
		testDataRepository.insertArrangorVeileder(TestData.ARRANGOR_ANSATT_1_VEILEDER_1)
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltakeroversikt",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson =
			"""{"veilederInfo":{"veilederFor":1,"medveilederFor":0},"koordinatorInfo":{"deltakerlister":[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","type":"Tiltak1"}]}}""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

}
