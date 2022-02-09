package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import java.time.LocalDateTime
import java.util.*

class DeltakerProcessorTest : StringSpec({

	"Skal ikke prosessere meldinger på brukere med diskresjonskode" {
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService)

		every {
			personService.hentPerson("1234")
		} returns Person(
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			telefonnummer = null,
			diskresjonskode = Diskresjonskode.KODE_6
		)

		processor.processMessage(MessageWrapper(
			transactionId = "",
			type = "",
			timestamp = LocalDateTime.now(),
			operation = Operation.CREATED,
			payload = DeltakerPayload(
				id = UUID.randomUUID(),
				gjennomforingId = UUID.randomUUID(),
				personIdent = "1234",
				startDato = null,
				sluttDato = null,
				status = DeltakerPayload.Status.DELTAR,
				dagerPerUke = null,
				prosentDeltid = null,
				registrertDato = LocalDateTime.now(),
				statusEndretDato = LocalDateTime.now()
			)
		))

		verify(exactly = 0) {
			deltakerService.upsertDeltaker(any(), any(), any())
		}
	}

	"Skal slette deltaker som er feilregistrert" {
		val deltakerId = UUID.randomUUID()
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService)

		every {
			personService.hentPerson("1234")
		} returns Person(
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			telefonnummer = null,
			diskresjonskode = Diskresjonskode.KODE_6
		)

		every {
			deltakerService.slettDeltaker(any())
		} returns Unit

		processor.processMessage(MessageWrapper(
			transactionId = "",
			type = "",
			timestamp = LocalDateTime.now(),
			operation = Operation.CREATED,
			payload = DeltakerPayload(
				id = deltakerId,
				gjennomforingId = UUID.randomUUID(),
				personIdent = "1234",
				startDato = null,
				sluttDato = null,
				status = DeltakerPayload.Status.FEILREGISTRERT,
				dagerPerUke = null,
				prosentDeltid = null,
				registrertDato = LocalDateTime.now(),
				statusEndretDato = LocalDateTime.now()
			)
		))

		verify(exactly = 1) {
			deltakerService.slettDeltaker(deltakerId)
		}
	}

})
