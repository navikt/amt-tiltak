package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import java.time.LocalDateTime
import java.util.*

data class AnsattDbo(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toAnsatt(virksomheter: List<TilknyttetArrangor>): Ansatt {
		return Ansatt(
			id = id,
			personligIdent = personligIdent,
			fornavn = fornavn,
			mellomnavn = mellomnavn,
			etternavn = etternavn,
			arrangorer = virksomheter
		)
	}
}
