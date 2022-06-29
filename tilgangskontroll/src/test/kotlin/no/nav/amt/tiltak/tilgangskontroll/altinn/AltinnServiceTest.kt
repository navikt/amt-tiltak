package no.nav.amt.tiltak.tilgangskontroll.altinn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt_tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt_tiltak.clients.amt_altinn_acl.Rettighet

class AltinnServiceTest : FunSpec( {

	val rettighetId = "1234"

	val amtAltinnAclClient = mockk<AmtAltinnAclClient>()

	val altinnService = AltinnService(rettighetId, amtAltinnAclClient)

	test("skal filtrere ut irrelevante rettigheter") {
		val personligIdent = "12378912"
		val virksomhet = "989076893"

		every {
			amtAltinnAclClient.hentRettigheter(personligIdent, listOf(rettighetId))
		} returns listOf(
			Rettighet(rettighetId, virksomhet),
			Rettighet("34890874", "783290723"),
		)

		val virksomheter = altinnService.hentVirksomehterMedKoordinatorRettighet(personligIdent)

		virksomheter shouldHaveSize 1
		virksomheter.first() shouldBe virksomhet
	}

	test("skal cache respons fra amt-altinn-acl") {
		val personligIdent = "12378912"
		val virksomhet = "989076893"

		every {
			amtAltinnAclClient.hentRettigheter(personligIdent, listOf(rettighetId))
		} returns listOf(
			Rettighet(rettighetId, virksomhet),
			Rettighet("34890874", "783290723"),
		)

		altinnService.hentVirksomehterMedKoordinatorRettighet(personligIdent)
		altinnService.hentVirksomehterMedKoordinatorRettighet(personligIdent)

		verify(exactly = 1) {
			amtAltinnAclClient.hentRettigheter(any(), any())
		}
	}

})
