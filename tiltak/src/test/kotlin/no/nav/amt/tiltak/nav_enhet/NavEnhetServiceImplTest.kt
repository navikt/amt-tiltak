package no.nav.amt.tiltak.nav_enhet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient

class NavEnhetServiceImplTest : FunSpec({

	val navEnhetRepositoy = mockk<NavEnhetRepository>()
	val veilarbarenaClient = mockk<VeilarbarenaClient>()

	val service = NavEnhetServiceImpl(
		navEnhetRepository = navEnhetRepositoy,
		veilarbarenaClient = veilarbarenaClient
	)

	test("getNavEnhetForBruker - skal håndtere at enhet ikke finnes i database") {
		val fodselsenummer = "213211"
		val enhetId = "1234"

		every {
			veilarbarenaClient.hentBrukerOppfolgingsenhetId(fodselsenummer)
		} returns enhetId

		every {
			navEnhetRepositoy.hentEnhet(enhetId)
		} returns null

		service.getNavEnhetForBruker(fodselsenummer) shouldBe null
	}

})
