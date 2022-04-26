package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Tiltak
import java.time.LocalDateTime
import java.util.*

class GjennomforingProcessorTest : StringSpec({

	"Skal slette gjennomføring hvis melding om sletting mottas" {
		val gjennomforingId = UUID.randomUUID()

		val arrangorService: ArrangorService = mockk()
		val gjennomforingService: GjennomforingService = mockk()
		val tiltakService: TiltakService = mockk()

		val processor = GjennomforingProcessor(arrangorService, gjennomforingService, tiltakService)

		every {
			gjennomforingService.slettGjennomforing(gjennomforingId)
		} returns Unit

		processor.processMessage(MessageWrapper(
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
				fremmoteDato = null
			)
		))

		verify(exactly = 1) {
			gjennomforingService.slettGjennomforing(gjennomforingId)
		}
	}

})
