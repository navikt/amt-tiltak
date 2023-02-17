package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.IdentType
import java.time.LocalDateTime
import java.util.*

data class BrukerDbo(
	val id: UUID,
	val personIdent: String,
	val personIdentType: IdentType?, //Trenger ikke være nullable i fremtiden
	val historiskeIdenter: List<String>,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val epost: String?,
	val ansvarligVeilederId: UUID?,
	val navEnhetId: UUID?,
	val erSkjermet: Boolean,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun upsert(): BrukerUpsertDbo {
		return BrukerUpsertDbo(
			personIdent,
			fornavn,
			mellomnavn,
			etternavn,
			telefonnummer,
			epost,
			ansvarligVeilederId,
			navEnhetId,
			erSkjermet
		)
	}


}

