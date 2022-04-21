package no.nav.amt.tiltak.nav_enhet_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.NavKontorService

class NavEnhetIngestorImplTest : StringSpec({

	"Skal ingeste kafka melding" {
		val navKontorService = mockk<NavKontorService>()

		val ingestor = NavEnhetIngestorImpl(navKontorService)

		every {
			navKontorService.upsertNavKontor(any(), any())
		} returns Unit

		ingestor.ingestKafkaRecord("""
			{
			  "enhetId": "1234",
			  "navn": "NAV Testheim"
			}
		""".trimIndent())

		verify (exactly = 1) { navKontorService.upsertNavKontor("1234", "NAV Testheim") }
	}

})
