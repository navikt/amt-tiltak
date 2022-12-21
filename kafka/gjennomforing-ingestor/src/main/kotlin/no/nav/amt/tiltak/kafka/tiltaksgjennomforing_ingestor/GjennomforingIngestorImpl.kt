package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.clients.mr_arena_adapter_client.MrArenaAdapterClient
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.TiltakService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GjennomforingIngestorImpl(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
	private val navEnhetService: NavEnhetService,
	private val mrArenaAdapterClient: MrArenaAdapterClient,
): GjennomforingIngestor {

	private val log = LoggerFactory.getLogger(javaClass)
	override fun ingestKafkaRecord(recordValue: String) {
		val gjennomforing = fromJsonString<GjennomforingMessage>(recordValue)

		val arenaData = mrArenaAdapterClient.hentGjennomforingArenaData(gjennomforing.id)

		val arrangor = arrangorService.upsertArrangor(arenaData.virksomhetsnummer)

		val tiltak = tiltakService.upsertTiltak(
			gjennomforing.tiltak.id,
			gjennomforing.tiltak.navn,
			gjennomforing.tiltak.arenaKode
		)

		val navEnhet = arenaData.ansvarligNavEnhetId.let { navEnhetService.getNavEnhet(it) }

		gjennomforingService.upsert(
			GjennomforingUpsert(
				id = gjennomforing.id,
				tiltakId = tiltak.id,
				arrangorId = arrangor.id,
				navn = gjennomforing.navn,
				status = GjennomforingStatusConverter.convert(arenaData.status),
				startDato = gjennomforing.startDato,
				sluttDato = gjennomforing.sluttDato,
				navEnhetId = navEnhet?.id,
				lopenr = arenaData.lopenr,
				opprettetAar = arenaData.opprettetAar,
				deprecated = false,
			)
		)

		log.info("Fullført upsert av gjennomføring id=${gjennomforing.id} arrangorId=${arrangor.id}")
	}
}
