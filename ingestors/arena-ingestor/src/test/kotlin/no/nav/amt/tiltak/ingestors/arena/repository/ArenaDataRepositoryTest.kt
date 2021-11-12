package no.nav.amt.tiltak.ingestors.arena.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import junit.framework.Assert.*
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.IngestStatus
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime

@Testcontainers
class ArenaDataRepositoryTest {

	// TODO: Kopiert fra LocalPostgresDatabase.kt. Hadde det vært bedre med en modul for test-verktøy?
	private fun createDataSource(container: PostgreSQLContainer<Nothing>): HikariDataSource {
		val config = HikariConfig()
		config.username = container.username
		config.password = container.password
		config.jdbcUrl = container.jdbcUrl
		config.driverClassName = container.driverClassName
		return HikariDataSource(config)
	}

	@Container
	val postgresContainer: PostgreSQLContainer<Nothing> =
		PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate
	lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate

	lateinit var arenaDataRepository: ArenaDataRepository

	@BeforeEach
	fun migrate() {
		val dataSource = createDataSource(postgresContainer)

		// TODO: Kopiert fra LocalPostgresDatabase.kt. Hadde det vært bedre med en modul for test-verktøy?
		val flyway: Flyway = Flyway.configure()
			.dataSource(dataSource)
			.load()

		flyway.clean()
		flyway.migrate()

		jdbcTemplate = JdbcTemplate(dataSource)
		namedJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

		arenaDataRepository = ArenaDataRepository(namedJdbcTemplate)

		jdbcTemplate.update(this::class.java.getResource("/arena-data.sql").readText())
	}

	@Test
	fun `insert() should insert arena data`() {
		val now = LocalDateTime.now()

		arenaDataRepository.upsert(
			ArenaData(
				tableName = "TABLE_NAME",
				operationType = OperationType.INSERT,
				operationPosition = 4L,
				operationTimestamp = now,
				before = null,
				after = "{}"
			)
		)

		val arenaData = jdbcTemplate.query(
			"SELECT * FROM arena_data ORDER BY id DESC",
			arenaDataRepository.rowMapper
		)[0]

		assertEquals("TABLE_NAME", arenaData.tableName)
		assertEquals(OperationType.INSERT, arenaData.operationType)
		assertEquals(4L, arenaData.operationPosition)
		assertEquals(now, arenaData.operationTimestamp)
		assertEquals(IngestStatus.NEW, arenaData.ingestStatus)
		assertNull(arenaData.ingestedTimestamp)
		assertEquals(0, arenaData.ingestAttempts)
		assertNull(arenaData.lastRetry)
		assertNull(arenaData.before)
		assertEquals("{}", arenaData.after)
	}

	@Test
	fun `getUningestedData() should return uningested data`() {
		val uningestedData = arenaDataRepository.getUningestedData("tiltak")

		assertEquals(3, uningestedData.size)

		uningestedData.forEach {
			assertTrue(listOf(IngestStatus.NEW, IngestStatus.RETRY).contains(it.ingestStatus))
		}
	}

	@Test
	fun `getUningestedData() should return paginated uningested data`() {
		val uningestedData = arenaDataRepository.getUningestedData("tiltak", 2, 1)

		assertEquals(1, uningestedData.size)
		assertEquals(4, uningestedData[0].id)
	}

	@Test
	fun `getFailedData() should return failed data`() {
		val failedData = arenaDataRepository.getFailedData("tiltak")

		assertEquals(2, failedData.size)

		failedData.forEach {
			assertTrue(IngestStatus.FAILED.equals(it.ingestStatus))
		}
	}

	@Test
	fun `getFailedData() should return paginated failed data`() {
		val failedData = arenaDataRepository.getFailedData("tiltak", 1, 1)

		assertEquals(1, failedData.size)
		assertEquals(6, failedData[0].id)
	}

	@Test
	fun `markAsIngested() should mark data as ingested and set ingested timestamp`() {

		val uningestedArenaData = arenaDataRepository.getById(2)

		assertEquals(IngestStatus.NEW, uningestedArenaData.ingestStatus)

		arenaDataRepository.upsert(uningestedArenaData.markAsIngested())

		val ingestedArenaData = arenaDataRepository.getById(2)

		assertEquals(IngestStatus.INGESTED, ingestedArenaData.ingestStatus)
		assertTrue(ingestedArenaData.ingestedTimestamp!!.isAfter(LocalDateTime.now().minusMinutes(1)))
	}

	@Test
	fun `markAsFailed() should mark data as failed`() {

		val uningestedArenaData = arenaDataRepository.getById(4)

		assertEquals(IngestStatus.RETRY, uningestedArenaData.ingestStatus)

		arenaDataRepository.upsert(uningestedArenaData.markAsFailed())

		val failedArenaData = arenaDataRepository.getById(4)

		assertEquals(IngestStatus.FAILED, failedArenaData.ingestStatus)
	}

	@Test
	fun `increment() shoud increment retry and update last retry`() {
		val beforeIncrementData = arenaDataRepository.getById(4)

		arenaDataRepository.upsert(beforeIncrementData.retry())

		val afterIncrementData = arenaDataRepository.getById(4)

		assertEquals(beforeIncrementData.ingestAttempts + 1, afterIncrementData.ingestAttempts)
		assertTrue(afterIncrementData.lastRetry!!.isAfter(LocalDateTime.now().minusMinutes(1)))
	}
}
