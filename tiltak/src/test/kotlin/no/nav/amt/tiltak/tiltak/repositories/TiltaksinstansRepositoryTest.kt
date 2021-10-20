package no.nav.amt.tiltak.tiltak.repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Testcontainers
internal class TiltaksinstansRepositoryTest {
    @Container
    val postgresContainer: PostgreSQLContainer<Nothing> =
        PostgreSQLContainer(DockerImageName.parse("postgres:12-alpine"))

    lateinit var jdbcTemplate: JdbcTemplate

    lateinit var repository: TiltaksinstansRepository

    companion object TestData {
        val TILTAK_ID = UUID.fromString("9665b0b6-ea7d-44b0-b9c2-8867c2a6c106")
        val TILTAK_INTERNAL_ID = 1
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
        repository = TiltaksinstansRepository(jdbcTemplate)

        jdbcTemplate.update(this::class.java.getResource("/tiltaksinstans-repository_test-data.sql").readText())
    }

    @Test
    internal fun `insert() should insert tiltaksinstans and return object`() {
        val instans = TiltakInstans(
            id = null,
            tiltakId = TILTAK_ID,
            "TEST Tiltaksinstans",
            status = null,
            oppstartDato = LocalDate.now().plusDays(2),
            sluttDato = LocalDate.now().plusDays(10),
            registrertDato = LocalDateTime.now(),
            fremmoteDato = LocalDateTime.now().plusDays(2).minusHours(2)
        )

        val savedInstans = repository.insert(1, instans)

        assertNotNull(savedInstans)
        assertNotNull(savedInstans.id)
        assertNotNull(savedInstans.externalId)

        assertEquals(TILTAK_INTERNAL_ID, savedInstans.tiltaksId)
        assertEquals(instans.navn, savedInstans.navn)
        assertEquals(instans.status, savedInstans.status)

        assertTrue(instans.oppstartDato!!.isEqualTo(savedInstans.oppstartsdato!!))
        assertTrue(instans.sluttDato!!.isEqualTo(savedInstans.sluttdato!!))
        assertTrue(instans.registrertDato!!.isEqualTo(savedInstans.registrertDato!!))
        assertTrue(instans.fremmoteDato!!.isEqualTo(savedInstans.fremmoteDato!!))
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


/**
 * A helping function as SQL Timestamp and LocalDateTime does not have the same precision
 */
fun LocalDateTime.isEqualTo(other: LocalDateTime): Boolean {
    return this.year == other.year
            && this.month == other.month
            && this.dayOfMonth == other.dayOfMonth
            && this.hour == other.hour
            && this.minute == other.minute
            && this.second == other.second

}

fun LocalDate.isEqualTo(other: LocalDate): Boolean {
    return this.year == other.year
            && this.month == other.month
            && this.dayOfMonth == other.dayOfMonth
}
