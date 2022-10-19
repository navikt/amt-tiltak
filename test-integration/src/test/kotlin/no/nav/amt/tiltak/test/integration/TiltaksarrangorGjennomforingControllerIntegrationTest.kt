package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class TiltaksarrangorGjennomforingControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/deltakere"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/koordinatorer"),
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/tilgjengelig"),
			Request.Builder().post(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
			Request.Builder().delete().url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
		)
			.forEach {
				val withoutTokenResponse = sendRequest(it.build())
				withoutTokenResponse.code shouldBe 401
				val withoutWrongTokenResponse = sendRequest(it.header(
					name = "authorization",
					value = "Bearer ${oAuthServer.issueAzureAdToken(ident = "", oid = UUID.randomUUID())}")
					.build())
				withoutWrongTokenResponse.code shouldBe 401
			}
	}

	@Test
	internal fun `hentGjennomforing - Tiltaksgjennomforing finnes ikke - skal kaste 403 - FORBIDDEN`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 500
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

}
