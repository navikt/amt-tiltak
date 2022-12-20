package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testNavAnsattAutentisering
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGrupper
import okhttp3.Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class EndringsmeldingControllerIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var endringsmeldingRepository: EndringsmeldingRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/endringsmelding"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/nav-ansatt/endringsmelding/${UUID.randomUUID()}/ferdig"),
		)
		testNavAnsattAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {

		testDataRepository.deleteAllTiltaksansvarligGjennomforingTilgang()

		val oid = UUID.randomUUID()

		poaoTilgangServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		Assertions.assertEquals(403, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()

		poaoTilgangServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
			[{"id":"9830e130-b18a-46b8-8e3e-6c06734d797e","deltaker":{"fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","erSkjermet":false},"innhold":{"oppstartsdato":"2022-11-11"},"status":"AKTIV","opprettetDato":"2022-11-08T14:00:00+01:00","type":"LEGG_TIL_OPPSTARTSDATO"},{"id":"07099997-e02e-45e3-be6f-3c1eaf694557","deltaker":{"fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","erSkjermet":false},"innhold":{"sluttdato":"2022-11-10","aarsak":{"type":"ANNET","beskrivelse":"Flyttet til utland"}},"status":"AKTIV","opprettetDato":"2022-11-08T15:00:00+01:00","type":"AVSLUTT_DELTAKELSE"},{"id":"3fc16362-ba8b-4c0f-af93-b2ed56f12cd5","deltaker":{"fornavn":"Bruker 2 fornavn","mellomnavn":null,"etternavn":"Bruker 2 etternavn","fodselsnummer":"7908432423","erSkjermet":false},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","type":"LEGG_TIL_OPPSTARTSDATO"}]
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `markerFerdig() - skal returnere 200 og markere som ferdig`() {
		val oid = UUID.randomUUID()

		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		poaoTilgangServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		val endringsmeldingBefore = endringsmeldingRepository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		endringsmeldingBefore.status shouldBe Endringsmelding.Status.AKTIV

		val response = sendRequest(
			method = "PATCH",
			url = "/api/nav-ansatt/endringsmelding/${ENDRINGSMELDING_1_DELTAKER_1.id}/ferdig",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldingAfter = endringsmeldingRepository.get(ENDRINGSMELDING_1_DELTAKER_1.id)

		endringsmeldingAfter.status shouldBe Endringsmelding.Status.UTFORT

	}
}
