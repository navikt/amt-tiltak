package no.nav.amt.tiltak.tools.token_provider

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import no.nav.amt.tiltak.tools.token_provider.utils.TokenUtils.tokenNeedsRefresh
import java.util.concurrent.ConcurrentHashMap

class CachedScopedTokenProvider(private val scopedTokenProvider: ScopedTokenProvider) : ScopedTokenProvider {

	private val cachedTokens: ConcurrentHashMap<String, JWT> = ConcurrentHashMap<String, JWT>()

	override fun getToken(scope: String): String {
		var token: JWT? = cachedTokens[scope]

		if (tokenNeedsRefresh(token)) {
			token = JWTParser.parse(scopedTokenProvider.getToken(scope))
			cachedTokens[scope] = token
		}

		return token!!.parsedString
	}

}
