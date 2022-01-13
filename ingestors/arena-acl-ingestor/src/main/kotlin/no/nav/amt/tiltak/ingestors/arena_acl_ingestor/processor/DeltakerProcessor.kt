package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Deltaker
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.springframework.stereotype.Service

@Service
class DeltakerProcessor(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
) : GenericProcessor<Deltaker>() {

	override fun processInsertMessage(message: MessageWrapper<Deltaker>) {
		upsert(message)
	}

	override fun processModifyMessage(message: MessageWrapper<Deltaker>) {
		upsert(message)
	}

	override fun processDeleteMessage(message: MessageWrapper<Deltaker>) {
		TODO("Not yet implemented")
	}

	private fun upsert(message: MessageWrapper<Deltaker>) {
		val deltaker = message.payload

		val tiltaksgjennomforing = gjennomforingService.getGjennomforing(deltaker.gjennomforingId)

		deltakerService.upsertDeltaker(
			id = deltaker.id,
			gjennomforingId = tiltaksgjennomforing.id,
			fodselsnummer = deltaker.personIdent,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			status = tilDeltakerStatus(deltaker.status),
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentDeltid,
			registrertDato = deltaker.registrertDato
		)
	}

	private fun tilDeltakerStatus(status: Deltaker.Status): no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status {
		return when(status){
			Deltaker.Status.VENTER_PA_OPPSTART -> no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.VENTER_PA_OPPSTART
			Deltaker.Status.DELTAR -> no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.DELTAR
			Deltaker.Status.HAR_SLUTTET -> no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.HAR_SLUTTET
			Deltaker.Status.IKKE_AKTUELL -> no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.IKKE_AKTUELL
		}
	}

}
