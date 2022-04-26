package no.nav.amt.tiltak.clients.norg

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class NorgClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentNavKontorNavn skal lage riktig request og parse respons") {
		val client = NorgClientImpl(
			url = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
					  "enhetId": 900000042,
					  "navn": "NAV Testheim",
					  "enhetNr": "1234",
					  "antallRessurser": 330,
					  "status": "Aktiv",
					  "orgNivaa": "EN",
					  "type": "LOKAL",
					  "organisasjonsnummer": "12345645",
					  "underEtableringDato": "1970-01-01",
					  "aktiveringsdato": "1970-01-01",
					  "underAvviklingDato": null,
					  "nedleggelsesdato": null,
					  "oppgavebehandler": true,
					  "versjon": 40,
					  "sosialeTjenester": "Fritekst",
					  "kanalstrategi": null,
					  "orgNrTilKommunaltNavKontor": "123456789"
					}
				""".trimIndent()
			)
		)

		val kontorNavn = client.hentNavKontorNavn("1234")

		kontorNavn shouldBe "NAV Testheim"

		val request = server.takeRequest()

		request.path shouldBe "/api/v1/enhet/1234"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

})
