package no.nav.amt.tiltak.clients.amt_person_service

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.amt_person.AmtPersonClientImpl
import no.nav.amt.tiltak.clients.amt_person.NavAnsattRequest
import no.nav.amt.tiltak.clients.amt_person.NavEnhetRequest
import no.nav.amt.tiltak.clients.amt_person.PersonRequest
import no.nav.amt.tiltak.clients.amt_person.dto.NavAnsattDto
import no.nav.amt.tiltak.clients.amt_person.dto.NavBrukerDto
import no.nav.amt.tiltak.clients.amt_person.dto.NavEnhetDto
import no.nav.amt.tiltak.common.json.JsonUtils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AmtPersonClientImplTest {
	lateinit var server: MockWebServer
	lateinit var client: AmtPersonClientImpl

	@BeforeEach
	fun setup() {
		server = MockWebServer()
		client = AmtPersonClientImpl(
			baseUrl = server.url("").toString().removeSuffix("/"),
			tokenProvider = { "TOKEN" },
		)
	}

	@Test
	fun `hentNavBruker - skal lage riktig request og parse respons`() {
		val personident = "0011222244555"

		val navEnhet = NavBrukerDto.NavEnhetDto(
			id = UUID.randomUUID(),
			enhetId = "887766",
			navn = "Nav Oslo",
		)

		val bruker = NavBrukerDto(
				id = UUID.randomUUID(),
				personident = personident,
				fornavn = "Fornavn",
				mellomnavn = "Mellomnavn",
				etternavn = "Etternavn",
				navVeilederId = UUID.randomUUID(),
				navEnhet = navEnhet,
				telefon = "77742777",
				epost = "bruker@nav.no",
				erSkjermet = false,
			)

		server.enqueue(
			MockResponse().setBody(
				JsonUtils.toJsonString(bruker)
			)
		)

		val faktiskBruker = client.hentNavBruker(personident).getOrThrow()

		faktiskBruker.id shouldBe bruker.id
		faktiskBruker.erSkjermet shouldBe bruker.erSkjermet
		faktiskBruker.personident shouldBe bruker.personident
		faktiskBruker.fornavn shouldBe bruker.fornavn
		faktiskBruker.mellomnavn shouldBe bruker.mellomnavn
		faktiskBruker.etternavn shouldBe bruker.etternavn
		faktiskBruker.telefon shouldBe bruker.telefon
		faktiskBruker.epost shouldBe bruker.epost
		faktiskBruker.navVeilederId shouldBe bruker.navVeilederId

		val faktiskNavEnhet = faktiskBruker.navEnhet!!
		faktiskNavEnhet.id shouldBe navEnhet.id
		faktiskNavEnhet.enhetId shouldBe navEnhet.enhetId
		faktiskNavEnhet.navn shouldBe navEnhet.navn


		val request = server.takeRequest()


		request.path shouldBe "/api/nav-bruker"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.body.readUtf8() shouldBe JsonUtils.toJsonString(PersonRequest(personident))

	}

	@Test
	fun `hentNavAnsatt - skal lage riktig request og parse respons`() {
		val navIdent = "Z123456"
		val navAnsattDto = NavAnsattDto(
			id = UUID.randomUUID(),
			navIdent = navIdent,
			navn = "Mr Nav",
			epost = "mr.nav@nav.no",
			telefon = "42",
		)

		server.enqueue(MockResponse().setBody(JsonUtils.toJsonString(navAnsattDto)))

		val faktiskNavAnsatt = client.hentNavAnsatt(navIdent).getOrThrow()

		faktiskNavAnsatt.id shouldBe navAnsattDto.id
		faktiskNavAnsatt.navIdent shouldBe navAnsattDto.navIdent
		faktiskNavAnsatt.navn shouldBe navAnsattDto.navn
		faktiskNavAnsatt.epost shouldBe navAnsattDto.epost
		faktiskNavAnsatt.telefonnummer shouldBe navAnsattDto.telefon

		val request = server.takeRequest()
		request.path shouldBe "/api/nav-ansatt"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.body.readUtf8() shouldBe JsonUtils.toJsonString(NavAnsattRequest(navIdent))
	}

	@Test
	fun `hentNavEnhet - skal lage riktig request og parse respons`() {
		val enhetId = "1234"
		val navEnhetDto = NavEnhetDto(
			id = UUID.randomUUID(),
			enhetId = enhetId,
			navn = "Nav Oslo",
		)

		server.enqueue(MockResponse().setBody(JsonUtils.toJsonString(navEnhetDto)))

		val faktiskNavEnhet = client.hentNavEnhet(enhetId).getOrThrow()

		faktiskNavEnhet.id shouldBe navEnhetDto.id
		faktiskNavEnhet.enhetId shouldBe navEnhetDto.enhetId
		faktiskNavEnhet.navn shouldBe navEnhetDto.navn


		val request = server.takeRequest()
		request.path shouldBe "/api/nav-enhet"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.body.readUtf8() shouldBe JsonUtils.toJsonString(NavEnhetRequest(enhetId))
	}

}
