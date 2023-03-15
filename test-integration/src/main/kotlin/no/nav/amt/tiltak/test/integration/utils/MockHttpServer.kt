package no.nav.amt.tiltak.test.integration.utils

import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory
import java.util.*

abstract class MockHttpServer(
	private val name: String
) {

	private val server = MockWebServer()

	private val log = LoggerFactory.getLogger(javaClass)

	private var lastRequestCount = 0

	private val responses = mutableMapOf<(request: RecordedRequest) -> Boolean, ResponseHolder>()

	fun start() {
		try {
			server.start()

			server.dispatcher = object : Dispatcher() {
				override fun dispatch(request: RecordedRequest): MockResponse {
					val response = responses.entries.find { it.key.invoke(request) }?.value
						?: throw IllegalStateException(
							"$name: Mock has no handler for $request\n" +
								"	Headers: \n${printHeaders(request.headers)}\n" +
								"	Body: ${request.body.readUtf8()}"
						)

					response.count = response.count + 1

					log.info("Responding [${request.method}: ${request.path}]: $response")
					return response.response.invoke(request)
				}

			}

		} catch (e: IllegalArgumentException) {
			log.info("${javaClass.simpleName} is already started")
		}
	}

	fun addResponseHandler(
		predicate: (req: RecordedRequest) -> Boolean,
		response: (req: RecordedRequest) -> MockResponse
	): UUID {
		val id = UUID.randomUUID()
		responses[predicate] = ResponseHolder(id, response)
		return id
	}

	fun addResponseHandler(predicate: (req: RecordedRequest) -> Boolean, response: MockResponse): UUID {
		return addResponseHandler(predicate) { response }
	}

	fun addResponseHandler(path: String, response: MockResponse): UUID {
		val predicate = { req: RecordedRequest -> req.path == path }
		return addResponseHandler(predicate) { response }
	}

	fun resetHttpServer() {
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

	fun getRequestCount(id: UUID): Int {
		val responseCount = responses.entries.find { it.value.id == id }?.value?.count
			?: throw IllegalStateException("No request with id $id")

		return responseCount
	}

	fun clearResponses() {
		responses.clear()
	}

	private fun printHeaders(headers: Headers): String {
		return headers.map { "		${it.first} : ${it.second}" }
			.joinToString("\n")
	}

	private data class ResponseHolder(
		val id: UUID,
		val response: (request: RecordedRequest) -> MockResponse,
		var count: Int = 0
	)
}
