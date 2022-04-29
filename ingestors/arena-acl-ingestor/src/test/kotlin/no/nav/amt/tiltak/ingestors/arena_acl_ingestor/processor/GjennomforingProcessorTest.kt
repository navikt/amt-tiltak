package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Tiltak
import java.time.LocalDateTime
import java.util.*

class GjennomforingProcessorTest : StringSpec({

	val arrangorService: ArrangorService = mockk()
	val gjennomforingService: GjennomforingService = mockk()
	val tiltakService: TiltakService = mockk()
	val navEnhetService: NavEnhetService = mockk()

	lateinit var gjennomforingProcessor: GjennomforingProcessor

	beforeEach {
		gjennomforingProcessor = GjennomforingProcessor(arrangorService, gjennomforingService, tiltakService, navEnhetService)
	}

	"Skal slette gjennomføring hvis melding om sletting mottas" {
		val gjennomforingId = UUID.randomUUID()

		every {
			gjennomforingService.slettGjennomforing(gjennomforingId)
		} returns Unit

		gjennomforingProcessor.processMessage(MessageWrapper(
			transactionId = "",
			type = "",
			timestamp = LocalDateTime.now(),
			operation = Operation.DELETED,
			payload = GjennomforingPayload(
				id = gjennomforingId,
				tiltak = Tiltak(UUID.randomUUID(), "", ""),
				virksomhetsnummer = "",
				navn = "",
				status = GjennomforingPayload.Status.GJENNOMFORES,
				startDato = null,
				sluttDato = null,
				registrertDato = LocalDateTime.now(),
				fremmoteDato = null,
				ansvarligNavEnhetId = null,
				lopenr = null,
				opprettetAar = null
			)
		))

		verify(exactly = 1) {
			gjennomforingService.slettGjennomforing(gjennomforingId)
		}
	}

})
