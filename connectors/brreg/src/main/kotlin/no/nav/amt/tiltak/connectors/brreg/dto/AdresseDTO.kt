package no.nav.amt.tiltak.connectors.brreg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Adresse
import java.util.stream.Collectors

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdresseDTO(
    @JsonProperty("land") val land: String,
    @JsonProperty("landkode") val landkode: String,
    @JsonProperty("postnummer") val postnummer: String,
    @JsonProperty("poststed") val poststed: String,
    @JsonProperty("adresse") val adresse: List<String>,
    @JsonProperty("kommune") val kommune: String,
    @JsonProperty("kommunenummer") val kommunenummer: String

)

fun AdresseDTO.toModel(): Adresse {
    return Adresse(
        land = this.land,
        landKode = this.landkode,
        postnummer = this.postnummer,
        poststed = this.poststed,
        adresse = this.adresse.stream().collect(Collectors.joining(", ")),
        kommune = this.kommune,
        kommunenummer = this.kommunenummer
    )
}
