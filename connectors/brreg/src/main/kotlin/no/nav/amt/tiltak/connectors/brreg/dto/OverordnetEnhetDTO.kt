package no.nav.amt.tiltak.connectors.brreg.dto

data class OverordnetEnhetDTO(
    val organisasjonsnummer: String,
    val navn: String,
    val forretningsadresse: AdresseDTO
)
