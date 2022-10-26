package no.nav.amt.tiltak.test.integration.test_utils

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.integration.mocks.MockOAuthServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*

object ControllerTestUtils {

	fun testTiltaksarrangorAutentisering(requestBuilders: List<Request.Builder>, client: OkHttpClient, oAuthServer: MockOAuthServer) {
		requestBuilders.forEach {
			val withoutTokenResponse = client.newCall(it.build()).execute()
			withoutTokenResponse.code shouldBe 401
			val wrongTokenResponse = client.newCall(it.header(
				name = "authorization",
				value = "Bearer ${oAuthServer.issueAzureAdToken(ident = "", oid = UUID.randomUUID())}")
				.build()
			)
			.execute()
			wrongTokenResponse.code shouldBe 401
		}
	}

	fun testNavAnsattAutentisering(requestBuilders: List<Request.Builder>, client: OkHttpClient, oAuthServer: MockOAuthServer) {
		requestBuilders.forEach {
			val withoutTokenResponse = client.newCall(it.build()).execute()
			withoutTokenResponse.code shouldBe 401
			val wrongTokenResponse = client.newCall(it.header(
				name = "authorization",
				value = "Bearer ${oAuthServer.issueTokenXToken("ident")}")
				.build()
			)
				.execute()
			wrongTokenResponse.code shouldBe 401
		}
	}
}
