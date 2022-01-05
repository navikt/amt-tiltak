package no.nav.amt.tiltak.ingestors.arena.processors

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena.ArenaDataProcessor
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.util.*

class ArenaDataProcessorIntegrationTest {
	private val dataSource = SingletonPostgresContainer.getDataSource()
	private lateinit var jdbcTemplate: JdbcTemplate
	private lateinit var arenaDataRepository: ArenaDataRepository

	private lateinit var tiltakService: TiltakService
	private lateinit var tiltakProcessor: TiltakProcessor

	private lateinit var gjennomforingProcessor: GjennomforingProcessor
	private lateinit var deltakerProcessor: DeltakerProcessor

	private lateinit var arenaDataProcessor: ArenaDataProcessor
	private var tiltakTabellnavn = "SIAMO.TILTAK"

	val tiltak = Tiltak(UUID.randomUUID(), "INDOPPFAG", "Oppfølging")
	val insertOppfTiltak = { tiltakService.upsertTiltak(tiltak.kode, tiltak.navn, tiltak.kode) }

	@BeforeEach
	fun beforeAll() {
		jdbcTemplate = JdbcTemplate(dataSource)
		arenaDataRepository = ArenaDataRepository(NamedParameterJdbcTemplate(jdbcTemplate))
		tiltakService = mock(TiltakService::class.java)
		tiltakProcessor = TiltakProcessor(arenaDataRepository, tiltakService)
		gjennomforingProcessor = mock(GjennomforingProcessor::class.java)
		deltakerProcessor = mock(DeltakerProcessor::class.java)
		arenaDataProcessor = ArenaDataProcessor(arenaDataRepository, tiltakProcessor, gjennomforingProcessor, deltakerProcessor)

	}

	@Test
	fun `processUningestedMessages() - Skal ingeste oppfølgingstiltak`() {

		val arenaData = ArenaData(
			id = 1,
			tableName = tiltakTabellnavn,
			operationType = OperationType.INSERT,
			operationPosition = 1L,
			operationTimestamp = LocalDateTime.now(),
			after = after
		)
		arenaDataRepository.upsert(arenaData)
		arenaDataRepository.getUningestedData(tiltakTabellnavn) shouldHaveSize 1

		arenaDataProcessor.processUningestedMessages()

		arenaDataRepository.getUningestedData(tiltakTabellnavn) shouldHaveSize 0
		verify(tiltakService, times(1)).upsertTiltak(tiltak.kode, tiltak.navn, tiltak.kode)

	}

	@Test
	fun `processUningestedMessages() - Skal forsøke å ingeste 11 ganger`() {

		val arenaData = ArenaData(
			id = 1,
			tableName = tiltakTabellnavn,
			operationType = OperationType.INSERT,
			operationPosition = 1L,
			operationTimestamp = LocalDateTime.now(),
			after = after
		)
		arenaDataRepository.upsert(arenaData)
		arenaDataRepository.getUningestedData(tiltakTabellnavn) shouldHaveSize 1

		`when`(insertOppfTiltak())
			.thenThrow(DataIntegrityViolationException::class.java)

		arenaDataProcessor.processUningestedMessages()
		repeat(10) { arenaDataProcessor.processUningestedMessages() }

		val failedData = arenaDataRepository.getFailedData(tiltakTabellnavn)

		failedData shouldHaveSize 1
		failedData[0].ingestStatus shouldBe IngestStatus.FAILED
		failedData[0].ingestAttempts shouldBe 11
	}

	@Test
	fun `processFailedMessages() - tiltak har feilet tidligere - skal ingestes`() {
		val arenaData = ArenaData(
			id = 1,
			tableName = tiltakTabellnavn,
			operationType = OperationType.INSERT,
			operationPosition = 1L,
			operationTimestamp = LocalDateTime.now(),
			after = after,
			ingestAttempts = 10,
			ingestStatus = IngestStatus.FAILED
		)
		arenaDataRepository.upsert(arenaData)

		`when`(insertOppfTiltak())
			.thenReturn(tiltak)

		arenaDataProcessor.processFailedMessages()
		val failedData = arenaDataRepository.getFailedData(tiltakTabellnavn)

		failedData shouldHaveSize 0

	}

	val after = """
			{
			  "TILTAKSNAVN": "Oppfølging",
			  "TILTAKSGRUPPEKODE": "OPPFOLG",
			  "REG_DATO": "2009-01-01 07:26:30",
			  "REG_USER": "SIAMO",
			  "MOD_DATO": "2021-04-14 09:44:13",
			  "MOD_USER": "SIAMO",
			  "TILTAKSKODE": "INDOPPFAG",
			  "DATO_FRA": "2009-01-01 00:00:00",
			  "DATO_TIL": "2099-01-01 00:00:00",
			  "AVSNITT_ID_GENERELT": null,
			  "STATUS_BASISYTELSE": "J",
			  "ADMINISTRASJONKODE": "INST",
			  "STATUS_KOPI_TILSAGN": "N",
			  "ARKIVNOKKEL": "829",
			  "STATUS_ANSKAFFELSE": "J",
			  "MAKS_ANT_PLASSER": null,
			  "MAKS_ANT_SOKERE": null,
			  "STATUS_FAST_ANT_PLASSER": "N",
			  "STATUS_SJEKK_ANT_DELTAKERE": "N",
			  "STATUS_KALKULATOR": "N",
			  "RAMMEAVTALE": "KAN",
			  "OPPLAERINGSGRUPPE": null,
			  "HANDLINGSPLAN": "SOK",
			  "STATUS_SLUTTDATO": "J",
			  "MAKS_PERIODE": null,
			  "STATUS_MELDEPLIKT": "N",
			  "STATUS_VEDTAK": "J",
			  "STATUS_IA_AVTALE": "N",
			  "STATUS_TILLEGGSSTONADER": "J",
			  "STATUS_UTDANNING": "N",
			  "AUTOMATISK_TILSAGNSBREV": "N",
			  "STATUS_BEGRUNNELSE_INNSOKT": "N",
			  "STATUS_HENVISNING_BREV": "J",
			  "STATUS_KOPIBREV": "N"
			}""".trimIndent()

}
