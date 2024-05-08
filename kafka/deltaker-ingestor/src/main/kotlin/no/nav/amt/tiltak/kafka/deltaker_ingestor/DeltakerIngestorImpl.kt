package no.nav.amt.tiltak.kafka.deltaker_ingestor

import io.getunleash.Unleash
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.Gjennomforing
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.MulighetsrommetApiClient
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.kafka.DeltakerIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor.GjennomforingStatusConverter
import no.nav.common.utils.EnvironmentUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class DeltakerIngestorImpl(
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val arrangorService: ArrangorService,
	private val tiltakService: TiltakService,
	private val mulighetsrommetApiClient: MulighetsrommetApiClient,
	private val transactionTemplate: TransactionTemplate,
	private val unleashClient: Unleash
) : DeltakerIngestor {
	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingest(key: String, value: String?) {
		if (unleashClient.isEnabled("amt.enable-komet-deltakere")) {
			val deltakerId = UUID.fromString(key)
			if (value == null) {
				val deltaker = deltakerService.hentDeltaker(deltakerId)
				if (deltaker?.kilde == Kilde.KOMET) {
					deltakerService.slettDeltaker(deltakerId, Kilde.KOMET)
					log.info("Slettet deltaker fra ny løsning med id $deltakerId")
				}
			} else {
				val deltakerDto: DeltakerDto = fromJsonString(value)
				if (deltakerDto.kilde == Kilde.KOMET) {
					upsert(deltakerDto)
					log.info("Håndterte deltaker fra ny løsning med id $deltakerId")
				}
			}
		}
	}

	private fun upsert(deltakerDto: DeltakerDto) {
		val gjennomforingId = gjennomforingService.getGjennomforingOrNull(deltakerDto.deltakerlisteId)?.id
			?: try {
				upsertGjennomforing(deltakerDto.deltakerlisteId).id
			} catch (e: IllegalStateException) {
				if (EnvironmentUtils.isDevelopment().orElse(false)) {
					log.warn("Ignorerer deltaker med id ${deltakerDto.id} på gjennomføring ${deltakerDto.deltakerlisteId} i dev: ${e.message}")
					return
				} else {
					throw e
				}
			}

		val status = DeltakerStatusInsert(
			id = deltakerDto.status.id ?: UUID.randomUUID(),
			deltakerId = deltakerDto.id,
			type = deltakerDto.status.type,
			aarsak = deltakerDto.status.aarsak,
			aarsaksbeskrivelse = deltakerDto.status.aarsaksbeskrivelse,
			gyldigFra = deltakerDto.status.gyldigFra,
		)

		val deltakerUpsert = DeltakerUpsert(
			id = deltakerDto.id,
			statusInsert = status,
			startDato = deltakerDto.oppstartsdato,
			sluttDato = deltakerDto.sluttdato,
			dagerPerUke = deltakerDto.dagerPerUke,
			prosentStilling = deltakerDto.prosentStilling?.toFloat(),
			registrertDato = deltakerDto.innsoktDato.atStartOfDay(),
			gjennomforingId = gjennomforingId,
			innsokBegrunnelse = deltakerDto.bestillingTekst,
			innhold = deltakerDto.innhold,
			kilde = Kilde.KOMET,
			forsteVedtakFattet = deltakerDto.forsteVedtakFattet,
			historikk = deltakerDto.historikk,
			sistEndretAv = deltakerDto.sistEndretAv,
			sistEndretAvEnhet = deltakerDto.sistEndretAvEnhet
		)

		transactionTemplate.executeWithoutResult {
			deltakerService.upsertDeltaker(deltakerDto.personalia.personident, deltakerUpsert)
		}
		log.info("Fullført upsert av deltaker id=${deltakerUpsert.id} deltakerlisteId=${gjennomforingId} fra ny løsning")
	}

	private fun upsertGjennomforing(gjennomforingId: UUID): Gjennomforing {
		val gjennomforing = mulighetsrommetApiClient.hentGjennomforing(gjennomforingId)
		val gjennomforingArenaData = mulighetsrommetApiClient.hentGjennomforingArenaData(gjennomforingId)
			?: throw IllegalStateException("Lagrer ikke gjennomføring med id ${gjennomforing.id} som er opprettet utenfor Arena")

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
				lopenr = gjennomforingArenaData.lopenr,
				opprettetAar = gjennomforingArenaData.opprettetAar,
				erKurs = gjennomforing.erKurs()
			)
		)
		return gjennomforing
	}
}
