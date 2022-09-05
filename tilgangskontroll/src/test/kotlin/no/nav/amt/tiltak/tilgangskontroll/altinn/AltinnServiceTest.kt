package no.nav.amt.tiltak.tilgangskontroll.altinn

import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt.tiltak.clients.amt_altinn_acl.TiltaksarrangorAnsattRoller

class AltinnServiceTest : FunSpec( {

	val amtAltinnAclClient = mockk<AmtAltinnAclClient>()

	val altinnService = AltinnService(amtAltinnAclClient)

	test("skal cache respons fra amt-altinn-acl") {
		val personligIdent = "12378912"
		val virksomhet = "989076893"

		every {
			amtAltinnAclClient.hentTiltaksarrangorRoller(personligIdent)
		} returns listOf(
			TiltaksarrangorAnsattRoller(virksomhet, emptyList()),
		)

		altinnService.hentVirksomheterMedKoordinatorRettighet(personligIdent)
		altinnService.hentVirksomheterMedKoordinatorRettighet(personligIdent)

		verify(exactly = 1) {
			amtAltinnAclClient.hentTiltaksarrangorRoller(any())
		}
	}

})
