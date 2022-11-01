package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingServiceImpl(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val auditLoggerService: AuditLoggerService,
	private val transactionTemplate: TransactionTemplate,
) : EndringsmeldingService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun hentEndringsmelding(id: UUID): Endringsmelding {
		return endringsmeldingRepository.get(id).toModel()
	}

	override fun markerSomUtfort(endringsmeldingId: UUID, navAnsattId: UUID) {
		val endringsmelding = endringsmeldingRepository.get(endringsmeldingId)

		if (endringsmelding.status != Endringsmelding.Status.AKTIV) {
			throw EndringsmeldingIkkeAktivException("Endringsmelding er ikke aktiv")
		}

		endringsmeldingRepository.markerSomUtfort(endringsmeldingId, navAnsattId)

		auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(navAnsattId, endringsmelding.deltakerId)
	}

	override fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByGjennomforing(gjennomforingId)
			.map { it.toModel() }
	}

	override fun hentEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByDeltaker(deltakerId).map { it.toModel() }
	}

	override fun hentAktive(deltakerIder: List<UUID>): Map<UUID, List<Endringsmelding>> {
		val endringsmeldinger = endringsmeldingRepository.getAktive(deltakerIder).map { it.toModel() }
		val map = mutableMapOf<UUID, List<Endringsmelding>>()
		deltakerIder.forEach {
			map.put(it, endringsmeldinger.filter { e -> e.deltakerId == it })
		}
		return map
	}

	override fun hentAntallAktiveForGjennomforing(gjennomforingId: UUID): Int {
		return hentEndringsmeldingerForGjennomforing(gjennomforingId).count { it.status == Endringsmelding.Status.AKTIV }
	}
	 private fun EndringsmeldingDbo.toModel(): Endringsmelding {
		return Endringsmelding(
			id = id,
			deltakerId = deltakerId,
			utfortAvNavAnsattId = utfortAvNavAnsattId,
			utfortTidspunkt = utfortTidspunkt,
			opprettetAvArrangorAnsattId = opprettetAvArrangorAnsattId,
			status = status,
			innhold = mapEndringsmeldingInnhold(innhold),
			opprettet = createdAt,
		)
	}
	private fun mapEndringsmeldingInnhold(innhold: EndringsmeldingDbo.Innhold): Endringsmelding.Innhold {
		return when(innhold) {
			is EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold ->
				Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(innhold.oppstartsdato)
			is EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold ->
				Endringsmelding.Innhold.EndreOppstartsdatoInnhold(innhold.oppstartsdato)
			is EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold ->
				Endringsmelding.Innhold.ForlengDeltakelseInnhold(innhold.sluttdato)
			is EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold ->
				Endringsmelding.Innhold.AvsluttDeltakelseInnhold(innhold.sluttdato, innhold.aarsak)
			is EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold ->
				Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold(innhold.aarsak)
		}
	}

	override fun opprettLeggTilOppstartsdatoEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		oppstartsdato: LocalDate
	) {
		val innhold = EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold(oppstartsdato)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO,
			innhold,
		)
	}

	override fun opprettEndreOppstartsdatoEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		oppstartsdato: LocalDate
	) {
		val innhold = EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold(oppstartsdato)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO,
			innhold,
		)
	}

	override fun opprettForlengDeltakelseEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate
	) {
		val innhold = EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold(sluttdato)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			EndringsmeldingDbo.Type.FORLENG_DELTAKELSE,
			innhold,
		)
	}

	override fun opprettAvsluttDeltakelseEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate,
		statusAarsak: Deltaker.StatusAarsak
	) {
		val innhold = EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold(sluttdato, statusAarsak)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE,
			innhold,
		)
	}

	override fun opprettDeltakerIkkeAktuellEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		statusAarsak: Deltaker.StatusAarsak
	) {
		val innhold = EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold(statusAarsak)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL,
			innhold,
		)
	}

	private fun opprettOgMarkerAktiveSomUtdatert(
		deltakerId: UUID, opprettetAvArrangorAnsattId: UUID, type: EndringsmeldingDbo.Type, innhold: EndringsmeldingDbo.Innhold
	) {
		val id = UUID.randomUUID()
		transactionTemplate.executeWithoutResult {
			endringsmeldingRepository.markerSomUtdatert(deltakerId, type)
			endringsmeldingRepository.insertEndringsmelding(id, deltakerId, opprettetAvArrangorAnsattId, type, innhold)
		}

		log.info("Endringsmelding av type ${type.name} opprettet med id $id for deltaker $deltakerId")

	}

}


