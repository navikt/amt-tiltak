package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import java.util.UUID

data class BrukerInput(
    val id: UUID,
    val personIdent: String,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    val telefonnummer: String,
    val epost: String,
    val ansvarligVeilederId: UUID?,
    val navEnhet: NavEnhetInput?,
	val erSkjermet: Boolean
) {
	fun toModel() = Bruker(
		id = id,
		personIdent = personIdent,
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		telefonnummer = telefonnummer,
		epost = epost,
		ansvarligVeilederId = ansvarligVeilederId,
		navEnhetId = navEnhet?.id,
		erSkjermet = erSkjermet
	)
}

