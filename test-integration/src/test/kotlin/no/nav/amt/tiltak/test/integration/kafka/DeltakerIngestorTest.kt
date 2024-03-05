package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.tiltak.DeltakelsesInnhold
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerEndring
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerHistorikk
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Innhold
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.kafka.deltaker_ingestor.DeltakerDto
import no.nav.amt.tiltak.kafka.deltaker_ingestor.DeltakerDtoJsonHistorikk
import no.nav.amt.tiltak.kafka.deltaker_ingestor.DeltakerPersonaliaDto
import no.nav.amt.tiltak.kafka.deltaker_ingestor.DeltakerStatusDto
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.utils.AsyncUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DeltakerIngestorTest : IntegrationTestBase() {
	@Autowired
	lateinit var deltakerService: DeltakerService

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	fun `ingest - ny deltaker - oppretter deltaker`() {
		val deltakerDto = mockDeltakerDto(TestData.GJENNOMFORING_1.id)
		val value = toJsonString(deltakerDto.toDeltakerDtoJsonHistorikk())

		kafkaMessageSender.sendTilDeltakerV2Topic(deltakerDto.id, value)

		AsyncUtils.eventually {
			val lagretDeltaker = deltakerService.hentDeltaker(deltakerDto.id)
			lagretDeltaker shouldNotBe null
			sammenlign(lagretDeltaker!!, deltakerDto)
		}
	}

	@Test
	fun `ingest - ny deltaker, deltakerliste finnes ikke i Arena - lagres ikke`() {
		val deltakerlisteId = UUID.randomUUID()
		mockMulighetsrommetApiServer.gjennomforingArenaData(deltakerlisteId, null)
		val deltakerDto = mockDeltakerDto(deltakerlisteId)
		val value = toJsonString(deltakerDto.toDeltakerDtoJsonHistorikk())

		kafkaMessageSender.sendTilDeltakerV2Topic(deltakerDto.id, value)

		AsyncUtils.eventually {
			val lagretDeltaker = deltakerService.hentDeltaker(deltakerDto.id)
			lagretDeltaker shouldBe null
		}
	}

	private fun mockDeltakerDto(deltakerlisteId: UUID): DeltakerDto {
		val deltakerId = UUID.randomUUID()
		return DeltakerDto(
			id = deltakerId,
			deltakerlisteId = deltakerlisteId,
			personalia = DeltakerPersonaliaDto(
				personId = UUID.randomUUID(),
				personident = "12345678910",
				navn = DeltakerPersonaliaDto.Navn("Fornavn", null, "Etternavn"),
				kontaktinformasjon = DeltakerPersonaliaDto.DeltakerKontaktinformasjonDto("90909090", "epost@nav.no"),
				skjermet = false,
				adresse = null,
				adressebeskyttelse = null
			),
			status = DeltakerStatusDto(
				id = UUID.randomUUID(),
				type = DeltakerStatus.Type.VENTER_PA_OPPSTART,
				aarsak = null,
				gyldigFra = LocalDateTime.now(),
				opprettetDato = LocalDateTime.now()
			),
			dagerPerUke = null,
			prosentStilling = 100.0,
			oppstartsdato = LocalDate.now().plusDays(1),
			sluttdato = LocalDate.now().plusWeeks(5),
			innsoktDato = LocalDate.now().minusDays(2),
			forsteVedtakFattet = LocalDate.now().minusDays(1),
			bestillingTekst = "Dette tiltaket vil være nyttig",
			navKontor = "0101",
			navVeileder = null,
			deltarPaKurs = false,
			kilde = Kilde.KOMET,
			innhold = DeltakelsesInnhold(
				"Ledetekst",
				listOf(
					Innhold(
						tekst = "Visningstekst",
						innholdskode = "type",
						beskrivelse = null
					)
				)
			),
			historikk = listOf(
				DeltakerHistorikk.Endring(
					DeltakerEndring(
						id = UUID.randomUUID(),
						deltakerId = deltakerId,
						endring = DeltakerEndring.Endring.EndreBakgrunnsinformasjon("Bakgrunn"),
						endretAv = UUID.randomUUID(),
						endretAvEnhet = UUID.randomUUID(),
						endret = LocalDateTime.now().minusDays(1)
					)
				)
			),
			sistEndretAv = UUID.randomUUID(),
			sistEndretAvEnhet = UUID.randomUUID(),
			sistEndret = LocalDateTime.now()
		)
	}

	private fun sammenlign(faktisk: Deltaker, forventet: DeltakerDto) {
		faktisk.id shouldBe forventet.id
		faktisk.personIdent shouldBe forventet.personalia.personident
		faktisk.gjennomforingId shouldBe forventet.deltakerlisteId
		faktisk.startDato shouldBe forventet.oppstartsdato
		faktisk.sluttDato shouldBe forventet.sluttdato
		faktisk.dagerPerUke shouldBe forventet.dagerPerUke
		faktisk.prosentStilling shouldBe forventet.prosentStilling
		faktisk.innsokBegrunnelse shouldBe forventet.bestillingTekst
		faktisk.endretDato shouldBeCloseTo LocalDateTime.now()
		faktisk.registrertDato shouldBeCloseTo forventet.innsoktDato.atStartOfDay()
		faktisk.forsteVedtakFattet shouldBe forventet.forsteVedtakFattet
		faktisk.innhold shouldBe forventet.innhold
		faktisk.kilde shouldBe Kilde.KOMET
		(faktisk.historikk!!.first() as DeltakerHistorikk.Endring).endring.deltakerId shouldBe forventet.id
		faktisk.sistEndretAv shouldBe forventet.sistEndretAv
		faktisk.sistEndretAvEnhet shouldBe forventet.sistEndretAvEnhet
		sammenlignStatus(faktisk.status, forventet.status)
	}

	private fun sammenlignStatus(faktisk: DeltakerStatus, forventet: DeltakerStatusDto) {
		faktisk.id shouldBe forventet.id
		faktisk.type shouldBe forventet.type
		faktisk.aarsak shouldBe forventet.aarsak
		faktisk.gyldigFra shouldBeEqualTo forventet.gyldigFra
		faktisk.opprettetDato shouldBeCloseTo LocalDateTime.now()
		faktisk.aktiv shouldBe true
	}

	private fun DeltakerDto.toDeltakerDtoJsonHistorikk(): DeltakerDtoJsonHistorikk {
		val h = historikk?.let { h -> toJsonString(h.map { toJsonString(it) }) }
		return DeltakerDtoJsonHistorikk(
			id = id,
			deltakerlisteId = deltakerlisteId,
			personalia = personalia,
			status = status,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			oppstartsdato = oppstartsdato,
			sluttdato = sluttdato,
			innsoktDato = innsoktDato,
			forsteVedtakFattet = forsteVedtakFattet,
			bestillingTekst = bestillingTekst,
			navKontor = navKontor,
			navVeileder = navVeileder,
			deltarPaKurs = deltarPaKurs,
			kilde = kilde,
			innhold = innhold,
			historikk = h,
			sistEndretAv = sistEndretAv,
			sistEndretAvEnhet = sistEndretAvEnhet,
			sistEndret = sistEndret
		)
	}
}
