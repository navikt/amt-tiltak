package no.nav.amt.tiltak.core.port.output

import no.nav.amt.tiltak.core.model.User

interface UserRepository {
    fun saveNewUser(command: SaveNewUserCommand)

    fun getUsers(): List<User>

    data class SaveNewUserCommand(
        val name: String,
        val age: Int
    )
}