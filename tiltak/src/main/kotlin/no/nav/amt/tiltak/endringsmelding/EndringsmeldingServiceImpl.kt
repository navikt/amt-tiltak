package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.port.AuditEventSeverity
import no.nav.amt.tiltak.core.port.AuditEventType
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingServiceImpl(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val auditLoggerService: AuditLoggerService,
) : EndringsmeldingService {

	companion object {
		const val AUDIT_LOG_REASON =
			"NAV-ansatt har lest melding fra tiltaksarrangoer om oppstartsdato paa tiltak for aa registrere dette."
	}

	override fun hentEndringsmelding(id: UUID): Endringsmelding {
		return endringsmeldingRepository.get(id).toModel()
	}

	override fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): Endringsmelding {
		return endringsmeldingRepository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId).toModel()
	}

	override fun markerSomFerdig(endringsmeldingId: UUID, navAnsattId: UUID) {
		val endringsmelding = endringsmeldingRepository.get(endringsmeldingId)

		if (!endringsmelding.aktiv) {
			throw EndringsmeldingIkkeAktivException("Endringsmelding er ikke aktiv")
		}

		endringsmeldingRepository.markerSomFerdig(endringsmeldingId, navAnsattId)

		auditLoggerService.navAnsattAuditLog(
			navAnsattId,
			endringsmelding.deltakerId,
			AuditEventType.ACCESS,
			AuditEventSeverity.INFO,
			AUDIT_LOG_REASON
		)
	}

	override fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByGjennomforing(gjennomforingId)
			.map { it.toModel() }
	}

	override fun hentEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByDeltaker(deltakerId).map { it.toModel() }
	}

}
