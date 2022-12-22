package no.nav.amt.tiltak.deltaker.dbo

import java.util.*

data class BrukerUpsertDbo(
    val personIdent: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val telefonnummer: String?,
    val epost: String?,
    val ansvarligVeilederId: UUID?,
    val navEnhetId: UUID?,
    val erSkjermet: Boolean
)
