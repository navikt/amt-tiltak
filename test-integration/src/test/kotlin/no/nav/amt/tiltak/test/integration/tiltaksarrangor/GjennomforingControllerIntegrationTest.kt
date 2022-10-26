package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattRolleInput
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerStatusInput
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
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
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/deltakere"),
			Request.Builder().get()
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/koordinatorer"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/tilgjengelig"),
			Request.Builder().post(emptyRequest())
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
			Request.Builder().delete()
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes ikke - skal kaste 404`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 404
	}

	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes har ikke tilgang - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentGjennomforing - skal returnere 200 med korrekt respons`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1"}}"""
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentGjennomforinger - skal hente gjennomforinger med status Gjennomfores`() {
		testDataRepository.insertGjennomforing(GJENNOMFORING_1.copy(id = UUID.randomUUID(), status = Gjennomforing.Status.AVSLUTTET.name))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)
		val expectedJson = """[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1"}}]"""
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentTilgjengeligeGjennomforinger - skal hente gjennomforinger med status Gjennomfores`() {
		testDataRepository.insertGjennomforing(GJENNOMFORING_1.copy(id = UUID.randomUUID(), status = Gjennomforing.Status.AVSLUTTET.name))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/tilgjengelig",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)
		val expectedJson = """[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1"}}]"""
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


	@Test
	internal fun `opprettTilgangTilGjennomforing - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `opprettTilgangTilGjennomforing - skal ha status 200 og opprette tilgang`() {
		testDataRepository.insertArrangorAnsattRolle(
			ArrangorAnsattRolleInput(UUID.randomUUID(), ARRANGOR_2.id, ARRANGOR_ANSATT_1.id, ArrangorAnsattRolle.KOORDINATOR.name)
		)
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		response.code shouldBe 200
		val tilganger = testDataRepository.getArrangorAnsattGjennomforingTilganger(ARRANGOR_ANSATT_1.id)
		tilganger.any {
			it.gjennomforingId == GJENNOMFORING_2.id && it.gyldigFra.isAfter(ZonedDateTime.now().minusSeconds(10))
		} shouldBe true
	}

	@Test
	internal fun `fjernTilgangTilGjennomforing - skal ha status 200 og fjerne tilgang`() {
		val response = sendRequest(
			method = "DELETE",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/tilgang",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		response.code shouldBe 200

		val tilganger = testDataRepository.getArrangorAnsattGjennomforingTilganger(ARRANGOR_ANSATT_1.id)
		tilganger.any {
			it.gjennomforingId == GJENNOMFORING_1.id && it.gyldigTil.isBefore(ZonedDateTime.now().plusSeconds(10))
		} shouldBe true
	}

	@Test
	internal fun `hentDeltakere - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/deltakere",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentDeltakere - skal ha status 200 og returnere deltakere`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/deltakere",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","aktivEndringsmelding":null},{"id":"8a0b7158-4d5e-4563-88be-b9bce5662879","fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","startDato":"2022-02-10","sluttDato":"2022-02-12","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-10T12:12:00","aktivEndringsmelding":null}]""".trimIndent()
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	internal fun `hentDeltakere - returnere deltakere med aktive endringsmeldinger`() {
		val endringsmelding = EndringsmeldingInput(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_1.id,
			startDato = LocalDate.parse("2022-10-21"),
			sluttDato = null,
			aktiv = true,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
		)

		testDataRepository.insertEndringsmelding(endringsmelding)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/deltakere",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","aktivEndringsmelding":{"startDato":"2022-10-21","sluttDato":null}},{"id":"8a0b7158-4d5e-4563-88be-b9bce5662879","fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","startDato":"2022-02-10","sluttDato":"2022-02-12","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-10T12:12:00","aktivEndringsmelding":null}]"""
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	internal fun `hentDeltakere - returnere ikke deltakere med status avsluttet etter 14 dager`() {
		testDataRepository.deleteAllDeltaker()

		val deltaker = DELTAKER_1.copy(id = UUID.randomUUID())
		testDataRepository.insertDeltaker(deltaker)

		testDataRepository.insertDeltakerStatus(
			DeltakerStatusInput(
				id = UUID.randomUUID(),
				deltakerId = deltaker.id,
				gyldigFra = LocalDateTime.now().minusDays(15),
				status = Deltaker.Status.HAR_SLUTTET.name,
				aktiv = true,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/deltakere",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = "[]"
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	internal fun `hentDeltakere - returnerer deltakere med status avsluttet for 14 dager`() {
		testDataRepository.deleteAllDeltaker()

		val deltaker = DELTAKER_1.copy(id = UUID.randomUUID())
		testDataRepository.insertDeltaker(deltaker)

		testDataRepository.insertDeltakerStatus(
			DeltakerStatusInput(
				id = UUID.randomUUID(),
				deltakerId = deltaker.id,
				gyldigFra = LocalDateTime.now().minusDays(13),
				status = Deltaker.Status.HAR_SLUTTET.name,
				aktiv = true,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/deltakere",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200
		JsonPath.parse(response.body?.string()).read<Int>("$.length()") shouldBe 1
	}

	@Test
	internal fun `hentKoordinatorerPaGjennomforing - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_2.id}/koordinatorer",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentKoordinatorerPaGjennomforing - skal ha status 200 og returnere koordinatorer`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${GJENNOMFORING_1.id}/koordinatorer",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """[{"fornavn":"Ansatt 1 fornavn","mellomnavn":"Ansatt 1 mellomnavn","etternavn":"Ansatt 1 etternavn","telefonnummer":null,"diskresjonskode":null}]"""

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


}
