package no.nav.amt.tiltak.deltaker.service

import io.getunleash.Unleash
import no.nav.amt.tiltak.core.port.UnleashService
import org.springframework.stereotype.Service

@Service
class UnleashServiceImpl(
	private val unleashClient: Unleash
) : UnleashService {
	private val tiltakstyperKometAlltidErMasterFor = listOf(
		"ARBFORB",
		"INDOPPFAG",
		"AVKLARAG",
		"ARBRRHDAG",
	)

	// her kan vi legge inn de neste tiltakstypene vi skal ta over
	private val tiltakstyperKometKanskjeErMasterFor = listOf(
		"VASV",
		"DIGIOPPARB",
	)

	override fun erKometMasterForTiltakstype(tiltakstype: String): Boolean {
		return tiltakstype in tiltakstyperKometAlltidErMasterFor ||
			(unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype in tiltakstyperKometKanskjeErMasterFor)
	}
}
