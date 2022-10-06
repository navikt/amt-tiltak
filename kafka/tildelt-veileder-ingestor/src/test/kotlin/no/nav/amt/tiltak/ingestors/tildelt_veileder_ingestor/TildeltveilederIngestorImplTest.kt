package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.PersonService
import java.util.*

class TildeltveilederIngestorImplTest : StringSpec({

	"Skal ingeste kafka melding" {
		val navAnsattService = mockk<NavAnsattService>()
		val personService = mockk<PersonService>()
		val deltakerService = mockk<DeltakerService>()

		val ingestor = TildeltVeilederIngestorImpl(navAnsattService, personService, deltakerService)

		val brukerFnr = "123454364334"
		val veilederId = UUID.randomUUID()
		val navAnsatt = NavAnsatt(id = veilederId, navIdent = "Z12345", navn = "Test")

		every {
			navAnsattService.getNavAnsatt("Z12345")
		} returns navAnsatt

		every {
			navAnsattService.upsertNavAnsatt(any())
		} returns Unit

		every {
			personService.hentGjeldendePersonligIdent("10000000000")
		} returns brukerFnr

		every {
			deltakerService.oppdaterAnsvarligVeileder(brukerFnr, veilederId)
		} returns Unit

		every {
			deltakerService.finnesBruker(brukerFnr)
		} returns true

		ingestor.ingestKafkaRecord("""
			{
			  "aktorId": "10000000000",
			  "veilederId": "Z12345",
			  "tilordnet": "2021-09-02T11:13:42.787+02:00"
			}
		""".trimIndent())
	}

	"Skal ikke upserte veileder hvis bruker ikke finnes" {
		val navAnsattService = mockk<NavAnsattService>()
		val personService = mockk<PersonService>()
		val deltakerService = mockk<DeltakerService>()

		val ingestor = TildeltVeilederIngestorImpl(navAnsattService, personService, deltakerService)

		val brukerFnr = "123454364334"

		every {
			personService.hentGjeldendePersonligIdent("10000000000")
		} returns brukerFnr

		every {
			deltakerService.finnesBruker(brukerFnr)
		} returns false

		ingestor.ingestKafkaRecord("""
			{
			  "aktorId": "10000000000",
			  "veilederId": "Z12345",
			  "tilordnet": "2021-09-02T11:13:42.787+02:00"
			}
		""".trimIndent())

		verify(exactly = 0) {
			navAnsattService.getNavAnsatt(any<String>())
		}

		verify(exactly = 0) {
			deltakerService.oppdaterAnsvarligVeileder(any(), any())
		}

		verify(exactly = 0) {
			navAnsattService.upsertNavAnsatt(any())
		}
	}

})
