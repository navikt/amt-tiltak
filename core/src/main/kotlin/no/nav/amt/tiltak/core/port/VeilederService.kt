package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import java.util.*

interface VeilederService {

	fun upsertVeileder(veileder: Veileder): UUID

	fun getVeileder(navIdent: String): Veileder?

}
