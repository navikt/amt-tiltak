package no.nav.amt.tiltak.core

import no.nav.amt.tiltak.core.domain.User
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
