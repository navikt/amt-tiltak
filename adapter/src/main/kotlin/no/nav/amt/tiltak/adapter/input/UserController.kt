package no.nav.amt.tiltak.adapter.input

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getUsers(): List<UserDTO> {
        return emptyList()
    }

    @PostMapping
    fun lagreTestData(@RequestBody createUserDTO: CreateUserDTO) {

    }

    data class UserDTO(
        val navn: String,
        val age: Int

    )

    data class CreateUserDTO(
        val navn: String,
        val age: Int
    )

}