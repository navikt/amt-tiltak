package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.port.AuditLoggerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.util.UUID

@Service
open class EndringsmeldingServiceImpl(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val auditLoggerService: AuditLoggerService,
	private val transactionTemplate: TransactionTemplate,
	private val publisherService: DataPublisherService
) : EndringsmeldingService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun hentEndringsmelding(id: UUID): Endringsmelding {
		return endringsmeldingRepository.get(id).toModel()
	}

	override fun hentEndringsmeldingerForGjennomforing(gjennomforingId: UUID): List<Endringsmelding> {
		return endringsmeldingRepository.getByGjennomforing(gjennomforingId)
			.map { it.toModel() }
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
		publisherService.publish(endringsmeldingId, DataPublishType.ENDRINGSMELDING, null)
	}

	override fun markerSomTilbakekalt(id: UUID) {
		val endringsmelding = hentEndringsmelding(id)

		if (endringsmelding.status != Endringsmelding.Status.AKTIV) {
			throw EndringsmeldingIkkeAktivException(
				"Kan ikke tilbakekalle endringsmelding med id $id med status ${endringsmelding.status}"
			)
		}

		endringsmeldingRepository.markerSomTilbakekalt(id)
		publisherService.publish(id, DataPublishType.ENDRINGSMELDING, null)
		log.info("Endringsmelding med id $id er tilbakekalt av arrangor ansatt")
	}

	override fun opprettLeggTilOppstartsdatoEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		oppstartsdato: LocalDate
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold(oppstartsdato)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO
		)
	}

	override fun opprettEndreOppstartsdatoEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		oppstartsdato: LocalDate?
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold(oppstartsdato)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO
		)
	}

	override fun opprettForlengDeltakelseEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold(sluttdato)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.FORLENG_DELTAKELSE
		)
	}

	override fun opprettEndreDeltakelseProsentEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		deltakerProsent: Int,
		dagerPerUke: Int?,
		gyldigFraDato: LocalDate?
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.EndreDeltakelseProsentInnhold(
			nyDeltakelseProsent = deltakerProsent,
			dagerPerUke = dagerPerUke,
			gyldigFraDato = gyldigFraDato
		)

		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.ENDRE_DELTAKELSE_PROSENT

		)
	}

	override fun opprettAvsluttDeltakelseEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate,
		statusAarsak: EndringsmeldingStatusAarsak
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold(sluttdato, statusAarsak)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE

		)
	}

	override fun opprettDeltakerIkkeAktuellEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		statusAarsak: EndringsmeldingStatusAarsak
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold(statusAarsak)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL
		)
	}

	override fun opprettEndresluttdatoEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		sluttdato: LocalDate
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.EndreSluttdatoInnhold(sluttdato)
		return opprettOgMarkerAktiveSomUtdatert(
			deltakerId,
			arrangorAnsattId,
			innhold,
			EndringsmeldingDbo.Type.ENDRE_SLUTTDATO
		)
	}

	override fun opprettEndreSluttaarsakEndringsmelding(
		deltakerId: UUID,
		arrangorAnsattId: UUID,
		statusAarsak: EndringsmeldingStatusAarsak
	): UUID {
		val innhold = EndringsmeldingDbo.Innhold.EndreSluttaarsakInnhold(statusAarsak)
		return  opprettOgMarkerAktiveSomUtdatert(
			deltakerId = deltakerId,
			opprettetAvArrangorAnsattId = arrangorAnsattId,
			innhold = innhold,
			type = EndringsmeldingDbo.Type.ENDRE_SLUTTAARSAK
		)
	}

	override fun slett(deltakerId: UUID) {
		val endringsmeldinger = endringsmeldingRepository.getByDeltaker(deltakerId)
		endringsmeldingRepository.deleteByDeltaker(deltakerId)
		endringsmeldinger.forEach {
			publisherService.publish(it.id, DataPublishType.ENDRINGSMELDING, null)
		}
	}

	private fun EndringsmeldingDbo.toModel(): Endringsmelding {
		return Endringsmelding(
			id = id,
			deltakerId = deltakerId,
			utfortAvNavAnsattId = utfortAvNavAnsattId,
			utfortTidspunkt = utfortTidspunkt,
			opprettetAvArrangorAnsattId = opprettetAvArrangorAnsattId,
			status = status,
			innhold = innhold?.toModel(),
			opprettet = createdAt,
			type = type.toModel()
		)
	}

	private fun EndringsmeldingDbo.Type.toModel(): Endringsmelding.Type {
		return when (this) {
			EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO -> Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO
			EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO -> Endringsmelding.Type.ENDRE_OPPSTARTSDATO
			EndringsmeldingDbo.Type.FORLENG_DELTAKELSE -> Endringsmelding.Type.FORLENG_DELTAKELSE
			EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE -> Endringsmelding.Type.AVSLUTT_DELTAKELSE
			EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL -> Endringsmelding.Type.DELTAKER_IKKE_AKTUELL
			EndringsmeldingDbo.Type.ENDRE_DELTAKELSE_PROSENT -> Endringsmelding.Type.ENDRE_DELTAKELSE_PROSENT
			EndringsmeldingDbo.Type.ENDRE_SLUTTDATO -> Endringsmelding.Type.ENDRE_SLUTTDATO
			EndringsmeldingDbo.Type.ENDRE_SLUTTAARSAK -> Endringsmelding.Type.ENDRE_SLUTTAARSAK
		}
	}

	private fun EndringsmeldingDbo.Innhold.toModel(): Endringsmelding.Innhold {
		return when(this) {
			is EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold ->
				Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold(this.oppstartsdato)
			is EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold ->
				Endringsmelding.Innhold.EndreOppstartsdatoInnhold(this.oppstartsdato)
			is EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold ->
				Endringsmelding.Innhold.ForlengDeltakelseInnhold(this.sluttdato)
			is EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold ->
				Endringsmelding.Innhold.AvsluttDeltakelseInnhold(
					this.sluttdato, EndringsmeldingStatusAarsak(this.aarsak.type, this.aarsak.beskrivelse)
				)
			is EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold ->
				Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold(
					EndringsmeldingStatusAarsak(this.aarsak.type, this.aarsak.beskrivelse)
				)

			is EndringsmeldingDbo.Innhold.EndreDeltakelseProsentInnhold ->
				Endringsmelding.Innhold.EndreDeltakelseProsentInnhold(
					deltakelseProsent = this.nyDeltakelseProsent,
					dagerPerUke = this.dagerPerUke,
					gyldigFraDato = this.gyldigFraDato
				)
			is EndringsmeldingDbo.Innhold.EndreSluttdatoInnhold ->
				Endringsmelding.Innhold.EndreSluttdatoInnhold(this.sluttdato)

			is EndringsmeldingDbo.Innhold.EndreSluttaarsakInnhold ->
				Endringsmelding.Innhold.EndreSluttaarsakInnhold(this.aarsak)
		}
	}

	private fun opprettOgMarkerAktiveSomUtdatert(
		deltakerId: UUID, opprettetAvArrangorAnsattId: UUID, innhold: EndringsmeldingDbo.Innhold?, type: EndringsmeldingDbo.Type
	): UUID {
		val id = UUID.randomUUID()
		val aktiveEndringsmeldinger = endringsmeldingRepository.getAktive(deltakerId, type)
		transactionTemplate.executeWithoutResult {
			endringsmeldingRepository.markerAktiveSomUtdatert(deltakerId, type)
			endringsmeldingRepository.insert(id, deltakerId, opprettetAvArrangorAnsattId, type, innhold)
		}

		log.info("Endringsmelding av type ${type.name} opprettet med id $id for deltaker $deltakerId")
		aktiveEndringsmeldinger.forEach { publisherService.publish(it.id, DataPublishType.ENDRINGSMELDING, null) }
		publisherService.publish(id, DataPublishType.ENDRINGSMELDING, null)
		return id
	}

}


