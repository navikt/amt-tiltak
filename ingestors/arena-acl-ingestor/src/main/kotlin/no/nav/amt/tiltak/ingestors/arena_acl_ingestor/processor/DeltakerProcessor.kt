package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeltakerProcessor(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val personService: PersonService
) : GenericProcessor<DeltakerPayload>() {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun processInsertMessage(message: MessageWrapper<DeltakerPayload>) {
		upsert(message)
	}

	override fun processModifyMessage(message: MessageWrapper<DeltakerPayload>) {
		upsert(message)
	}

	private fun upsert(message: MessageWrapper<DeltakerPayload>) {
		val deltakerDto = message.payload
		val deltakerFnr = message.payload.personIdent

		if (deltakerDto.status == DeltakerPayload.Status.FEILREGISTRERT) {
			log.info("Sletter deltaker med id=${deltakerDto.id} som er feilregistrert")
			deltakerService.slettDeltaker(deltakerDto.id)
			return
		}

		val person = personService.hentPerson(deltakerFnr)

		if (person.diskresjonskode != null) {
			log.info("Deltaker id=${deltakerDto.id} har diskresjonskode ${person.diskresjonskode} og skal filtreres ut")
			return
		}

		val tiltaksgjennomforing = gjennomforingService.getGjennomforing(deltakerDto.gjennomforingId)

		val deltaker = Deltaker(
			id = deltakerDto.id,
			startDato = deltakerDto.startDato,
			sluttDato = deltakerDto.sluttDato,
			statuser = DeltakerStatuser.settAktivStatus(
				tilDeltakerStatus(deltakerDto.status),
				endretDato = deltakerDto.statusEndretDato
			),
			dagerPerUke = deltakerDto.dagerPerUke,
			prosentStilling = deltakerDto.prosentDeltid,
			registrertDato = deltakerDto.registrertDato
		)

		deltakerService.upsertDeltaker(
			fodselsnummer =  deltakerDto.personIdent,
			gjennomforingId = tiltaksgjennomforing.id,
			deltaker = deltaker,
		)

		log.info("Fullført upsert av deltaker id=${deltaker.id} gjennomforingId=${tiltaksgjennomforing.id}")
	}

	private fun tilDeltakerStatus(status: DeltakerPayload.Status): Deltaker.Status {
		return when(status){
			DeltakerPayload.Status.VENTER_PA_OPPSTART -> Deltaker.Status.VENTER_PA_OPPSTART
			DeltakerPayload.Status.DELTAR -> Deltaker.Status.DELTAR
			DeltakerPayload.Status.HAR_SLUTTET -> Deltaker.Status.HAR_SLUTTET
			DeltakerPayload.Status.IKKE_AKTUELL -> Deltaker.Status.IKKE_AKTUELL
			DeltakerPayload.Status.FEILREGISTRERT -> Deltaker.Status.FEILREGISTRERT
		}
	}

	override fun processDeleteMessage(message: MessageWrapper<DeltakerPayload>) {
		val deltakerId = message.payload.id

		log.info("Motatt delete-melding, sletter deltaker med id=$deltakerId")

		deltakerService.slettDeltaker(deltakerId)
	}

}
