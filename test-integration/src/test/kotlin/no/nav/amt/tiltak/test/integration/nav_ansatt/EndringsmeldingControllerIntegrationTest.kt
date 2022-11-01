package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.inputs.TiltaksansvarligGjennomforingTilgangInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testNavAnsattAutentisering
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGrupper
import okhttp3.Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime
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
		val oid = UUID.randomUUID()

		poaoTilgangClient.addHentAdGrupperResponse(
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

		poaoTilgangClient.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			TiltaksansvarligGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		testDataRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)

		val endringsmeldingResponse = endringsmeldingRepository.get(ENDRINGSMELDING1_DELTAKER_1.id)

		endringsmeldingResponse.id shouldBe ENDRINGSMELDING1_DELTAKER_1.id
	}

	@Test
	fun `markerFerdig() - skal returnere 200 og markere som ferdig`() {
		val oid = UUID.randomUUID()

		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		poaoTilgangClient.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			TiltaksansvarligGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		testDataRepository.insertEndringsmelding(ENDRINGSMELDING1_DELTAKER_1)

		val endringsmeldingBefore = endringsmeldingRepository.get(ENDRINGSMELDING1_DELTAKER_1.id)

		endringsmeldingBefore.status shouldBe Endringsmelding.Status.AKTIV

		val response = sendRequest(
			method = "PATCH",
			url = "/api/nav-ansatt/endringsmelding/${ENDRINGSMELDING1_DELTAKER_1.id}/ferdig",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldingAfter = endringsmeldingRepository.get(ENDRINGSMELDING1_DELTAKER_1.id)

		endringsmeldingAfter.status shouldBe Endringsmelding.Status.UTFORT

	}
}
