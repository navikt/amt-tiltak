package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class GjennomforingControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().post(emptyRequest())
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
			Request.Builder().delete()
				.url("${serverUrl()}/api/tiltaksarrangor/gjennomforing/${UUID.randomUUID()}/tilgang"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
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
}
