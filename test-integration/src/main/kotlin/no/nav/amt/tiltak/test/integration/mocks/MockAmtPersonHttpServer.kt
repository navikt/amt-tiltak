package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_person.NavAnsattRequest
import no.nav.amt.tiltak.clients.amt_person.NavEnhetRequest
import no.nav.amt.tiltak.clients.amt_person.PersonRequest
import no.nav.amt.tiltak.clients.amt_person.dto.AdressebeskyttelseDto
import no.nav.amt.tiltak.clients.amt_person.model.AdressebeskyttelseGradering
import no.nav.amt.tiltak.clients.amt_person.model.NavBruker
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.test.database.data.inputs.BrukerInput
import no.nav.amt.tiltak.test.database.data.inputs.NavEnhetInput
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import no.nav.amt.tiltak.test.integration.utils.getBodyAsString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

class MockAmtPersonHttpServer : MockHttpServer("MockAmtPersonHttpServer") {
	init {
		addResponseHandler("/api/migrer/nav-bruker", MockResponse().setResponseCode(200))
		addResponseHandler("/api/migrer/nav-enhet", MockResponse().setResponseCode(200))
	}

	fun addNavBrukerResponse(navBruker: NavBruker) {
		val request = toJsonString(PersonRequest(navBruker.personident))

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/nav-bruker"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(navBruker))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)
	}

	fun addAnsattResponse(navAnsatt: NavAnsatt) {
		val request = toJsonString(NavAnsattRequest(navAnsatt.navIdent))

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/nav-ansatt"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(navAnsatt))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)
	}

	fun addNavEnhetResponse(navEnhetId: String, navn: String) {
		val request = toJsonString(NavEnhetRequest(navEnhetId))
		val enhet = NavEnhet(UUID.randomUUID(), navEnhetId, navn)

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/nav-enhet"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(enhet))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)
	}

	fun addAdressebeskyttelseResponse(personident: String, gradering: AdressebeskyttelseGradering?) {
		val request = toJsonString(PersonRequest(personident))

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/person/adressebeskyttelse"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(AdressebeskyttelseDto(gradering)))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)

	}
}

fun mockNavBruker(bruker: BrukerInput, navEnhet: NavEnhetInput) = NavBruker(
	personId = bruker.id,
	personident = bruker.personIdent,
	fornavn = bruker.fornavn,
	mellomnavn = bruker.mellomnavn,
	etternavn = bruker.etternavn,
	navVeilederId = bruker.ansvarligVeilederId,
	navEnhet = NavEnhet(navEnhet.id, navEnhet.enhetId, navEnhet.navn),
	telefon = bruker.telefonnummer,
	epost = bruker.epost,
	erSkjermet = bruker.erSkjermet,
)
