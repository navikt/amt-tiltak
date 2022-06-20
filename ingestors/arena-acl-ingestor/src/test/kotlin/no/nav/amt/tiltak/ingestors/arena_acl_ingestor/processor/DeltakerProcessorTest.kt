package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

class DeltakerProcessorTest : StringSpec({

	"Skal ikke prosessere meldinger på brukere med diskresjonskode" {
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService, mockk())

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
				statusEndretDato = LocalDateTime.now(),
				begrunnelseForDeltakelse = null
			)
		))

		verify(exactly = 0) {
			deltakerService.upsertDeltaker(any(), any())
		}
	}

	"Skal slette deltaker som er feilregistrert" {
		val deltakerId = UUID.randomUUID()
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService, mockk())

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
				statusEndretDato = LocalDateTime.now(),
				begrunnelseForDeltakelse = null
			)
		))

		verify(exactly = 1) {
			deltakerService.slettDeltaker(deltakerId)
		}
	}

	"Skal slette deltaker for delete melding" {
		val deltakerId = UUID.randomUUID()
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService, mockk())

		every {
			deltakerService.slettDeltaker(any())
		} returns Unit

		processor.processMessage(MessageWrapper(
			transactionId = "",
			type = "",
			timestamp = LocalDateTime.now(),
			operation = Operation.DELETED,
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
				statusEndretDato = LocalDateTime.now(),
				begrunnelseForDeltakelse = null
			)
		))

		verify(exactly = 1) {
			deltakerService.slettDeltaker(deltakerId)
		}
	}

	"upsertDeltaker - oppdaterer deltaker og status" {
		val gjennomforingService: GjennomforingService = mockk()
		val deltakerService: DeltakerService = mockk()
		val personService: PersonService = mockk()
		val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(SingletonPostgresContainer.getDataSource()))

		val personIdent = "123"
		val message = MessageWrapper(
			transactionId = "",
			type = "",
			timestamp = LocalDateTime.now(),
			operation = Operation.CREATED,
			payload = DeltakerPayload(
				id = UUID.randomUUID(),
				gjennomforingId = UUID.randomUUID(),
				personIdent = personIdent,
				startDato = null,
				sluttDato = null,
				status = DeltakerPayload.Status.DELTAR,
				dagerPerUke = null,
				prosentDeltid = null,
				registrertDato = LocalDateTime.now(),
				statusEndretDato = LocalDateTime.now(),
				begrunnelseForDeltakelse = null
			)
		)

		every {
			personService.hentPerson(personIdent)
		} returns Person(
			fornavn = "Fornavn",
			mellomnavn = null,
			etternavn = "Etternavn",
			telefonnummer = null,
			diskresjonskode = null
		)

		every { gjennomforingService.getGjennomforing(message.payload.gjennomforingId) } returns GJENNOMFORING_1.toGjennomforing(
			TILTAK_1.toTiltak(),
			ARRANGOR_1.toArrangor()
		)
		every {
			deltakerService.upsertDeltaker(personIdent, any())
		} returns Unit
		every {
			deltakerService.insertStatus(any())
		} returns Unit

		val processor = DeltakerProcessor(gjennomforingService, deltakerService, personService, transactionTemplate)
		processor.processMessage(message)

		verify(exactly = 1) {
			deltakerService.upsertDeltaker(personIdent, any())
		}
		verify (exactly = 1) {
			deltakerService.insertStatus(any())
		}
	}

})
