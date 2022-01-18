package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.*
import java.util.*

class TildeltveilederIngestorImplTest : StringSpec({

	"Should ingest kafka record" {
		val veilederConnector = mockk<VeilederConnector>()
		val veilederService = mockk<VeilederService>()
		val personService = mockk<PersonService>()
		val deltakerService = mockk<DeltakerService>()

		val ingestor = TildeltVeilederIngestorImpl(veilederConnector, veilederService, personService, deltakerService)

		val brukerFnr = "123454364334"
		val veilederId = UUID.randomUUID()
		val veileder = Veileder("Z12345", "Test", null, null)

		every {
			veilederConnector.hentVeileder("Z12345")
		} returns veileder

		every {
			veilederService.upsertVeileder(veileder)
		} returns veilederId

		every {
			personService.hentGjeldendePersonligIdent("10000000000")
		} returns brukerFnr

		every {
			deltakerService.oppdaterDeltakerVeileder(brukerFnr, veilederId)
		} returns Unit

		ingestor.ingestKafkaRecord("""
			{
			  "aktorId": "10000000000",
			  "veilederId": "Z12345",
			  "tilordnet": "2021-09-02T11:13:42.787+02:00"
			}
		""".trimIndent())
	}

})
