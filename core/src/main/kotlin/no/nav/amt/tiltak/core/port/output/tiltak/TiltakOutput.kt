package no.nav.amt.tiltak.core.port.output.tiltak

import java.util.*

interface TiltakOutput {

	fun instanser(bedriftsnummer: String)

	fun instans(uuid: UUID)

	fun deltagere(uuid: UUID)
}
