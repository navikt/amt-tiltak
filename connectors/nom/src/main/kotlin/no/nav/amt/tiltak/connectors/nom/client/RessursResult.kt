package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.annotation.JsonProperty

internal data class RessursResult (

	val code: ResultCode,
	val id: String,

	@JsonProperty("ressurs")
	val veileder: VeilederDTO

)
