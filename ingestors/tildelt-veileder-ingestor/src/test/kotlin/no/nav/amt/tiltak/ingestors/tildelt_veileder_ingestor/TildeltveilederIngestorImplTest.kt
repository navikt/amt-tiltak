package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.VeilederConnector
import no.nav.amt.tiltak.core.port.VeilederService
import java.util.*

class TildeltveilederIngestorImplTest : StringSpec({

	"Skal ingeste kafka melding" {
		val veilederConnector = mockk<VeilederConnector>()
		val veilederService = mockk<VeilederService>()
		val personService = mockk<PersonService>()
		val brukerService = mockk<BrukerService>()

		val ingestor = TildeltVeilederIngestorImpl(veilederConnector, veilederService, personService, brukerService)

		val brukerFnr = "123454364334"
		val veilederId = UUID.randomUUID()
		val navAnsatt = NavAnsatt(navIdent = "Z12345", navn = "Test")

		every {
			veilederConnector.hentVeileder("Z12345")
		} returns navAnsatt

		every {
			veilederService.upsertVeileder(navAnsatt)
		} returns veilederId

		every {
			personService.hentGjeldendePersonligIdent("10000000000")
		} returns brukerFnr

		every {
			brukerService.oppdaterAnsvarligVeileder(brukerFnr, veilederId)
		} returns Unit

		every {
			brukerService.finnesBruker(brukerFnr)
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
		val veilederConnector = mockk<VeilederConnector>()
		val veilederService = mockk<VeilederService>()
		val personService = mockk<PersonService>()
		val brukerService = mockk<BrukerService>()

		val ingestor = TildeltVeilederIngestorImpl(veilederConnector, veilederService, personService, brukerService)

		val brukerFnr = "123454364334"

		every {
			personService.hentGjeldendePersonligIdent("10000000000")
		} returns brukerFnr

		every {
			brukerService.finnesBruker(brukerFnr)
		} returns false

		ingestor.ingestKafkaRecord("""
			{
			  "aktorId": "10000000000",
			  "veilederId": "Z12345",
			  "tilordnet": "2021-09-02T11:13:42.787+02:00"
			}
		""".trimIndent())

		verify(exactly = 0) {
			veilederConnector.hentVeileder(any())
		}

		verify(exactly = 0) {
			brukerService.oppdaterAnsvarligVeileder(any(), any())
		}

		verify(exactly = 0) {
			veilederConnector.hentVeileder(any())
		}

		verify(exactly = 0) {
			veilederService.upsertVeileder(any())
		}
	}

})
