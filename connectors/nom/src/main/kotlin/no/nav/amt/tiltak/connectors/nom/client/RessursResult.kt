package no.nav.amt.tiltak.connectors.nom.client

internal data class RessursResult (
	private val code: ResultCode,
	private val ressurs: VeilederDTO?
) {
	fun isOk(): Boolean = code == ResultCode.OK

	fun toVeileder() = ressurs?.toVeileder()
}
