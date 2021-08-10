package no.nav.amt.tiltak.application

import no.nav.amt.tiltak.application.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun example_test() {
        val user = User("Douglas", 42)

        assertEquals("Douglas", user.name)
        assertEquals(42, user.age)
    }

}