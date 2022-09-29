package no.nav.amt.tiltak.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClient

class SkjermetPersonServiceTest : FunSpec ({

	test("erSkjermet - skal cache svar fra client") {
		val client = mockk<PoaoTilgangClient>()
		val service = SkjermetPersonServiceImpl(client)

		val norskIdent = "21354354"

		every {
			client.erSkjermet(listOf(norskIdent))
		} returns mapOf(norskIdent to true)

		service.erSkjermet(norskIdent) shouldBe true
		service.erSkjermet(norskIdent) shouldBe true

		verify(exactly = 1) {
			client.erSkjermet(any())
		}
	}

})
