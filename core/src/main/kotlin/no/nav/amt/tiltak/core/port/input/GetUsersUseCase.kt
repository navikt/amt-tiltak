package no.nav.amt.tiltak.core.port.input

import no.nav.amt.tiltak.core.model.User

interface GetUsersUseCase {
    fun getAll(): List<User>
}