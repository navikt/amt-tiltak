package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.port.AuditEventSeverity
import no.nav.amt.tiltak.core.port.AuditEventType
import no.nav.amt.tiltak.core.port.AuditLoggerService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingService(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val endringsmeldingQuery: EndringsmeldingForGjennomforingQuery,
	private val auditLoggerService: AuditLoggerService
) {

	companion object {
		const val AUDIT_LOG_REASON = "NAV-ansatt har lest melding fra tiltaksarrangør om oppstartsdato på tiltak for å registrere dette."
	}

	open fun hentEndringsmelding(id: UUID): EndringsmeldingDbo {
		return endringsmeldingRepository.get(id)
	}

	open fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): EndringsmeldingDbo {
		return endringsmeldingRepository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId)
	}

	open fun markerSomFerdig(endringsmeldingId: UUID, navAnsattId: UUID) {
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

	open fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID) : List<Endringsmelding> {
		return endringsmeldingQuery
			.query(gjennomforingId)
			.map { it.toEndringsmelding()}
	}

	open fun hentEndringsmeldingerForDeltaker(deltakerId: UUID) : List<EndringsmeldingDbo> {
		return endringsmeldingRepository.getByDeltaker(deltakerId)
	}

}
