package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_ADRESSEBESKYTTET
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_SKJERMET
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.inputs.BrukerInput
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ApiTestUtils.testNavAnsattAutentisering
import okhttp3.Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class EndringsmeldingAPIIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var endringsmeldingRepository: EndringsmeldingRepository

	@Autowired
	lateinit var vurderingRepository: VurderingRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testDataRepository.deleteAllEndringsmeldinger()
		testDataRepository.deleteAllVurderinger()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/endringsmelding"),
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/meldinger"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/nav-ansatt/endringsmelding/${UUID.randomUUID()}/ferdig"),
		)
		testNavAnsattAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {

		testDataRepository.deleteAllTiltaksansvarligGjennomforingTilgang()

		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		Assertions.assertEquals(403, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - uten tilgang til skjermede personer - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertSkjermetPersonMedEndringsmeldinger()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":true,"adressebeskyttelse":null},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}]
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


	@Test
	fun `hentEndringsmeldinger() - med tilgang til skjermede personer - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()

		val endringsmeldingInput = insertSkjermetPersonMedEndringsmeldinger()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.endringsmeldingGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.tilgangTilNavAnsattGroupId,
			)

		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":"Skjermet bruker fornavn","mellomnavn":null,"etternavn":"Skjermet bruker etternavn","fodselsnummer":"10101010101","erSkjermet":true,"adressebeskyttelse":null},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}]
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentEndringsmeldinger() - uten tilgang til adressebeskyttet personer - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertAdressebeskyttetPersonMedEndringsmeldinger()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}]
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


	@Test
	fun `hentEndringsmeldinger() - med tilgang til adressebeskyttet personer - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertAdressebeskyttetPersonMedEndringsmeldinger()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.endringsmeldingGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.adressebeskyttelseStrengtFortroligGroupId,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":"Beskyttet bruker fornavn","mellomnavn":null,"etternavn":"Beskyttet bruker etternavn","fodselsnummer":"6543219870","erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}]
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentMeldingerFraArrangor() - skal returnere 403 hvis ikke tilgang til gjennomforing`() {

		testDataRepository.deleteAllTiltaksansvarligGjennomforingTilgang()

		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/meldinger?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		Assertions.assertEquals(403, response.code)
	}

	@Test
	fun `hentMeldingerFraArrangor() - skal returnere 200 med riktig, maskert response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertSkjermetPersonMedEndringsmeldinger()
		insertVurdering(endringsmeldingInput.deltakerId)

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/meldinger?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				{"endringsmeldinger":[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":true,"adressebeskyttelse":null},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}],"vurderinger":[{"id":"866a387f-87d1-4623-8010-32fcdea5464e","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":true,"adressebeskyttelse":null},"vurderingstype":"OPPFYLLER_KRAVENE","begrunnelse":null,"opprettetDato":"2022-11-08T15:00:00"}]}
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}


	@Test
	fun `hentMeldingerFraArrangor() - med tilgang til skjermede personer - skal returnere 200 med riktig response`() {
		val oid = UUID.randomUUID()

		val endringsmeldingInput = insertSkjermetPersonMedEndringsmeldinger()
		insertVurdering(endringsmeldingInput.deltakerId)

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.endringsmeldingGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.tilgangTilNavAnsattGroupId,
			)

		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/meldinger?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				{"endringsmeldinger":[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":"Skjermet bruker fornavn","mellomnavn":null,"etternavn":"Skjermet bruker etternavn","fodselsnummer":"10101010101","erSkjermet":true,"adressebeskyttelse":null},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}],"vurderinger":[{"id":"866a387f-87d1-4623-8010-32fcdea5464e","deltaker":{"fornavn":"Skjermet bruker fornavn","mellomnavn":null,"etternavn":"Skjermet bruker etternavn","fodselsnummer":"10101010101","erSkjermet":true,"adressebeskyttelse":null},"vurderingstype":"OPPFYLLER_KRAVENE","begrunnelse":null,"opprettetDato":"2022-11-08T15:00:00"}]}
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentMeldingerFraArrangor() - adressebeskyttet person uten tilgang - skal returnere 200 med riktig, maskert response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertAdressebeskyttetPersonMedEndringsmeldinger()
		insertVurdering(endringsmeldingInput.deltakerId)

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/meldinger?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				{"endringsmeldinger":[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}],"vurderinger":[{"id":"866a387f-87d1-4623-8010-32fcdea5464e","deltaker":{"fornavn":null,"mellomnavn":null,"etternavn":null,"fodselsnummer":null,"erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"vurderingstype":"OPPFYLLER_KRAVENE","begrunnelse":null,"opprettetDato":"2022-11-08T15:00:00"}]}
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `hentMeldingerFraArrangor() - adressebeskyttet person med tilgang - skal returnere 200 med riktig, umaskert response`() {
		val oid = UUID.randomUUID()
		val endringsmeldingInput = insertAdressebeskyttetPersonMedEndringsmeldinger()
		insertVurdering(endringsmeldingInput.deltakerId)

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.endringsmeldingGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.adressebeskyttelseStrengtFortroligGroupId,
			),
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/meldinger?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token")
		)

		val expectedJson = """
				{"endringsmeldinger":[{"id":"${endringsmeldingInput.id}","deltaker":{"fornavn":"Beskyttet bruker fornavn","mellomnavn":null,"etternavn":"Beskyttet bruker etternavn","fodselsnummer":"6543219870","erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"innhold":{"oppstartsdato":"2022-11-09"},"status":"AKTIV","opprettetDato":"2022-11-08T16:00:00+01:00","utfortTidspunkt":null,"type":"LEGG_TIL_OPPSTARTSDATO"}],"vurderinger":[{"id":"866a387f-87d1-4623-8010-32fcdea5464e","deltaker":{"fornavn":"Beskyttet bruker fornavn","mellomnavn":null,"etternavn":"Beskyttet bruker etternavn","fodselsnummer":"6543219870","erSkjermet":false,"adressebeskyttelse":"STRENGT_FORTROLIG"},"vurderingstype":"OPPFYLLER_KRAVENE","begrunnelse":null,"opprettetDato":"2022-11-08T15:00:00"}]}
			""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson
	}

	@Test
	fun `markerFerdig() - skal returnere 200 og markere som ferdig`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		testDataRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

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

	@Test
	fun `markerFerdig() - skal returnere 403 og markere som ferdig`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val endringsmelding = insertSkjermetPersonMedEndringsmeldinger()
		val endringsmeldingBefore = endringsmeldingRepository.get(endringsmelding.id)

		endringsmeldingBefore.status shouldBe Endringsmelding.Status.AKTIV

		val response = sendRequest(
			method = "PATCH",
			url = "/api/nav-ansatt/endringsmelding/${endringsmelding.id}/ferdig",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		response.code shouldBe 403

		val endringsmeldingAfter = endringsmeldingRepository.get(endringsmelding.id)

		endringsmeldingAfter.status shouldBe Endringsmelding.Status.AKTIV

	}

	private fun insertSkjermetPersonMedEndringsmeldinger() : EndringsmeldingInput {
		return insertPersonMedEndringsmeldinger(BRUKER_SKJERMET)
	}

	private fun insertAdressebeskyttetPersonMedEndringsmeldinger() : EndringsmeldingInput {
		return insertPersonMedEndringsmeldinger(BRUKER_ADRESSEBESKYTTET)
	}

	private fun insertPersonMedEndringsmeldinger(bruker: BrukerInput) : EndringsmeldingInput {
		val deltaker = TestData.createDeltakerInput(bruker, GJENNOMFORING_1)
		val endringsmelding = TestData.createEndringsmelding(deltaker, ARRANGOR_ANSATT_1)
		val status = TestData.createStatusInput(deltaker)

		testDataRepository.insertBruker(bruker)
		testDataRepository.insertDeltaker(deltaker)
		testDataRepository.insertDeltakerStatus(status)
		testDataRepository.insertEndringsmelding(endringsmelding)
		return endringsmelding
	}

	private fun insertVurdering(deltakerId: UUID): VurderingDbo {
		val vurdering = VurderingDbo(
			id = UUID.fromString("866a387f-87d1-4623-8010-32fcdea5464e"),
			deltakerId = deltakerId,
			vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
			begrunnelse = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.parse("2022-11-08T15:00:00.00000"),
			gyldigTil = null
		)
		vurderingRepository.insert(vurdering)
		return vurdering
	}
}
