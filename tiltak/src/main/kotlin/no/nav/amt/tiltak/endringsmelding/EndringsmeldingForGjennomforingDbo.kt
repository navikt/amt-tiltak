package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingForGjennomforingDbo (
	val id: UUID,
	val deltakerId: UUID,

	val brukerId: UUID,
	val brukerFornavn: String,
	val brukerEtternavn: String,
	val brukerMellomnavn: String?,
	val brukerFnr: String,

	val navEnhetId: UUID?,
	val navEnhetNorgId: String?,
	val navEnhetNavn: String?,

	val opprettetAvId: UUID,
	val opprettetAvPersonligIdent: String,
	val opprettetAvFornavn: String,
	val opprettetAvMellomnavn: String?,
	val opprettetAvEtternavn: String,

	val startDato: LocalDate?,
	val godkjentAvNavAnsatt: UUID?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {
	fun toEndringsmelding() = Endringsmelding (
		id = id,
		bruker = Bruker (
			id = brukerId,
			fornavn = brukerFornavn,
			mellomnavn = brukerMellomnavn,
			etternavn = brukerEtternavn,
			fodselsnummer = brukerFnr,
			navEnhet = if (navEnhetId == null) null else NavEnhet(
				id = navEnhetId,
				enhetId = navEnhetNorgId!!,
				navn = navEnhetNavn!!
			)
		),
		startDato = startDato,
		aktiv = aktiv,
		godkjent = godkjentAvNavAnsatt != null,
		arkivert = !aktiv || godkjentAvNavAnsatt != null,
		opprettetAvArrangorAnsatt = Ansatt(
			id = opprettetAvId,
			personligIdent = opprettetAvPersonligIdent,
			fornavn = opprettetAvFornavn,
			mellomnavn = opprettetAvMellomnavn,
			etternavn = opprettetAvEtternavn,
			arrangorer = emptyList()
		),
		opprettetDato = createdAt
	)
}
