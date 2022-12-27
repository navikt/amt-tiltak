package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_2
import no.nav.amt.tiltak.test.database.data.inputs.GjennomforingInput
import no.nav.amt.tiltak.test.database.data.inputs.NavAnsattInput
import no.nav.amt.tiltak.test.database.data.inputs.TiltaksansvarligGjennomforingTilgangInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testNavAnsattAutentisering
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGrupper
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.*

class GjennomforingControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/gjennomforing"),
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/gjennomforing/${UUID.randomUUID()}"),
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/gjennomforing?lopenr=1234"),
		)
		testNavAnsattAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	internal fun `hentGjennomforinger - skal ha status 200 med korrekt respons`() {
		val token = lagTokenMedAdGruppe(NAV_ANSATT_1)
		giTilgang(NAV_ANSATT_1, GJENNOMFORING_1)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing/",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
			[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","arrangorNavn":"Org Tiltaksarrangør 1","lopenr":123,"opprettetAar":2020,"antallAktiveEndringsmeldinger":3,"harSkjermedeDeltakere":false,"tiltak":{"kode":"AMO","navn":"Tiltak1"}},{"id":"513219ca-481b-4aae-9d51-435dba9929cd","navn":"Tiltaksgjennomforing2","arrangorNavn":"Org Tiltaksarrangør 2","lopenr":124,"opprettetAar":2020,"antallAktiveEndringsmeldinger":0,"harSkjermedeDeltakere":false,"tiltak":{"kode":"AMO","navn":"Tiltak1"}}]""".trimIndent()
		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentGjennomforinger() - skal returnere 403 hvis ikke tilgang til flate`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = emptyArray(),
		)

		mockPoaoTilgangHttpServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = "IngenNyttigGruppe"
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing/",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentGjennomforing - skal ha status 200 med korrekt respons`() {
		val token = lagTokenMedAdGruppe(NAV_ANSATT_1)
		giTilgang(NAV_ANSATT_1, GJENNOMFORING_1)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
			{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","tiltakNavn":"Tiltak1","startDato":"2022-02-01","sluttDato":"2050-12-30","arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","virksomhetOrgnr":"111111111","organisasjonNavn":"Org Tiltaksarrangør 1","organisasjonOrgnr":"911111111"},"lopenr":123,"opprettetAr":2020,"tiltak":{"kode":"AMO","navn":"Tiltak1"}}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentGjennomforing() - skal returnere 403 hvis ikke tilgang til flate`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = emptyArray(),
		)

		mockPoaoTilgangHttpServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = "IngenNyttigGruppe"
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		response.code shouldBe 403
	}

	@Test
	fun `hentGjennomforing() - skal returnere 403 hvis ikke tilgang til gjennomforing`() {
		val token = lagTokenMedAdGruppe(NAV_ANSATT_2)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing/${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		response.code shouldBe 403
	}

	@Test
	internal fun `hentGjennomforingerMedLopenr - skal ha status 200 med korrekt respons`() {
		val token = lagTokenMedAdGruppe(NAV_ANSATT_1)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing?lopenr=${GJENNOMFORING_1.lopenr}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """[{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","lopenr":123,"opprettetAr":2020,"arrangorNavn":"Org Tiltaksarrangør 1","tiltak":{"kode":"AMO","navn":"Tiltak1"}}]"""

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentGjennomforingerMedLopenr() - skal returnere 403 hvis ikke tilgang til flate`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = emptyArray(),
		)

		mockPoaoTilgangHttpServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = "IngenNyttigGruppe"
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing?lopenr=${GJENNOMFORING_1.lopenr}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		response.code shouldBe 403
	}

	private fun giTilgang(ansatt: NavAnsattInput, gjennomforing: GjennomforingInput) {
		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			TiltaksansvarligGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				navAnsattId = ansatt.id,
				gjennomforingId = gjennomforing.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)
	}

	private fun lagTokenMedAdGruppe(ansatt: NavAnsattInput): String {
		val oid = UUID.randomUUID()

		mockPoaoTilgangHttpServer.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE
		)

		return mockOAuthServer.issueAzureAdToken(
			ident = ansatt.navIdent,
			oid = oid,
		)
	}

}
