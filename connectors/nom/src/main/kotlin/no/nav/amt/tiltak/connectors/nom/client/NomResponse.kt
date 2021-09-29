package no.nav.amt.tiltak.connectors.nom.client
import com.fasterxml.jackson.annotation.JsonProperty

internal data class NomResponse (
	@JsonProperty("ressurser")
	val veiledere: List<RessursResult>
) {
	fun tilVeiledere() = veiledere.map{it.veileder.toVeileder()}
}

