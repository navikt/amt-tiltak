package no.nav.amt.tiltak.tiltaksleverandor.ansatt.dbo

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.TilknyttetLeverandor
import java.time.LocalDateTime
import java.util.*

data class AnsattDbo(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toAnsatt(virksomheter: List<TilknyttetLeverandor>): Ansatt {
		return Ansatt(
			id = id,
			personligIdent = personligIdent,
			fornavn = fornavn,
			etternavn = etternavn,
			telefonnummer = telefonnummer,
			epost = epost,
			leverandorer = virksomheter
		)
	}
}
