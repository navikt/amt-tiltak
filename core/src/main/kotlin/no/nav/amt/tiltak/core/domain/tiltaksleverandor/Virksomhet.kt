package no.nav.amt.tiltak.core.domain.tiltaksleverandor

import java.util.*

data class Virksomhet(
    val id: UUID?,
    val organisasjonsnummer: String,
    val organisasjonsnavn: String,
    val organisasjonensAdresse: Adresse,
    val virksomhetsnummer: String,
    val virksomhetsnavn: String,
    val postadresse: Adresse,
    val beliggenhetsadresse: Adresse
)
