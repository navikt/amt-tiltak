package no.nav.amt.tiltak.test.integration.utils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*

class TokenCreator private constructor() {
    private val rsaJWK: RSAKey = RSAKeyGenerator(2048)
		.keyID(UUID.randomUUID().toString())
		.generate()

	private val signer: JWSSigner = RSASSASigner(rsaJWK)

	companion object {
		private var tokenCreator: TokenCreator? = null

		fun instance(): TokenCreator {
			if (tokenCreator == null) {
				tokenCreator = TokenCreator()
			}

			return tokenCreator as TokenCreator
		}
	}

    fun createToken(expireFromNowMs: Int = 60 * 1000): String {
        val claimsSet: JWTClaimsSet = JWTClaimsSet.Builder()
            .subject("test")
            .issuer("https://example.com")
            .jwtID(UUID.randomUUID().toString())
            .expirationTime(Date(Date().time + expireFromNowMs))
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.keyID).build(),
            claimsSet
        )

        signedJWT.sign(signer)

        return signedJWT.serialize()
    }

}
