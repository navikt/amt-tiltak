package no.nav.amt.tiltak.adapter.output

import no.nav.amt.tiltak.core.model.User
import no.nav.amt.tiltak.core.port.output.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl : UserRepository {

    private val users: MutableList<User> = ArrayList()

    override fun saveNewUser(command: UserRepository.SaveNewUserCommand) {
        users.add(User(command.name, command.age))
    }

    override fun getUsers(): List<User> {
        return users
    }

}