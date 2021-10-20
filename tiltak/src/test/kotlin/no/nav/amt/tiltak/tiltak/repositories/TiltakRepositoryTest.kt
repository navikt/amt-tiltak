package no.nav.amt.tiltak.tiltak.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@Testcontainers
internal class TiltakRepositoryTest {

    @Container
    val postgresContainer: PostgreSQLContainer<Nothing> =
        PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

    lateinit var jdbcTemplate: JdbcTemplate
    lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate

    lateinit var repository: TiltakRepository

    companion object TestData {
        val TILTAKSLEVERANDOR_1_ID = UUID.fromString("0dc9ccec-fd1e-4c4e-b91a-c23e6d89c18e")
        val TILTAKSLEVERANDOR_ID_NOT_EXIST = UUID.fromString("3cc09a7b-147b-4b0f-b186-e24cb199c8dc")
    }

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

        repository = TiltakRepository(namedJdbcTemplate)

        jdbcTemplate.update(this::class.java.getResource("/tiltak-repository_test-data.sql").readText())
    }

    @Test
    internal fun `insert() should insert tiltak and return object`() {
        val tiltak = Tiltak(
            id = null,
            tiltaksleverandorId = TILTAKSLEVERANDOR_1_ID,
            kode = "AMO",
            navn = "Dette er et testtiltak"
        )

        val savedDbo = repository.insert("1", tiltak)

        assertNotNull(savedDbo)
        assertNotNull(savedDbo.internalId)
        assertNotNull(savedDbo.externalId)

        assertEquals(tiltak.kode, savedDbo.type)
        assertEquals(tiltak.navn, savedDbo.navn)

    }

    @Test
    internal fun `insert on nonexistent tiltaksleverandor should throw`() {
        val tiltak = Tiltak(
            id = null,
            tiltaksleverandorId = TILTAKSLEVERANDOR_ID_NOT_EXIST,
            kode = "AMO",
            navn = "VIL FEILE?!"
        )

        assertThrows<Exception> { repository.insert("1", tiltak) }
    }

    @Test
    internal fun `getByArenaId returns the correct object`() {
        val arenaId = "1"

        val tiltak = Tiltak(
            id = null,
            tiltaksleverandorId = TILTAKSLEVERANDOR_1_ID,
            kode = "AMO",
            navn = "Dette er et testtiltak"
        )

        repository.insert(arenaId, tiltak)

        val savedDbo = repository.getByArenaId(arenaId)

        assertNotNull(savedDbo)
        assertEquals(tiltak.kode, savedDbo?.type)
        assertEquals(tiltak.navn, savedDbo?.navn)

    }

    private fun createDataSource(container: PostgreSQLContainer<Nothing>): HikariDataSource {
        val config = HikariConfig()
        config.username = container.username
        config.password = container.password
        config.jdbcUrl = container.jdbcUrl
        config.driverClassName = container.driverClassName
        return HikariDataSource(config)
    }

}
