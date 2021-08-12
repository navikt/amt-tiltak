package no.nav.amt.tiltak.core.port.input

interface CreateUserUseCase {
    fun create(command: CreateUserCommand)

    data class CreateUserCommand(
        val name: String,
        val age: Int
    )
}