package no.nav.amt.tiltak.adapter.input

import no.nav.amt.tiltak.core.port.input.CreateUserUseCase
import no.nav.amt.tiltak.core.port.input.GetUsersUseCase
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val createUserUseCase: CreateUserUseCase,
    private val getUsersUseCase: GetUsersUseCase
    ) {

    @GetMapping
    fun getUsers(): List<UserDTO> {
        return getUsersUseCase.getAll().map { UserDTO(it.name, it.age) }
    }

    @PostMapping
    fun lagreTestData(@RequestBody createUserDTO: CreateUserDTO) {
        createUserUseCase.create(CreateUserUseCase.CreateUserCommand(createUserDTO.name, createUserDTO.age))
    }

    data class UserDTO(
        val name: String,
        val age: Int
    )

    data class CreateUserDTO(
        val name: String,
        val age: Int
    )

}