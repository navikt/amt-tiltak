package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.MulighetsrommetApiClient
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.TiltakService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class GjennomforingIngestorImpl(
	private val arrangorService: ArrangorService,
	private val gjennomforingService: GjennomforingService,
	private val tiltakService: TiltakService,
	private val navEnhetService: NavEnhetService,
	private val mulighetsrommetApiClient: MulighetsrommetApiClient,
): GjennomforingIngestor {

	private val log = LoggerFactory.getLogger(javaClass)
	override fun ingestKafkaRecord(gjennomforingId: String, recordValue: String?) {
		if (recordValue == null) {
			val id = UUID.fromString(gjennomforingId)
			log.info("Mottok melding om å slette gjennomføring med id $id")
			gjennomforingService.slettGjennomforing(id)
			return
		}
		val gjennomforing = fromJsonString<GjennomforingMessage>(recordValue)

		val arenaData = mulighetsrommetApiClient.hentGjennomforingArenaData(gjennomforing.id)

		if (arenaData.virksomhetsnummer == null) {
			log.error("Lagrer ikke gjennomføring med id ${gjennomforing.id} og tiltakstype ${gjennomforing.tiltakstype.arenaKode} fordi virksomhetsnummer mangler.")
			return
		}

		val arrangor = arrangorService.upsertArrangor(arenaData.virksomhetsnummer!!)

		val tiltak = tiltakService.upsertTiltak(
			gjennomforing.tiltakstype.id,
			gjennomforing.tiltakstype.navn,
			gjennomforing.tiltakstype.arenaKode
		)

		val navEnhet = arenaData.ansvarligNavEnhetId.let { navEnhetService.getNavEnhet(it) }

		gjennomforingService.upsert(
			GjennomforingUpsert(
				id = gjennomforing.id,
				tiltakId = tiltak.id,
				arrangorId = arrangor.id,
				navn = gjennomforing.navn ?: "",
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
