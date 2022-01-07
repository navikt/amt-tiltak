package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Gjennomforing
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.springframework.stereotype.Service

@Service
class GjennomforingProcessor(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
) : GenericProcessor<Gjennomforing>() {

	override fun processInsertMessage(message: MessageWrapper<Gjennomforing>) {
		upsert(message)
	}

	override fun processModifyMessage(message: MessageWrapper<Gjennomforing>) {
		upsert(message)
	}

	override fun processDeleteMessage(message: MessageWrapper<Gjennomforing>) {
		TODO("Not yet implemented")
	}

	private fun upsert(message: MessageWrapper<Gjennomforing>) {
		val gjennomforing = message.payload
		val tiltak = gjennomforing.tiltak

		val arrangor = arrangorService.upsertArrangor(gjennomforing.virksomhetsnummer)

		tiltakService.upsertTiltak(tiltak.id, tiltak.navn, tiltak.kode)

		gjennomforingService.upsertGjennomforing(
			id = gjennomforing.id,
			tiltakId = gjennomforing.tiltak.id,
			arrangorId = arrangor.id,
			navn = gjennomforing.navn,
			status = null,
			oppstartDato = gjennomforing.oppstartDato,
			sluttDato = gjennomforing.sluttDato,
			registrertDato = gjennomforing.registrert,
			fremmoteDato = gjennomforing.fremmote
		)
	}

}
