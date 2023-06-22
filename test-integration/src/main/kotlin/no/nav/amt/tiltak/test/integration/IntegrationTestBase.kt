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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.Duration
import javax.sql.DataSource


@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
@ActiveProfiles("integration")
@TestConfiguration("application-integration.properties")
abstract class IntegrationTestBase {

	@LocalServerPort
	private var port: Int = 0

	val client = OkHttpClient.Builder()
		.callTimeout(Duration.ofMinutes(5))
		.readTimeout(Duration.ofMinutes(5))
		.build()

	@Autowired
	lateinit var kafkaMessageSender: KafkaMessageSender

	@Autowired
	lateinit var dataSource: DataSource

	@Autowired
	lateinit var testDataRepository: TestDataRepository

	companion object {
		val mockOAuthServer = MockOAuthServer()
		val mockArrangorServer = MockAmtArrangorServer()
		val mockNorgHttpServer = MockNorgHttpServer()
		val mockPoaoTilgangHttpServer = MockPoaoTilgangHttpServer()
		val mockPdlHttpServer = MockPdlHttpServer()
		val mockMachineToMachineHttpServer = MockMachineToMachineHttpServer()
		val mockMulighetsrommetApiServer = MockMulighetsrommetApiServer()
		val mockVeilarboppfolgingHttpServer = MockVeilarboppfolgingHttpServer()
		val mockVeilarbarenaHttpServer = MockVeilarbarenaHttpServer()
		val mockDkifHttpServer = MockDkifHttpServer()
		val mockAmtPersonHttpServer = MockAmtPersonHttpServer()

		@JvmStatic
		@DynamicPropertySource
		fun startEnvironment(registry: DynamicPropertyRegistry) {
			mockOAuthServer.start()
			registry.add("no.nav.security.jwt.issuer.azuread.discovery-url") { mockOAuthServer.getDiscoveryUrl("azuread") }
			registry.add("no.nav.security.jwt.issuer.azuread.accepted-audience") { "test-aud" }

			registry.add("no.nav.security.jwt.issuer.tokenx.discovery-url") { mockOAuthServer.getDiscoveryUrl("tokenx") }
			registry.add("no.nav.security.jwt.issuer.tokenx.accepted-audience") { "test-aud" }

			mockArrangorServer.start()
			registry.add("amt-arrangor.url") { mockArrangorServer.serverUrl() }
			registry.add("amt-arrangor.scope") { "test.arrangor" }

			mockDkifHttpServer.start()
			registry.add("digdir-krr-proxy.url") { mockDkifHttpServer.serverUrl() }
			registry.add("digdir-krr-proxy.scope") { "test.digdir-krr-proxy" }

			mockVeilarboppfolgingHttpServer.start()
			registry.add("veilarboppfolging.url") { mockVeilarboppfolgingHttpServer.serverUrl() }
			registry.add("veilarboppfolging.scope") { "test.veilarboppfolging" }

			mockVeilarbarenaHttpServer.start()
			registry.add("veilarbarena.url") { mockVeilarbarenaHttpServer.serverUrl() }
			registry.add("veilarbarena.scope") { "test.veilarbarena" }

			mockMulighetsrommetApiServer.start()
			registry.add("mulighetsrommet-api.url") { mockMulighetsrommetApiServer.serverUrl() }
			registry.add("mulighetsrommet-api.scope") { "test.mulighetsrommet-api" }

			mockAmtPersonHttpServer.start()
			registry.add("amt-person.url") { mockAmtPersonHttpServer.serverUrl() }
			registry.add("amt-person.scope") { "test.amt-person" }

			mockNorgHttpServer.start()
			registry.add("norg.url") { mockNorgHttpServer.serverUrl() }

			mockPoaoTilgangHttpServer.start()
			registry.add("poao-tilgang.url") { mockPoaoTilgangHttpServer.serverUrl() }
			registry.add("poao-tilgang.scope") { "test.poao-tilgang" }

			mockPdlHttpServer.start()
			registry.add("pdl.url") { mockPdlHttpServer.serverUrl() }
			registry.add("pdl.scope") { "test.pdl" }

			mockMachineToMachineHttpServer.start()
			registry.add("nais.env.azureOpenIdConfigTokenEndpoint") {
				mockMachineToMachineHttpServer.serverUrl() + MockMachineToMachineHttpServer.tokenPath
			}

			registry.add("arrangoransatt.tilgang.updater.number-to-check") { 10 }

			// Database ting
			val container = SingletonPostgresContainer.getContainer()

			registry.add("spring.datasource.url") { container.jdbcUrl }
			registry.add("spring.datasource.username") { container.username }
			registry.add("spring.datasource.password") { container.password }
			registry.add("spring.datasource.hikari.maximum-pool-size") { 3 }
		}
	}

	fun resetMockServers() {
		mockNorgHttpServer.reset()
		mockPoaoTilgangHttpServer.reset()
		mockArrangorServer.reset()
		mockPdlHttpServer.reset()
		mockMulighetsrommetApiServer.reset()
		mockVeilarboppfolgingHttpServer.resetHttpServer()
		mockVeilarbarenaHttpServer.resetHttpServer()
		mockDkifHttpServer.resetHttpServer()
	}

	fun resetMockServersAndAddDefaultData() {
		resetMockServers()
		mockNorgHttpServer.addDefaultData()
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
