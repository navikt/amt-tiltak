package no.nav.amt.tiltak.connectors.veileder

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.axsys.AxsysClient
import no.nav.amt.tiltak.clients.axsys.Enhet
import no.nav.amt.tiltak.clients.axsys.Enheter
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.VeilederConnector
import no.nav.amt.tiltak.navansatt.auth.NavAnsattServiceImpl

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
		every { veilederConnector.hentVeileder(navIdent) } returns Veileder(navIdent, navn, null, null)
		every { axsysClient.hentTilganger(navIdent) } returns Enheter(listOf(
			Enhet("Enhet", listOf("TEMA1", "TEMA2"), "Navn")
		))
		val ansatt = navAnsattService.getNavAnsatt(navIdent)

		ansatt.navIdent shouldBe navIdent
		ansatt.navn shouldBe navn
		ansatt.tilganger.harTilgang("Enhet", "TEMA1") shouldBe true
		ansatt.tilganger.harTilgang("Enhet", "TEMA2") shouldBe true
		ansatt.tilganger.harTilgang("Enhet", "TEMA3") shouldBe false
		ansatt.tilganger.harTilgang("Enhet2", "TEMA1") shouldBe false
	}


	"getNavAnsatt - henter veileder - axys call is lazyloaded" {
		every { veilederConnector.hentVeileder(navIdent) } returns Veileder(navIdent, navn, null, null)
		every { axsysClient.hentTilganger(navIdent) } returns Enheter(listOf(
			Enhet("Enhet", listOf("TEMA1", "TEMA2"), "Navn")
		))
		val ansatt = navAnsattService.getNavAnsatt(navIdent)

		ansatt.navIdent shouldBe navIdent
		ansatt.navn shouldBe navn
		verify(exactly = 0) { axsysClient.hentTilganger(navIdent) }

		ansatt.tilganger.harTilgang("Enhet", "TEMA1") shouldBe true
		ansatt.tilganger.harTilgang("Enhet", "TEMA2") shouldBe true
		ansatt.tilganger.harTilgang("Enhet", "TEMA3") shouldBe false
		ansatt.tilganger.harTilgang("Enhet2", "TEMA1") shouldBe false

		verify(exactly = 1) { axsysClient.hentTilganger(navIdent) }
	}

})
