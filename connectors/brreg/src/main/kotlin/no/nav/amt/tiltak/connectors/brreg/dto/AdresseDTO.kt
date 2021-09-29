package no.nav.amt.tiltak.connectors.brreg.dto

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Adresse
import java.util.stream.Collectors

data class AdresseDTO(
    val land: String,
    val landkode: String,
    val postnummer: String,
    val poststed: String,
    val adresse: List<String>,
    val kommune: String,
    val kommunenummer: String

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
