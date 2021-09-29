package no.nav.amt.tiltak.connectors.brreg.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class VirksomhetDTO(
    val organisasjonsnummer: String,
    val navn: String,
    val postadresse: AdresseDTO,
    val beliggenhetsadresse: AdresseDTO,
    @JsonProperty("_links") val links: Map<String, LinkDTO>
)
