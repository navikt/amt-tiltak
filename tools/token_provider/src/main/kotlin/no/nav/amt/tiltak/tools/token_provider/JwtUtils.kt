package no.nav.amt.tiltak.tools.token_provider.azure_ad

import com.nimbusds.jwt.JWT
import org.slf4j.LoggerFactory
import java.text.ParseException

object JwtUtils {

	private val log = LoggerFactory.getLogger(JwtUtils::class.java)

	private const val MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH: Long = 60 * 1000L

	fun JWT.needsRefresh(withinMillis: Long = MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH): Boolean {
		return try {
			val tokenExpiration = jwtClaimsSet.expirationTime ?: return true

			// Token should have an expiration, but if it does not, then the safest option is to assume it to be expired
			val expirationTime = tokenExpiration.time - withinMillis
			System.currentTimeMillis() > expirationTime
		} catch (e: ParseException) {
			log.error("Unable to parse JWT token", e)
			true
		}
	}

}
