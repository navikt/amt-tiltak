package no.nav.amt.tiltak.endringsmelding

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.amt.tiltak.data_publisher.DataPublisherServiceImpl
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class EndringsmeldingServiceImplTest {
	lateinit var endringsmeldingService: EndringsmeldingServiceImpl

	lateinit var repository: EndringsmeldingRepository

	lateinit var auditLoggerService: AuditLoggerService

	lateinit var transactionTemplate: TransactionTemplate

	lateinit var publisherService: DataPublisherServiceImpl


	@BeforeEach
	fun beforeEach() {
		repository = mockk(relaxUnitFun = true)
		auditLoggerService = mockk(relaxUnitFun = true)
		transactionTemplate = mockk(relaxUnitFun = true)
		publisherService = mockk()
		endringsmeldingService = EndringsmeldingServiceImpl(repository, auditLoggerService, transactionTemplate, publisherService)

		every { publisherService.publish(id = any(), type = any(), erKometDeltaker = any()) } returns Unit
	}

	@Test
	fun `markerSomTilbakekalt - skal sette status pa endringsmelding til TILBAKEKALT`() {
		val utfortEndringsmelding = endringsmeldingDbo.copy(id = UUID.randomUUID(), status = Endringsmelding.Status.UTFORT)

		every {
			repository.get(endringsmeldingDbo.id)
		} returns endringsmeldingDbo

		every {
			repository.get(utfortEndringsmelding.id)
		} returns utfortEndringsmelding

		shouldThrow<EndringsmeldingIkkeAktivException> {
			endringsmeldingService.markerSomTilbakekalt(utfortEndringsmelding.id)
		}

		endringsmeldingService.markerSomTilbakekalt(endringsmeldingDbo.id)

		verify(exactly = 1) {
			repository.markerSomTilbakekalt(endringsmeldingDbo.id)
		}
		verify(exactly = 1) { publisherService.publish(endringsmeldingDbo.id, DataPublishType.ENDRINGSMELDING, null) }
	}

	@Test
	fun `markerSomUtfort - skal sette status på endringsmelding til UTFORT og audit logge`() {
		every {
			repository.get(endringsmeldingDbo.id)
		} returns endringsmeldingDbo

		endringsmeldingService.markerSomUtfort(endringsmeldingDbo.id, NAV_ANSATT_1.id)

		verify(exactly = 1) {
			auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(NAV_ANSATT_1.id, DELTAKER_1.id)
		}

		verify(exactly = 1) {
			repository.markerSomUtfort(endringsmeldingDbo.id, NAV_ANSATT_1.id)
		}
		verify(exactly = 1) { publisherService.publish(endringsmeldingDbo.id, DataPublishType.ENDRINGSMELDING, null) }
	}

	@Test
	fun `opprettLeggTilOppstartsdatoEndringsmelding - finnes allerede - publiserer utdatert og ny endringsmelding`() {
		every {
			repository.getAktive(endringsmeldingDbo.deltakerId, EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO)
		} returns listOf(endringsmeldingDbo)

		val nyOppstartsdato = LocalDate.now().plusWeeks(1)

		endringsmeldingService.opprettLeggTilOppstartsdatoEndringsmelding(endringsmeldingDbo.deltakerId, endringsmeldingDbo.opprettetAvArrangorAnsattId, nyOppstartsdato)

		verify(exactly = 2) { publisherService.publish(any(), DataPublishType.ENDRINGSMELDING, null) }
	}

	private val endringsmeldingDbo = EndringsmeldingDbo(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_1.id,
			utfortAvNavAnsattId = null,
			utfortTidspunkt = null,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			status = Endringsmelding.Status.AKTIV,
			type = EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO,
			innhold = EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold(LocalDate.now()),
			createdAt = ZonedDateTime.now(),
			modifiedAt = ZonedDateTime.now(),
		)

}
