package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Service
class DeltakerProcessor(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val personService: PersonService,
	private val transactionTemplate: TransactionTemplate
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

		val status = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltakerDto.id,
			type = tilDeltakerStatusType(deltakerDto.status),
			aarsak = tilDeltakerAarsak(deltakerDto.statusAarsak),
			gyldigFra = deltakerDto.statusEndretDato,
		)

		val deltakerUpsert = DeltakerUpsert(
			id = deltakerDto.id,
			statusInsert = status,
			startDato = deltakerDto.startDato,
			sluttDato = deltakerDto.sluttDato,
			dagerPerUke = deltakerDto.dagerPerUke,
			prosentStilling = deltakerDto.prosentDeltid,
			registrertDato = deltakerDto.registrertDato,
			gjennomforingId = tiltaksgjennomforing.id,
			innsokBegrunnelse = deltakerDto.innsokBegrunnelse
		)

		transactionTemplate.executeWithoutResult {
			deltakerService.upsertDeltaker(deltakerDto.personIdent, deltakerUpsert)

			/*
			 	Deltakere blir noen ganger gjenbrukt istedenfor at det opprettes en ny,
			 	eller NAV har bestemt at deltakeren skal på tiltaket selv om tiltaksarrangøren har skjult deltakeren.
			 	I disse tilfellene så må vi oppheve at deltakeren skjules.
			*/
			if (
				deltakerService.erSkjultForTiltaksarrangor(deltakerUpsert.id)
				&& !deltakerService.kanDeltakerSkjulesForTiltaksarrangor(deltakerUpsert.id)
			) {
				deltakerService.opphevSkjulDeltakerForTiltaksarrangor(deltakerUpsert.id)
			}
		}

		log.info("Fullført upsert av deltaker id=${deltakerUpsert.id} gjennomforingId=${tiltaksgjennomforing.id}")
	}

	private fun tilDeltakerStatusType(status: DeltakerPayload.Status): DeltakerStatus.Type {
		return when(status){
			DeltakerPayload.Status.VENTER_PA_OPPSTART -> DeltakerStatus.Type.VENTER_PA_OPPSTART
			DeltakerPayload.Status.DELTAR -> DeltakerStatus.Type.DELTAR
			DeltakerPayload.Status.HAR_SLUTTET -> DeltakerStatus.Type.HAR_SLUTTET
			DeltakerPayload.Status.IKKE_AKTUELL -> DeltakerStatus.Type.IKKE_AKTUELL
			DeltakerPayload.Status.FEILREGISTRERT -> DeltakerStatus.Type.FEILREGISTRERT
			DeltakerPayload.Status.PABEGYNT -> DeltakerStatus.Type.PABEGYNT
		}
	}

	private fun tilDeltakerAarsak(aarsak: DeltakerPayload.StatusAarsak?): DeltakerStatus.Aarsak? {
		return when(aarsak){
			DeltakerPayload.StatusAarsak.SYK -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.SYK)
			DeltakerPayload.StatusAarsak.FATT_JOBB -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.FATT_JOBB)
			DeltakerPayload.StatusAarsak.TRENGER_ANNEN_STOTTE -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.TRENGER_ANNEN_STOTTE)
			DeltakerPayload.StatusAarsak.FIKK_IKKE_PLASS -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.FIKK_IKKE_PLASS)
			DeltakerPayload.StatusAarsak.UTDANNING -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.UTDANNING)
			DeltakerPayload.StatusAarsak.FERDIG -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.FERDIG)
			DeltakerPayload.StatusAarsak.AVLYST_KONTRAKT -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.AVLYST_KONTRAKT)
			DeltakerPayload.StatusAarsak.IKKE_MOTT -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.IKKE_MOTT)
			DeltakerPayload.StatusAarsak.FEILREGISTRERT -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.FEILREGISTRERT)
			DeltakerPayload.StatusAarsak.ANNET -> DeltakerStatus.Aarsak(DeltakerStatus.Aarsak.Type.ANNET, null)
			else -> null
		}
	}

	override fun processDeleteMessage(message: MessageWrapper<DeltakerPayload>) {
		val deltakerId = message.payload.id

		log.info("Motatt delete-melding, sletter deltaker med id=$deltakerId")

		deltakerService.slettDeltaker(deltakerId)
	}

}
