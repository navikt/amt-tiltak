package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.GjennomforingPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GjennomforingProcessor(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
	private val navEnhetService: NavEnhetService
) : GenericProcessor<GjennomforingPayload>() {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun processInsertMessage(message: MessageWrapper<GjennomforingPayload>) {
		upsert(message)
	}

	override fun processModifyMessage(message: MessageWrapper<GjennomforingPayload>) {
		upsert(message)
	}

	private fun upsert(message: MessageWrapper<GjennomforingPayload>) {
		val gjennomforing = message.payload
		val tiltakPayload = gjennomforing.tiltak

		val arrangor = arrangorService.upsertArrangor(gjennomforing.virksomhetsnummer)
		val tiltak = tiltakService.upsertTiltak(tiltakPayload.id, tiltakPayload.navn, tiltakPayload.kode)
		val navEnhet = gjennomforing.ansvarligNavEnhetId?.let { navEnhetService.getNavEnhet(it) }

		gjennomforingService.upsert(
			Gjennomforing(
				id = gjennomforing.id,
				tiltak = tiltak,
				arrangor = arrangor,
				navn = gjennomforing.navn,
				status = mapGjennomforingStatus(gjennomforing.status),
				startDato = gjennomforing.startDato,
				sluttDato = gjennomforing.sluttDato,
				navEnhetId = navEnhet?.id,
				lopenr = gjennomforing.lopenr,
				opprettetAar = gjennomforing.opprettetAar
			)
		)

		log.info("Fullført upsert av gjennomføring id=${gjennomforing.id} arrangorId=${arrangor.id}")
	}

	private fun mapGjennomforingStatus(status: GjennomforingPayload.Status): Gjennomforing.Status {
		return when(status) {
			GjennomforingPayload.Status.IKKE_STARTET -> Gjennomforing.Status.IKKE_STARTET
			GjennomforingPayload.Status.GJENNOMFORES -> Gjennomforing.Status.GJENNOMFORES
			GjennomforingPayload.Status.AVSLUTTET -> Gjennomforing.Status.AVSLUTTET
		}
	}

	override fun processDeleteMessage(message: MessageWrapper<GjennomforingPayload>) {
		val gjennomforingId = message.payload.id

		log.info("Motatt delete-melding, sletter gjennomføring med id=$gjennomforingId")

		gjennomforingService.slettGjennomforing(gjennomforingId)
	}

}
