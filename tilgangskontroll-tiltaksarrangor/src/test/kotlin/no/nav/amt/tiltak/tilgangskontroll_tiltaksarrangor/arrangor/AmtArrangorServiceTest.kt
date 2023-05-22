package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import java.util.UUID

class AmtArrangorServiceTest : FunSpec({
	val amtArrangorClient = mockk<AmtArrangorClient>()
	val amtArrangorService = AmtArrangorService(amtArrangorClient)

	test("Henter roller fra amt-arrangør") {
		val personident = "12345678910"
		val orgnummer1 = "98879887"
		val orgnummer2 = "12341234"

		every { amtArrangorClient.hentAnsatt(personident) } returns AmtArrangorClient.AnsattDto(
			id = UUID.randomUUID(),
			personalia = AmtArrangorClient.PersonaliaDto(
				personident = personident,
				personId = UUID.randomUUID(),
				navn = AmtArrangorClient.Navn("Fornavn", null, "Etternavn")
			),
			arrangorer = listOf(
				AmtArrangorClient.TilknyttetArrangorDto(
					arrangorId = UUID.randomUUID(),
					arrangor = AmtArrangorClient.Arrangor(UUID.randomUUID(), "Arrangør 1", orgnummer1),
					overordnetArrangor = null,
					deltakerlister = emptySet(),
					roller = listOf(AmtArrangorClient.AnsattRolle.KOORDINATOR),
					veileder = emptyList(),
					koordinator = emptyList()
				),
				AmtArrangorClient.TilknyttetArrangorDto(
					arrangorId = UUID.randomUUID(),
					arrangor = AmtArrangorClient.Arrangor(UUID.randomUUID(), "Arrangør 2", orgnummer2),
					overordnetArrangor = null,
					deltakerlister = emptySet(),
					roller = listOf(AmtArrangorClient.AnsattRolle.VEILEDER),
					veileder = emptyList(),
					koordinator = emptyList()
				)
			)
		)

		val ansattRoller = amtArrangorService.hentTiltaksarrangorRoller(personident)

		ansattRoller.size shouldBe 2
		ansattRoller.find { it.organisasjonsnummer == orgnummer1 }?.roller shouldBe listOf(ArrangorAnsattRolle.KOORDINATOR)
		ansattRoller.find { it.organisasjonsnummer == orgnummer2 }?.roller shouldBe listOf(ArrangorAnsattRolle.VEILEDER)
	}
})
