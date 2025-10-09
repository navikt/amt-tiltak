package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.common.json.JsonUtils.objectMapper
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ApiTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeltakerAPIIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var endringsmeldingService: EndringsmeldingService

	@Autowired
	lateinit var vurderingRepository: VurderingRepository

	val dato = "2022-11-01"
	val deltakerIkkeTilgang = DELTAKER_1.copy(id = UUID.randomUUID(), gjennomforingId = GJENNOMFORING_2.id)
	val deltakerIkkeTilgangStatus = DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerIkkeTilgang.id )
	val createAnsatt1AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}") }
	val createAnsatt2AuthHeader = { mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ARRANGOR_ANSATT_2.personligIdent)}") }

	@BeforeEach
	fun setup() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
		testDataRepository.insertDeltaker(deltakerIkkeTilgang)
		testDataRepository.insertDeltakerStatus(deltakerIkkeTilgangStatus)
		testDataRepository.deleteAllEndringsmeldinger()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().post(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/oppstartsdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/avslutt-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/forleng-deltakelse"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/ikke-aktuell"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/endre-sluttdato"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/tiltaksarrangor/deltaker/${UUID.randomUUID()}/sluttaarsak"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
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

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
	}

	@Test
	fun `endreOppstartsdato() - dato er null - skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/oppstartsdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"oppstartsdato": null}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreOppstartsdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreOppstartsdatoInnhold).oppstartsdato shouldBe null

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
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

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
	}

	@Test
	internal fun `endreDeltakelsesprosent med dagerPerUke skal returnere 200 og opprette endringsmelding`() {
		val deltakelseProsent = 95
		val gyldigFraDato = LocalDate.now()

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/deltakelse-prosent",
			headers = createAnsatt1AuthHeader(),
			body = """{"deltakelseProsent": $deltakelseProsent, "dagerPerUke": 3, "gyldigFraDato": "$gyldigFraDato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)

		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreDeltakelseProsentInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).deltakelseProsent shouldBe deltakelseProsent
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).gyldigFraDato shouldBe gyldigFraDato
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreDeltakelseProsentInnhold).dagerPerUke shouldBe 3
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
		(endringsmelding.innhold as Endringsmelding.Innhold.AvsluttDeltakelseInnhold).aarsak.type shouldBe EndringsmeldingStatusAarsak.Type.FATT_JOBB

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
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

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
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
		(endringsmelding.innhold as Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold).aarsak.type shouldBe EndringsmeldingStatusAarsak.Type.FATT_JOBB

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
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

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
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
	fun `endreSluttdato() skal returnere 200 og opprette endringsmelding`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/endre-sluttdato",
			headers = createAnsatt1AuthHeader(),
			body = """{"sluttdato": "$dato"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(DELTAKER_1.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreSluttdatoInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreSluttdatoInnhold).sluttdato shouldBe LocalDate.parse(dato)

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
	}

	@Test
	fun `registrerVurdering() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/vurdering",
			headers = createAnsatt1AuthHeader(),
			body = """{"id": "${UUID.randomUUID()}", "opprettet": ${objectMapper.writeValueAsString(LocalDateTime.now())}, "vurderingstype": "OPPFYLLER_IKKE_KRAVENE", "begrunnelse": "Mangler førerkort"}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}

	@Test
	fun `registrerVurdering() skal returnere 200 og opprette vurdering`() {
		val id = UUID.randomUUID()
		val deltakerId = UUID.randomUUID()
		val opprettet = LocalDateTime.now()
		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "VURDERES"))

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/$deltakerId/vurdering",
			headers = createAnsatt1AuthHeader(),
			body = """{"id": "$id", "opprettet": ${objectMapper.writeValueAsString(opprettet)}, "vurderingstype": "OPPFYLLER_IKKE_KRAVENE", "begrunnelse": "Mangler førerkort"}""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val vurderinger = vurderingRepository.getVurderingerForDeltaker(deltakerId)
		vurderinger shouldHaveSize 1

		val vurdering = vurderinger.first()
		vurdering.id shouldBe id
		vurdering.vurderingstype shouldBe Vurderingstype.OPPFYLLER_IKKE_KRAVENE
		vurdering.begrunnelse shouldBe "Mangler førerkort"
		vurdering.gyldigTil shouldBe null
		val vurderingerFraResponse = response.body?.string()?.let { fromJsonString<List<Vurdering>>(it) }?.firstOrNull()
		vurderingerFraResponse?.id shouldBe id
		vurderingerFraResponse?.vurderingstype shouldBe Vurderingstype.OPPFYLLER_IKKE_KRAVENE
		vurderingerFraResponse?.begrunnelse shouldBe "Mangler førerkort"
		vurderingerFraResponse?.opprettet!! shouldBeCloseTo opprettet
	}

	@Test
	fun `registrerVurdering skal returnere 400 hvis deltaker ikke har status VURDERES`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/vurdering",
			headers = createAnsatt1AuthHeader(),
			body = """{"id": "${UUID.randomUUID()}", "opprettet": ${objectMapper.writeValueAsString(LocalDateTime.now())}, "vurderingstype": "OPPFYLLER_IKKE_KRAVENE", "begrunnelse": "Mangler førerkort"}""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `endreSluttaarsak() skal returnere 200 og opprette endringsmelding`() {
		val deltaker = DELTAKER_1.copy(id = UUID.randomUUID())

		testDataRepository.insertDeltaker(deltaker)
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			status = "HAR_SLUTTET"
		))

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltaker.id}/sluttaarsak",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"} }""".toJsonRequestBody()
		)

		response.code shouldBe 200

		val endringsmeldinger = endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(deltaker.id)
		endringsmeldinger shouldHaveSize 1

		val endringsmelding = endringsmeldinger.first()
		endringsmelding.innhold should beInstanceOf<Endringsmelding.Innhold.EndreSluttaarsakInnhold>()
		endringsmelding.status shouldBe Endringsmelding.Status.AKTIV
		(endringsmelding.innhold as Endringsmelding.Innhold.EndreSluttaarsakInnhold).aarsak.type shouldBe EndringsmeldingStatusAarsak.Type.FATT_JOBB

		response.body?.string() shouldBe """{"id":"${endringsmelding.id}"}"""
	}

	@Test
	fun `endreSluttaarsak() skal returnere 400 hvis deltaker ikke har riktig status`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${DELTAKER_1.id}/sluttaarsak",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"} }""".toJsonRequestBody()
		)

		response.code shouldBe 400
	}

	@Test
	fun `endreSluttaarsak() skal returnere 403 hvis ikke tilgang`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksarrangor/deltaker/${deltakerIkkeTilgang.id}/sluttaarsak",
			headers = createAnsatt1AuthHeader(),
			body = """{"aarsak": {"type": "FATT_JOBB"}}""".toJsonRequestBody()
		)

		response.code shouldBe 403
	}
}
