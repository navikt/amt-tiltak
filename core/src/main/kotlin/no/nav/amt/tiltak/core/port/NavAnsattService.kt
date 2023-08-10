package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import java.util.UUID

interface NavAnsattService {

	fun getNavAnsatt(navAnsattId: UUID): NavAnsatt
	fun getNavAnsatt(navIdent: String): NavAnsatt
	fun upsert(ansatt: NavAnsatt)
	fun opprettNavAnsattHvisIkkeFinnes(navAnsattId: UUID)
}
