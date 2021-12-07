package no.nav.amt.tiltak.core.domain.arrangor

data class Adresse(
    val land: String,
    val landKode: String,
    val postnummer: String,
    val poststed: String,
    val adresse: String,
    val kommune: String,
    val kommunenummer: String
)
