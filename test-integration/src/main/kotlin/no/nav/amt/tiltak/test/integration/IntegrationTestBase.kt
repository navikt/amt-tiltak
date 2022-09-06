package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.application.Application
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.integration.mocks.*
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageSender
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import javax.sql.DataSource


@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
@ActiveProfiles("integration")
@TestConfiguration("application-integration.properties")
abstract class IntegrationTestBase {

	@LocalServerPort
	private var port: Int = 0

	private val client = OkHttpClient()

	@Autowired
	lateinit var kafkaMessageSender: KafkaMessageSender

	@Autowired
	lateinit var dataSource: DataSource

	@Autowired
	lateinit var db: TestDataRepository

	companion object {
		val oAuthServer = MockOAuthServer3()
		val enhetsregisterClient = MockAmtEnhetsregisterClient()
		val norgHttpClient = MockNorgHttpClient()
		val poaoTilgangClient = MockPoaoTilgangHttpClient()
		val nomHttpClient = MockNomHttpClient()

		@JvmStatic
		@DynamicPropertySource
		fun startEnvironment(registry: DynamicPropertyRegistry) {
			System.setProperty("NAIS_APP_NAME", "amt-tiltak-integration-test")

			oAuthServer.start()
			registry.add("no.nav.security.jwt.issuer.azuread.discovery-url") { oAuthServer.getDiscoveryUrl("azuread") }
			registry.add("no.nav.security.jwt.issuer.azuread.accepted-audience") { "test-aud" }

			registry.add("no.nav.security.jwt.issuer.tokenx.discovery-url") { oAuthServer.getDiscoveryUrl("tokenx") }
			registry.add("no.nav.security.jwt.issuer.tokenx.accepted-audience") { "test-aud" }

			enhetsregisterClient.start()
			registry.add("amt-enhetsregister.url") { enhetsregisterClient.serverUrl() }

			norgHttpClient.start()
			registry.add("poao-gcp-proxy.url") { norgHttpClient.serverUrl() }

			poaoTilgangClient.start()
			registry.add("poao-tilgang.url") { poaoTilgangClient.serverUrl() }

			nomHttpClient.start()
			registry.add("nom.url") { nomHttpClient.serverUrl() }

			// Database ting
			val container = SingletonPostgresContainer.getContainer()

			registry.add("spring.datasource.url") { container.jdbcUrl }
			registry.add("spring.datasource.username") { container.username }
			registry.add("spring.datasource.password") { container.password }
			registry.add("spring.datasource.hikari.maximum-pool-size") { 3 }


		}
	}

	fun resetClients() {
		enhetsregisterClient.reset()
		norgHttpClient.reset()
	}

	fun resetClientsAndAddDefaultData() {
		resetClients()
		norgHttpClient.addDefaultData()
	}

	fun serverUrl() = "http://localhost:$port"

	fun sendRequest(
		method: String,
		url: String,
		body: RequestBody? = null,
		headers: Map<String, String> = emptyMap()
	): Response {
		val reqBuilder = Request.Builder()
			.url("${serverUrl()}$url")
			.method(method, body)

		headers.forEach {
			reqBuilder.addHeader(it.key, it.value)
		}

		return client.newCall(reqBuilder.build()).execute()
	}

	fun String.toJsonRequestBody(): RequestBody {
		val mediaTypeJson = "application/json".toMediaType()
		return this.toRequestBody(mediaTypeJson)
	}

}
