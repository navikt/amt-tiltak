package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.test.database.data.inputs.TiltaksansvarligGjennomforingTilgangInput
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGrupper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class EndringsmeldingNavControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding"
		)

		Assertions.assertEquals(401, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {
		poaoTilgangClient.addHentAdGrupperResponse(name = AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		Assertions.assertEquals(403, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 200 med riktig response`() {
		poaoTilgangClient.addHentAdGrupperResponse(name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE)
		poaoTilgangClient.addHentAdGrupperResponse(
			navAnsattAzureId = NAV_ANSATT_1.id,
			name = AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
		)

		db.insertTiltaksansvarligGjennomforingTilgang(
			TiltaksansvarligGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		val endringsmeldingId = UUID.randomUUID()

		db.insertEndringsmelding(
			EndringsmeldingInput(
				id = endringsmeldingId,
				deltakerId = DELTAKER_1.id,
				startDato = LocalDate.now().plusDays(1),
				aktiv = true,
				opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken(NAV_ANSATT_1.navIdent)}")
		)

		response.code shouldBe 200

		val body = response.body?.string()

		body shouldNotBe null

		val responseBody: List<EndringsmeldingDto> = JsonUtils.fromJsonString(body!!)

		responseBody.size shouldBe 1
		responseBody.first().id shouldBe endringsmeldingId
	}
}
