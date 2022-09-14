package no.nav.amt.tiltak.test.integration.mocks

import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory

abstract class MockHttpClient(
	private val name: String
) {

	private val server = MockWebServer()

	private val log = LoggerFactory.getLogger(javaClass)

	private var lastRequestCount = 0

	private val responses = mutableMapOf<(request: RecordedRequest) -> Boolean, MockResponse>()

	fun start() {
		try {
			server.start()

			server.dispatcher = object : Dispatcher() {
				override fun dispatch(request: RecordedRequest): MockResponse {
					val response = responses.entries.find { it.key.invoke(request) }?.value
						?: throw IllegalStateException(
							"$name has no handler for $request\n" +
								"	Headers: ${printHeaders(request.headers)}\n" +
								"	Body: ${request.body.readUtf8()}	"
						)

					log.info("Responding [${request.path}]: $response")
					return response
				}

			}

		} catch (e: IllegalArgumentException) {
			log.info("${javaClass.simpleName} is already started")
		}
	}

	protected fun addResponseHandler(predicate: (req: RecordedRequest) -> Boolean, response: MockResponse) {
		responses[predicate] = response
	}

	protected fun addResponseHandler(path: String, response: MockResponse) {
		val predicate = { req: RecordedRequest -> req.path == path }
		responses[predicate] = response
	}

	protected fun resetHttpServer() {
		responses.clear()
		lastRequestCount = server.requestCount
	}

	fun serverUrl(): String {
		return server.url("").toString().removeSuffix("/")
	}

	fun latestRequest(): RecordedRequest {
		return server.takeRequest()
	}

	fun requestCount(): Int {
		return server.requestCount - lastRequestCount
	}

	fun shutdown() {
		server.shutdown()
	}

	private fun printHeaders(headers: Headers): String {
		return headers.map { "		${it.first} : ${it.second}" }
			.joinToString("\n")
	}

}
