package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
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

	override fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByGjennomforing(gjennomforingId)
			.map { it.toModel() }
	}

	override fun hentAktiveEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return hentEndringsmeldingerForGjennomforing(gjennomforingId)
			.filter { it.status == Endringsmelding.Status.AKTIV }
	}

	override fun hentAktiveEndringsmeldingerForDeltaker(deltakerId: UUID): List<Endringsmelding> {
		return hentAktiveEndringsmeldingerForDeltakere(listOf(deltakerId))
			.getOrDefault(deltakerId, emptyList())

	}

	override fun hentAktiveEndringsmeldingerForDeltakere(deltakerIder: List<UUID>): Map<UUID, List<Endringsmelding>> {
		val endringsmeldinger = endringsmeldingRepository.getAktive(deltakerIder).map { it.toModel() }

		return deltakerIder.associateWith { endringsmeldinger.filter { e -> e.deltakerId == it } }

	}

	override fun markerSomUtfort(endringsmeldingId: UUID, navAnsattId: UUID) {
		val endringsmelding = endringsmeldingRepository.get(endringsmeldingId)

		if (endringsmelding.status != Endringsmelding.Status.AKTIV) {
			throw EndringsmeldingIkkeAktivException("Endringsmelding er ikke aktiv")
		}

		endringsmeldingRepository.markerSomUtfort(endringsmeldingId, navAnsattId)

		auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(navAnsattId, endringsmelding.deltakerId)
	}

	override fun markerSomTilbakekalt(id: UUID) {
		val endringsmelding = hentEndringsmelding(id)

		if (endringsmelding.status != Endringsmelding.Status.AKTIV) {
			throw EndringsmeldingIkkeAktivException(
				"Kan ikke tilbakekalle endringsmelding med id $id med status ${endringsmelding.status}"
			)
		}

		endringsmeldingRepository.markerSomTilbakekalt(id)

		log.info("Endringsmelding med id $id er tilbakekalt av arrangor ansatt")
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
			innhold,
		)
	}

	override fun opprettEndreDeltakelseProsentEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		deltakerProsent: Int
	) {
		val innhold = EndringsmeldingDbo.Innhold.EndreDeltakelseProsentInnhold(
			nyDeltakelseProsent = deltakerProsent
		)

		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold
		)
	}

	override fun opprettAvsluttDeltakelseEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate,
		statusAarsak: DeltakerStatus.Aarsak
	) {
		val aarsak = EndringsmeldingDbo.DeltakerStatusAarsak(statusAarsak.type, statusAarsak.beskrivelse)
		val innhold = EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold(sluttdato, aarsak)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
		)
	}

	override fun opprettDeltakerIkkeAktuellEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		statusAarsak: DeltakerStatus.Aarsak
	) {
		val aarsak = EndringsmeldingDbo.DeltakerStatusAarsak(statusAarsak.type, statusAarsak.beskrivelse)
		val innhold = EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold(aarsak)
		opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
		)
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
				Endringsmelding.Innhold.AvsluttDeltakelseInnhold(
					innhold.sluttdato, DeltakerStatus.Aarsak(innhold.aarsak.type, innhold.aarsak.beskrivelse)
				)
			is EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold ->
				Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold(
					DeltakerStatus.Aarsak(innhold.aarsak.type, innhold.aarsak.beskrivelse)
				)

			is EndringsmeldingDbo.Innhold.EndreDeltakelseProsentInnhold ->
				Endringsmelding.Innhold.EndreDeltakelseProsentInnhold(
					deltakelseProsent = innhold.nyDeltakelseProsent
				)
		}
	}

	private fun opprettOgMarkerAktiveSomUtdatert(
		deltakerId: UUID, opprettetAvArrangorAnsattId: UUID, innhold: EndringsmeldingDbo.Innhold
	) {
		val id = UUID.randomUUID()
		transactionTemplate.executeWithoutResult {
			endringsmeldingRepository.markerAktiveSomUtdatert(deltakerId, innhold.type())
			endringsmeldingRepository.insert(id, deltakerId, opprettetAvArrangorAnsattId, innhold)
		}

		log.info("Endringsmelding av type ${innhold.type().name} opprettet med id $id for deltaker $deltakerId")

	}

}


