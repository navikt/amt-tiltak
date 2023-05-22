package no.nav.amt.tiltak.nav_enhet

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.clients.norg.NorgNavEnhet
import no.nav.amt.tiltak.clients.veilarbarena.VeilarbarenaClient
import java.util.*

class NavEnhetServiceImplTest : FunSpec({

	val norgClient = mockk<NorgClient>()
	val navEnhetRepositoy = mockk<NavEnhetRepository>()
	val veilarbarenaClient = mockk<VeilarbarenaClient>()
	val amtPersonClient = mockk<AmtPersonClient>(relaxUnitFun = true)

	val service = NavEnhetServiceImpl(
		norgClient = norgClient,
		navEnhetRepository = navEnhetRepositoy,
		veilarbarenaClient = veilarbarenaClient,
		amtPersonClient = amtPersonClient,
	)

	test("getNavEnhetForBruker - skal inserte enhet hvis den ikke finnes i database") {
		val id = UUID.randomUUID()
		val fodselsenummer = "213211"
		val enhetId = "1234"
		val enhetNavn = "Nav Testheim"

		every {
			veilarbarenaClient.hentBrukerOppfolgingsenhetId(fodselsenummer)
		} returns enhetId

		every {
			navEnhetRepositoy.hentEnhet(enhetId)
		} returns null

		every {
			navEnhetRepositoy.get(any())
		} returns NavEnhetDbo(id, enhetId, enhetNavn)

		every {
			norgClient.hentNavEnhet(enhetId)
		} returns NorgNavEnhet(enhetId, enhetNavn)

		every {
			navEnhetRepositoy.insert(any())
		} returns Unit

		val navEnhet = service.getNavEnhetForBruker(fodselsenummer)

		navEnhet?.id shouldBe id
		navEnhet?.enhetId shouldBe enhetId
		navEnhet?.navn shouldBe enhetNavn
	}

})
