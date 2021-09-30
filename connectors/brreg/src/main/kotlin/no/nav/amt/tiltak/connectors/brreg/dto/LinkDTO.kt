package no.nav.amt.tiltak.connectors.brreg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LinkDTO(
    @JsonProperty("href") val href: String
)

fun Map<String, LinkDTO>.href(name: String): String {
    if (this[name] == null) {
        throw UnsupportedOperationException("The link $name does not exist")
    }

    return this[name]!!.href
}
