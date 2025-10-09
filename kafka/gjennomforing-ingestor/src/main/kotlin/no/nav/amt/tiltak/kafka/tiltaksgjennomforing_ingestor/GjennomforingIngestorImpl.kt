package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GjennomforingIngestorImpl(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val tiltakService: TiltakService
): GjennomforingIngestor {

	private val stottedeTiltak = setOf(
		"INDOPPFAG",
		"ARBFORB",
		"AVKLARAG",
		"VASV",
		"ARBRRHDAG",
		"DIGIOPPARB",
		"JOBBK",
		"GRUPPEAMO",
		"GRUFAGYRKE",
		"ENKELAMO",
		"ENKFAGYRKE",
		"HOYEREUTD",
	)

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaRecord(gjennomforingId: String, recordValue: String?) {
		if (recordValue == null) {
			val id = UUID.fromString(gjennomforingId)
			log.info("Mottok melding om å slette gjennomføring med id $id")
			deltakerService.slettDeltakerePaaGjennomforing(id)
			gjennomforingService.slettGjennomforing(id)
			return
		}
		val gjennomforing = fromJsonString<GjennomforingMessage>(recordValue)

		if (!stottedeTiltak.contains(gjennomforing.tiltakstype.arenaKode)) {
			log.info("Lagrer ikke gjennomføring med id ${gjennomforing.id} og tiltakstype ${gjennomforing.tiltakstype.arenaKode} fordi tiltaket ikke er støttet.")
			return
		}

		val arrangor = arrangorService.upsertArrangor(gjennomforing.virksomhetsnummer)

		val tiltak = tiltakService.upsertTiltak(
			gjennomforing.tiltakstype.id,
			gjennomforing.tiltakstype.navn,
			gjennomforing.tiltakstype.arenaKode
		)

		gjennomforingService.upsert(
			GjennomforingUpsert(
				id = gjennomforing.id,
				tiltakId = tiltak.id,
				arrangorId = arrangor.id,
				navn = gjennomforing.navn,
				status = GjennomforingStatusConverter.convert(gjennomforing.status.name),
				startDato = gjennomforing.startDato,
				sluttDato = gjennomforing.sluttDato,
				lopenr = null,
				opprettetAar = null,
				erKurs = gjennomforing.erKurs()
			)
		)
		if (gjennomforing.status == GjennomforingMessage.Status.AVLYST || gjennomforing.status == GjennomforingMessage.Status.AVBRUTT) {
			deltakerService.avsluttDeltakerePaaAvbruttGjennomforing(gjennomforing.id)
		}
		log.info("Fullført upsert av gjennomføring id=${gjennomforing.id} arrangorId=${arrangor.id}")
	}
}
