package no.nav.amt.tiltak.deltaker.service

import io.getunleash.Unleash
import no.nav.amt.tiltak.core.port.UnleashService
import org.springframework.stereotype.Service

@Service
class UnleashServiceImpl(
	private val unleashClient: Unleash
) : UnleashService {
	override fun erKometMasterForTiltakstype(tiltakstype: String): Boolean {
		return unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype == "ARBFORB"
	}
}
