package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.springframework.stereotype.Service

@Service
class GjennomforingProcessor(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
) : GenericProcessor<GjennomforingPayload>() {

	override fun processInsertMessage(message: MessageWrapper<GjennomforingPayload>) {
		upsert(message)
	}

	override fun processModifyMessage(message: MessageWrapper<GjennomforingPayload>) {
		upsert(message)
	}

	private fun upsert(message: MessageWrapper<GjennomforingPayload>) {
		val gjennomforing = message.payload
		val tiltak = gjennomforing.tiltak

		val arrangor = arrangorService.upsertArrangor(gjennomforing.virksomhetsnummer)

		tiltakService.upsertTiltak(tiltak.id, tiltak.navn, tiltak.kode)

		gjennomforingService.upsertGjennomforing(
			id = gjennomforing.id,
			tiltakId = gjennomforing.tiltak.id,
			arrangorId = arrangor.id,
			navn = gjennomforing.navn,
			status = mapGjennomforingStatus(gjennomforing.status),
			startDato = gjennomforing.startDato,
			sluttDato = gjennomforing.sluttDato,
			registrertDato = gjennomforing.registrertDato,
			fremmoteDato = gjennomforing.fremmoteDato
		)
	}

	private fun mapGjennomforingStatus(status: GjennomforingPayload.Status): Gjennomforing.Status {
		return when(status) {
			GjennomforingPayload.Status.IKKE_STARTET -> Gjennomforing.Status.IKKE_STARTET
			GjennomforingPayload.Status.GJENNOMFORES -> Gjennomforing.Status.GJENNOMFORES
			GjennomforingPayload.Status.AVSLUTTET -> Gjennomforing.Status.AVSLUTTET
		}
	}

}
