package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
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

		auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(navAnsattId, endringsmelding.deltakerId)
	}

	override fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByGjennomforing(gjennomforingId)
			.map { it.toModel() }
	}

	override fun hentEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByDeltaker(deltakerId).map { it.toModel() }
	}

	override fun hentSisteAktive(deltakerId: UUID) = endringsmeldingRepository.getAktiv(deltakerId)?.toModel()

	override fun hentAntallAktiveForGjennomforing(gjennomforingId: UUID): Int {
		return hentEndringsmeldingerForGjennomforing(gjennomforingId).count { it.aktiv }
	}

}
