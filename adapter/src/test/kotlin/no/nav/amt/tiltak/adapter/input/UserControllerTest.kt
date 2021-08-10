package no.nav.amt.tiltak.adapter.input

import no.nav.amt.tiltak.core.model.User
import no.nav.amt.tiltak.core.port.input.CreateUserUseCase
import no.nav.amt.tiltak.core.port.input.GetUsersUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [UserController::class])
open class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var createUserUseCase: CreateUserUseCase

    @MockBean
    private lateinit var getUsersUseCase: GetUsersUseCase

    @Test
    fun getUsers__should_return_users() {
        whenever(getUsersUseCase.getAll()).thenReturn(listOf(User("Test", 42)))

        mockMvc.perform(get("/api/user"))
            .andExpect(content().json(  """[{"name":"Test","age":42}]"""))
            .andExpect(status().`is`(200))
    }

    @Test
    fun createUser__should_create_user() {
        mockMvc.perform(
            post("/api/user")
                .content("""{"name":"Test","age":42}""")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().`is`(201))

        verify(createUserUseCase).create(CreateUserUseCase.CreateUserCommand("Test", 42))
    }

}