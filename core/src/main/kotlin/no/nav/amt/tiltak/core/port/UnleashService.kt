package no.nav.amt.tiltak.core.port

interface UnleashService {
	fun erKometMasterForTiltakstype(tiltakstype: String): Boolean
}
