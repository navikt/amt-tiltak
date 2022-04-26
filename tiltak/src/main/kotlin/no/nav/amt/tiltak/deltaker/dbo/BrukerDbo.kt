package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import java.time.LocalDateTime
import java.util.*

data class BrukerDbo(
	val id: UUID,
	val fodselsnummer: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederId: UUID?,
	val navEnhetId: UUID?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {
	fun toBruker(navEnhet: NavEnhet?): Bruker{
		return Bruker(
			id = this.id,
			fornavn = this.fornavn,
			mellomnavn = this.mellomnavn,
			etternavn = this.etternavn,
			fodselsnummer = this.fodselsnummer,
			navEnhet = navEnhet
		)
	}
}
