package no.nav.amt.tiltak.connectors.brreg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class VirksomhetDTO(
    @JsonProperty("organisasjonsnummer") val organisasjonsnummer: String,
    @JsonProperty("navn") val navn: String,
    @JsonProperty("postadresse") val postadresse: AdresseDTO,
    @JsonProperty("beliggenhetsadresse") val beliggenhetsadresse: AdresseDTO,
    @JsonProperty("_links") val links: Map<String, LinkDTO>
)
