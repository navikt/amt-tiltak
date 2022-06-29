package no.nav.amt.tiltak.nav_enhet_ingestor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.NavEnhetService

class NavEnhetIngestorImplTest : StringSpec({

	"Skal ingeste kafka melding" {
		val navEnhetService = mockk<NavEnhetService>()

		val ingestor = NavEnhetIngestorImpl(navEnhetService)

		every {
			navEnhetService.upsertNavEnhet(any(), any())
		} returns Unit

		ingestor.ingestKafkaRecord("""
			{
			  "enhetId": "1234",
			  "navn": "NAV Testheim"
			}
		""".trimIndent())

		verify (exactly = 1) { navEnhetService.upsertNavEnhet("1234", "NAV Testheim") }
	}

})
