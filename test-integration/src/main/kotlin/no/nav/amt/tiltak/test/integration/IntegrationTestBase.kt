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
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.Duration
import javax.sql.DataSource


@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
@ActiveProfiles("integration")
@TestConfiguration("application-integration.properties")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
abstract class IntegrationTestBase {

	@LocalServerPort
	private var port: Int = 0

	val client = OkHttpClient.Builder().callTimeout(Duration.ofMinutes(5)).build()

	@Autowired
	lateinit var kafkaMessageSender: KafkaMessageSender

	@Autowired
	lateinit var dataSource: DataSource

	@Autowired
	lateinit var testDataRepository: TestDataRepository

	companion object {
		val oAuthServer = MockOAuthServer()
		val enhetsregisterServer = MockAmtEnhetsregisterServer()
		val norgHttpServer = MockNorgHttpServer()
		val poaoTilgangServer = MockPoaoTilgangHttpServer()
		val nomHttpServer = MockNomHttpServer()
		val altinnAclHttpServer = MockAmtAltinnAclHttpServer()
		val pdlHttpServer = MockPdlHttpServer()
		val mockMachineToMachineHttpServer = MockMachineToMachineHttpServer()

		@JvmStatic
		@DynamicPropertySource
		fun startEnvironment(registry: DynamicPropertyRegistry) {
			oAuthServer.start()
			registry.add("no.nav.security.jwt.issuer.azuread.discovery-url") { oAuthServer.getDiscoveryUrl("azuread") }
			registry.add("no.nav.security.jwt.issuer.azuread.accepted-audience") { "test-aud" }

			registry.add("no.nav.security.jwt.issuer.tokenx.discovery-url") { oAuthServer.getDiscoveryUrl("tokenx") }
			registry.add("no.nav.security.jwt.issuer.tokenx.accepted-audience") { "test-aud" }

			enhetsregisterServer.start()
			registry.add("amt-enhetsregister.url") { enhetsregisterServer.serverUrl() }

			norgHttpServer.start()
			registry.add("poao-gcp-proxy.url") { norgHttpServer.serverUrl() }

			poaoTilgangServer.start()
			registry.add("poao-tilgang.url") { poaoTilgangServer.serverUrl() }

			nomHttpServer.start()
			registry.add("nom.url") { nomHttpServer.serverUrl() }

			altinnAclHttpServer.start()
			registry.add("amt-altinn-acl.url") { altinnAclHttpServer.serverUrl() }

			pdlHttpServer.start()
			registry.add("pdl.url") { pdlHttpServer.serverUrl() }

			mockMachineToMachineHttpServer.start()
			registry.add("nais.env.azureOpenIdConfigTokenEndpoint") {
				mockMachineToMachineHttpServer.serverUrl() + MockMachineToMachineHttpServer.tokenPath
			}

			// Database ting
			val container = SingletonPostgresContainer.getContainer()

			registry.add("spring.datasource.url") { container.jdbcUrl }
			registry.add("spring.datasource.username") { container.username }
			registry.add("spring.datasource.password") { container.password }
			registry.add("spring.datasource.hikari.maximum-pool-size") { 3 }


		}
	}

	@AfterEach
	fun cleanup() {
		resetMockServers()
	}

	fun resetMockServers() {
		enhetsregisterServer.reset()
		norgHttpServer.reset()
		poaoTilgangServer.reset()
		nomHttpServer.reset()
		altinnAclHttpServer.reset()
		pdlHttpServer.reset()
	}

	fun resetMockServersAndAddDefaultData() {
		resetMockServers()
		norgHttpServer.addDefaultData()
		nomHttpServer.addDefaultData()
		altinnAclHttpServer.addDefaultData()
	}

	fun serverUrl() = "http://localhost:$port"

	fun sendRequest(
		method: String,
		url: String,
		body: RequestBody = emptyRequest(),
		headers: Map<String, String> = emptyMap()
	): Response {
		val requestBody = if (method.equals("GET", true)) null else body
		val reqBuilder = Request.Builder()
			.url("${serverUrl()}$url")
			.method(method, requestBody)

		headers.forEach {
			reqBuilder.addHeader(it.key, it.value)
		}

		return client.newCall(reqBuilder.build()).execute()
	}

	fun sendRequest(request: Request): Response {
		return client.newCall(request).execute()
	}


	fun String.toJsonRequestBody(): RequestBody {
		val mediaTypeJson = "application/json".toMediaType()
		return this.toRequestBody(mediaTypeJson)
	}

	fun emptyRequest(): RequestBody {
		val mediaTypeHtml = "text/html".toMediaType()
		return "".toRequestBody(mediaTypeHtml)
	}

}
