package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("UnleashControllerNavAnsatt")
@RequestMapping("/api/nav-ansatt/unleash")

open class UnleashController(
    private val unleash: UnleashService
) {

	@GetMapping("/feature")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun getToggles(
		@RequestParam("features") features: List<String>
	): Map<String, Boolean> {
		val toggles = getFeaturetoggles(features)
		return toggles
	}

	fun getFeaturetoggles(features: List<String>): Map<String, Boolean> {
		return features.associateWith { unleash.isEnabled(it) }
	}

}
