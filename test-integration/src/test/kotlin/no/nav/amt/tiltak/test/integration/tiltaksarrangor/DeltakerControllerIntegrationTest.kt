package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.inputs.DeltakerStatusInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var endringsmeldingService: EndringsmeldingService

	val dato = "2022-11-01"
	val deltakerIkkeTilgang = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_2.id)
	val deltakerIkkeTilgangStatus = DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerIkkeTilgang.id )

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testDataRepository.insertDeltaker(deltakerIkkeTilgang)
		testDataRepository.insertDeltakerStatus(deltakerIkkeTilgangStatus)
		testDataRepository.deleteAllEndringsmeldinger()
		poaoTilgangServer.addErSkjermetResponse(mapOf(BRUKER_1.fodselsnummer to false))
	}

	@Test
	internal fun `skal teste token autentisering`() {

		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}"),
			Request.Builder().post(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}/avslutt-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}/forleng-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}/ikke-aktuell"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should perform authorization check`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 404

	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 200 when authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedBody = """
			{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","telefonnummer":"73404782","epost":"bruker1@example.com","navEnhet":{"navn":"NAV Testheim"},"navVeileder":{"navn":"Vashnir Veiledersen","telefon":"88776655","epost":"vashnir.veiledersen@nav.no"},"erSkjermetPerson":false,"startDato":"2022-02-13","sluttDato":"2030-02-14","registrertDato":"2022-02-13T12:12:00","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"gjennomforing":{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1"}},"fjernesDato":null,"innsokBegrunnelse":"begrunnelse deltaker 1"}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedBody

	}

	@Test
	internal fun `hentDeltakere - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${GJENNOMFORING_2.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentDeltakere - skal ha status 200 og returnere deltakere`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """
				[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","aktiveEndringsmeldinger":[]},{"id":"8a0b7158-4d5e-4563-88be-b9bce5662879","fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","startDato":"2022-02-10","sluttDato":"2022-02-12","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-10T12:12:00","aktiveEndringsmeldinger":[]}]
			""".trimIndent()
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	internal fun `hentDeltakere - returnere deltakere med aktive endringsmeldinger`() {
		testDataRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testDataRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_2)
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedJson = """
			[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","aktiveEndringsmeldinger":[{"id":"9830e130-b18a-46b8-8e3e-6c06734d797e","innhold":{"oppstartsdato":"2022-11-11"},"type":"LEGG_TIL_OPPSTARTSDATO"}]},{"id":"8a0b7158-4d5e-4563-88be-b9bce5662879","fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","startDato":"2022-02-10","sluttDato":"2022-02-12","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-10T12:12:00","aktiveEndringsmeldinger":[{"id":"3fc16362-ba8b-4c0f-af93-b2ed56f12cd5","innhold":{"oppstartsdato":"2022-11-09"},"type":"LEGG_TIL_OPPSTARTSDATO"}]}]
		""".trimIndent()
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
			url = "/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
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
			url = "/api/tiltaksarrangor/tiltak-deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200
		JsonPath.parse(response.body?.string()).read<Int>("$.length()") shouldBe 1
	}

	@Test
	fun `endreOppstartsdato() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}/oppstartsdato",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreOppstartsdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreOppstartsdatoInnhold).oppstartsdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `endreOppstartsdato() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${deltakerIkkeTilgang.id}/oppstartsdato",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `avsluttDeltakelse() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}/avslutt-deltakelse",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"sluttdato": "$dato", "aarsak": "FATT_JOBB" }""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.AvsluttDeltakelseInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.AvsluttDeltakelseInnhold).sluttdato shouldBe LocalDate.parse(dato)
		(endringsmelding.innhold as Endringsmelding.Innhold.AvsluttDeltakelseInnhold).aarsak shouldBe Deltaker.StatusAarsak.FATT_JOBB
	}

	@Test
	fun `avsluttDeltakelse() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${deltakerIkkeTilgang.id}/avslutt-deltakelse",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"sluttdato": "$dato", "aarsak": "FATT_JOBB" }""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}
	@Test
	fun `forlengDeltakelse() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}/forleng-deltakelse",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.ForlengDeltakelseInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.ForlengDeltakelseInnhold).sluttdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `forlengDeltakelse() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${deltakerIkkeTilgang.id}/forleng-deltakelse",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}
	@Test
	fun `deltakerIkkeAktuell() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}/ikke-aktuell",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"aarsak": "FATT_JOBB"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold).aarsak shouldBe Deltaker.StatusAarsak.FATT_JOBB
	}

	@Test
	fun `deltakerIkkeAktuell() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${deltakerIkkeTilgang.id}/ikke-aktuell",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"aarsak": "FATT_JOBB"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `leggTilOppstartsdato() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}/oppstartsdato",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold).oppstartsdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `leggTilOppstartsdato() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${deltakerIkkeTilgang.id}/oppstartsdato",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}"),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

}