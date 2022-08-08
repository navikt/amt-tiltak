package no.nav.amt.tiltak.ansatt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import java.util.*

class AnsattDtoTest : FunSpec({

	test("Tilknyttede arrangører der ansatt har altinnkoordinator rolle bør har harAltinnKoordinatorRettighet satt til true") {

		val ansatt = Ansatt(
			id = UUID.randomUUID(),
			personligIdent = "12345678",
			fornavn = "FORNAVN",
			mellomnavn = "MELLOMNAVN",
			etternavn = "ETTERNAVN",
			arrangorer = listOf(
				TilknyttetArrangor(
					id = UUID.randomUUID(),
					navn = "ORG1",
					organisasjonsnummer = "1",
					overordnetEnhetOrganisasjonsnummer = "1",
					overordnetEnhetNavn = "OVER_ORG_1",
					roller = listOf(
						"KOORDINATOR"
					)
				),
				TilknyttetArrangor(
					id = UUID.randomUUID(),
					navn = "ORG2",
					organisasjonsnummer = "2",
					overordnetEnhetOrganisasjonsnummer = "2",
					overordnetEnhetNavn = "OVER_ORG_2",
					roller = listOf()
				)
			)
		)

		val virksomheterMedKoordinatorrettigheter = listOf("1")

		val dto = ansatt.toDto(virksomheterMedKoordinatorrettigheter)

		val org1 = dto.arrangorer.find { it.navn == "ORG1" }

		org1 shouldNotBe null
		org1?.harAltinnKoordinatorRettighet shouldBe true

		val org2 = dto.arrangorer.find { it.navn == "ORG2" }
		org2 shouldNotBe null
		org2?.harAltinnKoordinatorRettighet shouldBe false

	}


})
