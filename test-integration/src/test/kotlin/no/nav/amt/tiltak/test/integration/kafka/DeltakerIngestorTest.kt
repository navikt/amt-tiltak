package no.nav.amt.tiltak.test.integration.kafka

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Mal
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.kafka.deltaker_ingestor.DeltakerDto
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
		val value = JsonUtils.toJsonString(deltakerDto)

		kafkaMessageSender.sendTilDeltakerEndringTopic(deltakerDto.id, value)

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
		val value = JsonUtils.toJsonString(deltakerDto)

		kafkaMessageSender.sendTilDeltakerEndringTopic(deltakerDto.id, value)

		AsyncUtils.eventually {
			val lagretDeltaker = deltakerService.hentDeltaker(deltakerDto.id)
			lagretDeltaker shouldBe null
		}
	}

	@Test
	fun `ingest - tombstone - sletter deltaker`() {
		kafkaMessageSender.sendTilDeltakerEndringTopic(TestData.DELTAKER_4.id, null)

		AsyncUtils.eventually {
			val lagretDeltaker = deltakerService.hentDeltaker(TestData.DELTAKER_4.id)
			lagretDeltaker shouldBe null
		}
	}

	private fun mockDeltakerDto(deltakerlisteId: UUID): DeltakerDto =
		DeltakerDto(
			id = UUID.randomUUID(),
			personident = "12345678910",
			deltakerlisteId = deltakerlisteId,
			startdato = LocalDate.now().plusDays(1),
			sluttdato = LocalDate.now().plusWeeks(5),
			dagerPerUke = null,
			deltakelsesprosent = 100F,
			bakgrunnsinformasjon = "Dette tiltket vil være nyttig",
			mal = listOf(Mal(
				visningstekst = "Visningstekst",
				type = "type",
				valgt = true,
				beskrivelse = null
			)),
			status = DeltakerStatusDto(
				id = UUID.randomUUID(),
				type = DeltakerStatus.Type.VENTER_PA_OPPSTART,
				aarsak = null,
				gyldigFra = LocalDateTime.now(),
				gyldigTil = null,
				opprettet = LocalDateTime.now()
			),
			sistEndret = LocalDateTime.now(),
			opprettet = LocalDateTime.now()
		)

	private fun sammenlign(faktisk: Deltaker, forventet: DeltakerDto) {
		faktisk.id shouldBe forventet.id
		faktisk.personIdent shouldBe forventet.personident
		faktisk.gjennomforingId shouldBe forventet.deltakerlisteId
		faktisk.startDato shouldBe forventet.startdato
		faktisk.sluttDato shouldBe forventet.sluttdato
		faktisk.dagerPerUke shouldBe forventet.dagerPerUke
		faktisk.prosentStilling shouldBe forventet.deltakelsesprosent
		faktisk.innsokBegrunnelse shouldBe forventet.bakgrunnsinformasjon
		faktisk.endretDato shouldBeCloseTo LocalDateTime.now()
		faktisk.registrertDato shouldBeEqualTo forventet.opprettet
		faktisk.mal shouldBe forventet.mal
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
}
