package no.nav.amt.tiltak.clients.pdl

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.pdl.PdlClientImplTestData.errorPrefix
import no.nav.amt.tiltak.clients.pdl.PdlClientImplTestData.flereFeilRespons
import no.nav.amt.tiltak.clients.pdl.PdlClientImplTestData.gyldigRespons
import no.nav.amt.tiltak.clients.pdl.PdlClientImplTestData.minimalFeilRespons
import no.nav.amt.tiltak.clients.pdl.PdlClientImplTestData.nullError
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.assertThrows

class PdlClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentBruker - gyldig respons - skal lage riktig request og parse pdl bruker") {
		val connector = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(MockResponse().setBody(gyldigRespons))

		val pdlBruker = connector.hentBruker("FNR")

		pdlBruker.fornavn shouldBe "Tester"
		pdlBruker.mellomnavn shouldBe "Test"
		pdlBruker.etternavn shouldBe "Testersen"
		pdlBruker.telefonnummer shouldBe "+47 12345678"

		val request = server.takeRequest()

		request.path shouldBe "/graphql"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.getHeader("Tema") shouldBe "GEN"

		val expectedJson =
			"""
				{
					"query": "${PdlQueries.HentBruker.query.replace("\n", "\\n").replace("\t", "\\t")}",
					"variables": { "ident": "FNR" }
				}
			""".trimIndent()

		request.body.readUtf8() shouldEqualJson expectedJson
	}

	test("hentBruker - data mangler - skal kaste exception") {
		val client = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"errors": [{"message": "Noe gikk galt"}],
						"data": null
					}
				""".trimIndent()
			)
		)

		val exception = assertThrows<RuntimeException> {
			client.hentBruker("FNR")
		}

		exception.message shouldBe "$errorPrefix$nullError- Noe gikk galt (code: null details: null)\n"

		val request = server.takeRequest()

		request.path shouldBe "/graphql"
		request.method shouldBe "POST"
	}

	test("hentGjeldendePersonligIdent skal lage riktig request og parse response") {
		val client = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"errors": null,
						"data": {
							"hentIdenter": {
							  "identer": [
								{
								  "ident": "12345678900"
								}
							  ]
							}
						  }
					}
				""".trimIndent()
			)
		)

		val gjeldendeIdent = client.hentGjeldendePersonligIdent("112233445566")

		gjeldendeIdent shouldBe "12345678900"

		val request = server.takeRequest()

		request.path shouldBe "/graphql"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.getHeader("Tema") shouldBe "GEN"

		val expectedJson =
			"""
				{
					"query": "${PdlQueries.HentGjeldendeIdent.query.replace("\n", "\\n").replace("\t", "\\t")}",
					"variables": { "ident": "112233445566" }
				}
			""".trimIndent()

		request.body.readUtf8() shouldEqualJson expectedJson
	}

	test("hentGjeldendePersonligIdent skal kaste exception hvis data mangler") {
		val client = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"errors": [{"message": "error :("}],
						"data": null
					}
				""".trimIndent()
			)
		)

		val exception = assertThrows<RuntimeException> {
			client.hentGjeldendePersonligIdent("112233445566")
		}

		exception.message shouldBe "PDL respons inneholder ikke data"

		val request = server.takeRequest()

		request.path shouldBe "/graphql"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.getHeader("Tema") shouldBe "GEN"
	}

	test("hentBruker - Detaljert respons - skal kaste exception med noe detaljert informasjon") {
		val client = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(MockResponse().setBody(minimalFeilRespons))

		val exception = assertThrows<RuntimeException> {
			client.hentBruker("FNR")
		}

		exception.message shouldBe errorPrefix + nullError +
			"- Ikke tilgang til å se person (code: unauthorized details: PdlErrorDetails(type=abac-deny, cause=cause-0001-manglerrolle, policy=adressebeskyttelse_strengt_fortrolig_adresse))\n"
	}

	test("hentBruker - Flere feil i respons - skal kaste exception med noe detaljert informasjon") {
		val client = PdlClientImpl(
			serverUrl,
			{ "TOKEN" },
		)

		server.enqueue(MockResponse().setBody(flereFeilRespons))

		val exception = assertThrows<RuntimeException> {
			client.hentBruker("FNR")
		}

		exception.message shouldBe errorPrefix + nullError +
			"- Ikke tilgang til å se person (code: unauthorized details: PdlErrorDetails(type=abac-deny, cause=cause-0001-manglerrolle, policy=adressebeskyttelse_strengt_fortrolig_adresse))\n" +
			"- Test (code: unauthorized details: PdlErrorDetails(type=abac-deny, cause=cause-0001-manglerrolle, policy=adressebeskyttelse_strengt_fortrolig_adresse))\n"
	}

})
