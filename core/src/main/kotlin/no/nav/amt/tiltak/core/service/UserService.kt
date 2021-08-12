package no.nav.amt.tiltak.core.service

import no.nav.amt.tiltak.core.model.User
import no.nav.amt.tiltak.core.port.input.CreateUserUseCase
import no.nav.amt.tiltak.core.port.input.GetUsersUseCase
import no.nav.amt.tiltak.core.port.output.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) : CreateUserUseCase, GetUsersUseCase {

    override fun create(command: CreateUserUseCase.CreateUserCommand) {
        userRepository.saveNewUser(UserRepository.SaveNewUserCommand(command.name, command.age))
    }

    override fun getAll(): List<User> {
        return userRepository.getUsers()
    }

}