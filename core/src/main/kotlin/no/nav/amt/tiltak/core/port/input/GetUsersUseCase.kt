package no.nav.amt.tiltak.core.port.input

import no.nav.amt.tiltak.core.domain.User

interface GetUsersUseCase {
    fun getAll(): List<User>
}
