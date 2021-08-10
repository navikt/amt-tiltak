package no.nav.amt.tiltak.adapter.output

import no.nav.amt.tiltak.core.model.User
import no.nav.amt.tiltak.core.port.output.UserRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
open class UserRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) : UserRepository {

    override fun saveNewUser(command: UserRepository.SaveNewUserCommand) {
        jdbcTemplate.update("INSERT INTO test_user (name, age) VALUES(?, ?)", command.name, command.age)
    }

    override fun getUsers(): List<User> {
        return jdbcTemplate.query("SELECT * FROM test_user", userMapper)
    }

    // TODO: Dont map directly to domain model
    private val userMapper =
        RowMapper { rs, _ ->
            User(
                name = rs.getString("name"),
                age = rs.getInt("age")
            )
        }
}