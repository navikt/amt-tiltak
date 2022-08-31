package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.application.Application
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.integration.mocks.MockAmtEnhetsregisterClient
import no.nav.amt.tiltak.test.integration.mocks.MockMaskinportenHttpClient
import no.nav.amt.tiltak.test.integration.mocks.MockOAuthServer3
import no.nav.amt.tiltak.test.integration.utils.Constants.TEST_JWK
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource


@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
@ActiveProfiles("integration")
@TestConfiguration("application-integration.properties")
abstract class IntegrationTestBase {

	@Autowired
	lateinit var kafkaMessageSender: KafkaMessageSender

	companion object {
		val oAuthServer = MockOAuthServer3()
		val mockMaskinportenHttpClient = MockMaskinportenHttpClient()
		val mockEnhetsregisterClient = MockAmtEnhetsregisterClient()


		@JvmStatic
		@DynamicPropertySource
		fun startEnvironment(registry: DynamicPropertyRegistry) {
			System.setProperty("NAIS_APP_NAME", "amt-tiltak-integration-test")

			oAuthServer.start()
			mockMaskinportenHttpClient.start()
			mockEnhetsregisterClient.start()

			// Sikkerhets ting
			registry.add("no.nav.security.jwt.issuer.azuread.discovery-url", oAuthServer::getDiscoveryUrl)
			registry.add("no.nav.security.jwt.issuer.azuread.accepted-audience") { "test-aud" }

			registry.add("no.nav.security.jwt.issuer.tokenx.discovery-url", oAuthServer::getDiscoveryUrl)
			registry.add("no.nav.security.jwt.issuer.tokenx.accepted-audience") { "test-aud" }

			registry.add("maskinporten.scopes") { "scope1 scope2" }
			registry.add("maskinporten.client-id") { "abc123" }
			registry.add("maskinporten.issuer") { "https://test-issuer" }
			registry.add("maskinporten.token-endpoint") { mockMaskinportenHttpClient.serverUrl() }
			registry.add("maskinporten.client-jwk") { TEST_JWK }

			// Klient ting
			registry.add("amt-enhetsregister.url") { mockEnhetsregisterClient.serverUrl() }

			// Database ting
			val container = SingletonPostgresContainer.getContainer()

			registry.add("spring.datasource.url") { container.jdbcUrl }
			registry.add("spring.datasource.username") { container.username }
			registry.add("spring.datasource.password") { container.password }
			registry.add("spring.datasource.hikari.maximum-pool-size") { 3 }


		}
	}

}
