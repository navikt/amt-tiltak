package no.nav.amt.tiltak.adapter.output

import no.nav.amt.tiltak.core.port.output.UserRepository
import no.nav.amt.tiltak.util.LocalPostgresDatabase.cleanAndMigrate
import no.nav.amt.tiltak.util.LocalPostgresDatabase.createJdbcTemplate
import no.nav.amt.tiltak.util.LocalPostgresDatabase.createPostgresContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class UserRepositoryImplTest {

    @Container
    private val postgresContainer: PostgreSQLContainer<Nothing> = createPostgresContainer()

    @Test
    fun `skal lagre og hente bruker`() {
        val jdbcTemplate = createJdbcTemplate(postgresContainer)
        val userRepository = UserRepositoryImpl(jdbcTemplate)

        cleanAndMigrate(jdbcTemplate)

        userRepository.saveNewUser(UserRepository.SaveNewUserCommand("Test1", 65))
        userRepository.saveNewUser(UserRepository.SaveNewUserCommand("Test2", 41))

        val allUsers = userRepository.getUsers()

        assertEquals(2, allUsers.size)

        assertEquals("Test1", allUsers[0].name)
        assertEquals(65, allUsers[0].age)

        assertEquals("Test2", allUsers[1].name)
        assertEquals(41, allUsers[1].age)
    }

}