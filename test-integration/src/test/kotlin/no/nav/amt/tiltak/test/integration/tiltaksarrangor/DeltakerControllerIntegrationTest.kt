package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
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

	@Autowired
	lateinit var deltakerService: DeltakerService

	val dato = "2022-11-01"
	val deltakerIkkeTilgang = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_2.id)
	val deltakerIkkeTilgangStatus = DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerIkkeTilgang.id )
	val createAnsatt1AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}") }
	val createAnsatt2AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}") }

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testDataRepository.insertDeltaker(deltakerIkkeTilgang)
		testDataRepository.insertDeltakerStatus(deltakerIkkeTilgangStatus)
		testDataRepository.deleteAllEndringsmeldinger()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/deltaker?gjennomforingId=${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}"),
			Request.Builder().post(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/avslutt-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/forleng-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/ikke-aktuell"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should perform authorization check`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}",
			headers = createAnsatt1AuthHeader()
		)

		response.code shouldBe 404
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}",
			headers = createAnsatt1AuthHeader()
		)

		response.code shouldBe 400
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 200 when authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}",
			headers = createAnsatt1AuthHeader()
		)
		val expectedBody = """
{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","telefonnummer":"73404782","epost":"bruker1@example.com","deltakelseProsent":100,"navEnhet":{"navn":"NAV Testheim"},"navVeileder":{"navn":"Vashnir Veiledersen","telefon":"88776655","epost":"vashnir.veiledersen@nav.no"},"startDato":"2022-02-13","sluttDato":"2030-02-14","registrertDato":"2022-02-13T12:12:00","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"gjennomforing":{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1","virksomhetOrgnr":"111111111"}},"fjernesDato":null,"innsokBegrunnelse":"begrunnelse deltaker 1"}
""".trimIndent()


		response.code shouldBe 200
		response.body?.string() shouldBe expectedBody

	}

	@Test
	internal fun `hentDeltakere - har ikke tilgang til gjennomforing - skal kaste 403`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_2.id}",
			headers = createAnsatt1AuthHeader()
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentDeltakere - skal ha status 200 og returnere deltakere`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = createAnsatt1AuthHeader()
		)

		val expectedJson = """
				[{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","startDato":"2022-02-13","sluttDato":"2030-02-14","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-13T12:12:00","aktiveEndringsmeldinger":[]},{"id":"8a0b7158-4d5e-4563-88be-b9bce5662879","fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","startDato":"2022-02-10","sluttDato":"2022-02-12","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"registrertDato":"2022-02-10T12:12:00","aktiveEndringsmeldinger":[]}]
			""".trimIndent()
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	internal fun `hentDeltakere - skal ikke vise deltakere som er skjulte`() {
		val deltakerId = UUID.randomUUID()
		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId, gjennomforingId = GJENNOMFORING_1.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "IKKE_AKTUELL"))

		deltakerService.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = createAnsatt1AuthHeader()
		)

		response.code shouldBe 200

		val body = response.body?.string()

		JsonPath.parse(body).read<Int>("$.length()") shouldBe 2
		JsonPath.parse(body).read<List<String>>("$[*].id") shouldBe listOf(DELTAKER_1.id.toString(), DELTAKER_2.id.toString())
	}

	@Test
	internal fun `hentDeltakere - returnere deltakere med aktive endringsmeldinger`() {
		testDataRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testDataRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_2)
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = createAnsatt1AuthHeader()
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
				status = DeltakerStatus.Type.HAR_SLUTTET.name,
				aktiv = true,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = createAnsatt1AuthHeader()
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
				status = DeltakerStatus.Type.HAR_SLUTTET.name,
				aktiv = true,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/deltaker?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = createAnsatt1AuthHeader()
		)

		response.code shouldBe 200
		JsonPath.parse(response.body?.string()).read<Int>("$.length()") shouldBe 1
	}

	@Test
	fun `endreOppstartsdato() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreOppstartsdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreOppstartsdatoInnhold).oppstartsdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `endreOppstartsdato() skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `endreOppstartsdato() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `endreDeltakelsesprosent() skal returnere 403 om Ansatt kun er veileder`() {
		val deltakelseProsent = 95

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/deltakelse-prosent",
			headers = createAnsatt2AuthHeader(),
			body = """{"deltakelseProsent": $deltakelseProsent}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}


	@Test
	internal fun `endreDeltakelsesprosent skal returnere 200 og opprette endringsmelding`() {
		val deltakelseProsent = 95
		val gyldigFraDato = LocalDate.now()

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/deltakelse-prosent",
			headers = createAnsatt1AuthHeader(),
			body = """{"deltakelseProsent": $deltakelseProsent, "gyldigFraDato": "$gyldigFraDato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)

		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreDeltakelseProsentInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).deltakelseProsent shouldBe deltakelseProsent
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).gyldigFraDato shouldBe gyldigFraDato
	}

	@Test
	internal fun `endreDeltakelsesprosent skal returnere 200 og opprette endringsmelding selv om gyldigFraDato mangler`() {
		val deltakelseProsent = 95

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/deltakelse-prosent",
			headers = createAnsatt1AuthHeader(),
			body = """{"deltakelseProsent": $deltakelseProsent}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)

		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreDeltakelseProsentInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).deltakelseProsent shouldBe deltakelseProsent
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).gyldigFraDato shouldBe null
	}

	@Test
	fun `endreDeltakelsesprosent skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/deltakelse-prosent",
			headers = createAnsatt1AuthHeader(),
			body = """{"deltakelseProsent": 12}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `avsluttDeltakelse() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/avslutt-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato", "aarsak": {"type": "FATT_JOBB"} }""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.AvsluttDeltakelseInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.AvsluttDeltakelseInnhold).sluttdato shouldBe LocalDate.parse(dato)
		(endringsmelding.innhold as Endringsmelding.Innhold.AvsluttDeltakelseInnhold).aarsak.type shouldBe DeltakerStatus.Aarsak.Type.FATT_JOBB
	}

	@Test
	fun `avsluttDeltakelse skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/avslutt-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato", "aarsak": {"type": "FATT_JOBB"} }""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `avsluttDeltakelse() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/avslutt-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato", "aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}
	@Test
	fun `forlengDeltakelse() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/forleng-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.ForlengDeltakelseInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.ForlengDeltakelseInnhold).sluttdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `forlengDeltakelse skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/forleng-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `forlengDeltakelse() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/forleng-deltakelse",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `forlengDeltakelse() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/forleng-deltakelse",
			headers = createAnsatt2AuthHeader(),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `deltakerIkkeAktuell() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/ikke-aktuell",
			headers = createAnsatt2AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)


		response.code shouldBe 403
	}


	@Test
	fun `deltakerIkkeAktuell() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/ikke-aktuell",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold).aarsak.type shouldBe DeltakerStatus.Aarsak.Type.FATT_JOBB
	}

	@Test
	fun `deltakerIkkeAktuell skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/ikke-aktuell",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `deltakerIkkeAktuell() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/ikke-aktuell",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `leggTilOppstartsdato() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold).oppstartsdato shouldBe LocalDate.parse(dato)
	}

	@Test
	fun `leggTilOppstartsdato skal returnere 400 hvis deltaker er skjult`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/${opprettSkjultDeltaker()}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `leggTilOppstartsdato() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor() - skal skjule deltaker`() {
		val deltakerId = UUID.randomUUID()
		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "IKKE_AKTUELL"))

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerId}/skjul",
			headers = createAnsatt1AuthHeader(),
		)

		response.code shouldBe 200

		deltakerService.erSkjultForTiltaksarrangor(deltakerId) shouldBe true
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/skjul",
			headers = createAnsatt1AuthHeader(),
		)

		response.code shouldBe 403
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor() skal returnere 403 om Ansatt kun er veileder`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/skjul",
			headers = createAnsatt2AuthHeader(),
		)


		response.code shouldBe 403
	}


	private fun opprettSkjultDeltaker(): UUID {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId, gjennomforingId = GJENNOMFORING_1.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "IKKE_AKTUELL"))

		deltakerService.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)

		return deltakerId
	}

}
