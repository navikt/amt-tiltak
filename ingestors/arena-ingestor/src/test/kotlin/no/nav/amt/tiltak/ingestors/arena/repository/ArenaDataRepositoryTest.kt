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
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
	val postgresContainer: PostgreSQLContainer<Nothing> = PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

	lateinit var jdbcTemplate: JdbcTemplate

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
		arenaDataRepository = ArenaDataRepository(jdbcTemplate)

		jdbcTemplate.update(this::class.java.getResource("/arena-data.sql").readText())
	}

	@Test
	fun `insert() should insert arena data`() {
		val now = LocalDateTime.now()

		arenaDataRepository.insert(CreateArenaData(
			tableName = "TABLE_NAME",
			operationType = OperationType.INSERT,
			operationPosition = 4L,
			operationTimestamp = now,
			before = null,
			after = "{}"
		))

		val arenaData = jdbcTemplate.query(
			"SELECT * FROM ${ArenaDataRepository.TABLE_NAME} ORDER BY ${ArenaDataRepository.FIELD_ID} DESC",
			arenaDataRepository.rowMapper)[0]

		assertEquals("TABLE_NAME", arenaData.tableName)
		assertEquals(OperationType.INSERT, arenaData.operationType)
		assertEquals(4L, arenaData.operationPosition)
		assertEquals(now.truncatedTo(ChronoUnit.MILLIS), arenaData.operationTimestamp.truncatedTo(ChronoUnit.MILLIS))
		assertEquals(IngestStatus.NEW, arenaData.ingestStatus)
		assertNull(arenaData.ingestedTimestamp)
		assertEquals(0, arenaData.ingestAttempts)
		assertNull(arenaData.lastRetry)
		assertNull(arenaData.before)
		assertEquals("{}", arenaData.after)
	}

	@Test
	fun `getUningestedData() should return uningested data`() {
		val uningestedData = arenaDataRepository.getUningestedData()

		assertEquals(3, uningestedData.size)

		uningestedData.forEach {
			assertTrue(listOf(IngestStatus.NEW, IngestStatus.RETRY).contains(it.ingestStatus))
		}
	}

	@Test
	fun `getUningestedData() should return paginated uningested data`() {
		val uningestedData = arenaDataRepository.getUningestedData(2,1)

		assertEquals(1, uningestedData.size)
		assertEquals(4, uningestedData[0].id)
	}

	@Test
	fun `getFailedData() should return failed data`() {
		val failedData = arenaDataRepository.getFailedData()

		assertEquals(2, failedData.size)

		failedData.forEach {
			assertTrue(IngestStatus.FAILED.equals(it.ingestStatus))
		}
	}

	@Test
	fun `getFailedData() should return paginated failed data`() {
		val failedData = arenaDataRepository.getFailedData(1, 1)

		assertEquals(1, failedData.size)
		assertEquals(6, failedData[0].id)
	}

	@Test
	fun `markAsIngested() should mark data as ingested and set ingested timestamp`() {

		val uningestedArenaData = getById(2)

		assertEquals(IngestStatus.NEW, uningestedArenaData.ingestStatus)

		arenaDataRepository.markAsIngested(uningestedArenaData.id)

		val ingestedArenaData = getById(2)

		assertEquals(IngestStatus.INGESTED, ingestedArenaData.ingestStatus)
		assertTrue(ingestedArenaData.ingestedTimestamp!!.isAfter(LocalDateTime.now().minusMinutes(1)))
	}

	@Test
	fun `markAsFailed() should mark data as failed`() {

		val uningestedArenaData = getById(4)

		assertEquals(IngestStatus.RETRY, uningestedArenaData.ingestStatus)

		arenaDataRepository.markAsFailed(uningestedArenaData.id)

		val failedArenaData = getById(4)

		assertEquals(IngestStatus.FAILED, failedArenaData.ingestStatus)
	}

	@Test
	fun `increment() shoud increment retry and update last retry`() {
		val beforeIncrementData = getById(4)

		arenaDataRepository.incrementRetry(beforeIncrementData.id, beforeIncrementData.ingestAttempts)

		val afterIncrementData = getById(4)

		assertEquals(beforeIncrementData.ingestAttempts + 1, afterIncrementData.ingestAttempts)
		assertTrue(afterIncrementData.lastRetry!!.isAfter(LocalDateTime.now().minusMinutes(1)))
	}

	private fun getById(id: Int): ArenaData {
		return jdbcTemplate.query(
			"SELECT * FROM ${ArenaDataRepository.TABLE_NAME} WHERE ${ArenaDataRepository.FIELD_ID} = $id",
			arenaDataRepository.rowMapper)[0]
	}

}
