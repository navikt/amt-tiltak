package no.nav.amt.tiltak.tilgangskontroll.ad_gruppe

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.poao_tilgang.AdGruppe
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClient
import java.util.*

class AdGruppeServiceTest : FunSpec({

	test("hentAdGrupper - skal cache svar fra client") {
		val client = mockk<PoaoTilgangClient>()
		val service = AdGruppeService(client)

		val norskIdent = "21354354"

		val adGruppeId = UUID.randomUUID()
		val adGrupper = listOf(AdGruppe(adGruppeId, "Gruppe1"))

		every {
			client.hentAdGrupper(norskIdent)
		} returns adGrupper

		service.hentAdGrupper(norskIdent).first().id shouldBe adGruppeId
		service.hentAdGrupper(norskIdent).first().id shouldBe adGruppeId

		verify(exactly = 1) {
			client.hentAdGrupper(any())
		}
	}

	test("erMedlemAvGruppe - skal sjekke medlemskap til gruppe") {
		val client = mockk<PoaoTilgangClient>()
		val service = AdGruppeService(client)

		val norskIdent = "21354354"

		val adGruppeId = UUID.randomUUID()
		val adGruppeNavn = "Gruppe1"
		val adGrupper = listOf(AdGruppe(adGruppeId, adGruppeNavn))

		every {
			client.hentAdGrupper(norskIdent)
		} returns adGrupper

		service.erMedlemAvGruppe(norskIdent, adGruppeNavn) shouldBe true
		service.erMedlemAvGruppe(norskIdent, "Gruppe2") shouldBe false
	}

})
