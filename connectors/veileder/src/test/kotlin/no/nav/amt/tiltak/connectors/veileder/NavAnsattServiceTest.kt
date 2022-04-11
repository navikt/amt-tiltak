package no.nav.amt.tiltak.connectors.veileder

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.axsys.AxsysClient
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.VeilederConnector

class NavAnsattServiceTest: StringSpec ({

	val veilederConnector: VeilederConnector = mockk()
	val axsysClient: AxsysClient = mockk()
	val navAnsattService = NavAnsattServiceImpl(veilederConnector, axsysClient)

	val navn = "Navn Navnesen"
	val navIdent = "AB12345"

	afterEach {
		clearAllMocks()
	}

	"getNavAnsatt - henter veileder - happy path" {
		every { veilederConnector.hentVeileder(navIdent) } returns NavAnsatt(navIdent, navn, null, null)
		val ansatt = navAnsattService.getNavAnsatt(navIdent)

		ansatt.navIdent shouldBe navIdent
		ansatt.navn shouldBe navn
	}

})
