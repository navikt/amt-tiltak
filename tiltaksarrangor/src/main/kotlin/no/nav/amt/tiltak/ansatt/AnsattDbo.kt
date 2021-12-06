package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksarrangor.TilknyttetArrangor
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

	fun toAnsatt(virksomheter: List<TilknyttetArrangor>): Ansatt {
		return Ansatt(
			id = id,
			personligIdent = personligIdent,
			fornavn = fornavn,
			etternavn = etternavn,
			telefonnummer = telefonnummer,
			epost = epost,
			arrangorer = virksomheter
		)
	}
}
