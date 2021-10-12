package no.nav.amt.tiltak.tools.azure_ad.utils

import com.nimbusds.jwt.JWT
import org.slf4j.LoggerFactory
import java.text.ParseException

object TokenUtils {

	private val log = LoggerFactory.getLogger(TokenUtils::class.java)

	private const val MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH = 60 * 1000 // 1 minute

	@JvmStatic
	fun tokenNeedsRefresh(accessToken: JWT?): Boolean {
		return accessToken == null || expiresWithin(accessToken, MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH.toLong())
	}

	@JvmStatic
	fun expiresWithin(jwt: JWT, withinMillis: Long): Boolean {
		return try {
			val tokenExpiration = jwt.jwtClaimsSet.expirationTime ?: return true

			// Token should have an expiration, but if it does not, then the safest option is to assume it to be expired
			val expirationTime = tokenExpiration.time - withinMillis
			System.currentTimeMillis() > expirationTime
		} catch (e: ParseException) {
			log.error("Unable to parse JWT token", e)
			true
		}
	}

}
