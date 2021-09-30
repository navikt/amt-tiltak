package no.nav.amt.tiltak.connectors.brreg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OverordnetEnhetDTO(
    @JsonProperty("organisasjonsnummer") val organisasjonsnummer: String,
    @JsonProperty("navn") val navn: String,
    @JsonProperty("forretningsadresse") val forretningsadresse: AdresseDTO
)
