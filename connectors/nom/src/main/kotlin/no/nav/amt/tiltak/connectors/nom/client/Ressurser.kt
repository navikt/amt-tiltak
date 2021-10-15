package no.nav.amt.tiltak.connectors.nom.client

internal data class Ressurser(
	private val ressurser: List<RessursResult>
) {
	internal fun toVeiledere() = ressurser
		.filter { it.isOk() }
		.map { it.toVeileder() }
}
