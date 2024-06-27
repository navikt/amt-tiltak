package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processor

import io.getunleash.Unleash
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.Gjennomforing
import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.MulighetsrommetApiClient
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.DeltakerPayload
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor.GjennomforingStatusConverter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class DeltakerProcessor(
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService,
	private val arrangorService: ArrangorService,
	private val tiltakService: TiltakService,
	private val mulighetsrommetApiClient: MulighetsrommetApiClient,
	private val transactionTemplate: TransactionTemplate,
	private val unleashClient: Unleash
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

		val gjennomforing = gjennomforingService.getGjennomforingOrNull(deltakerDto.gjennomforingId)
		val gjennomforingFraMulighetsrommet = if (gjennomforing == null) {
			upsertGjennomforing(deltakerDto.gjennomforingId)
		} else {
			null
		}

		val gjennomforingId = gjennomforing?.id ?: gjennomforingFraMulighetsrommet!!.id
		val tiltakstype = gjennomforing?.tiltak?.kode ?: gjennomforingFraMulighetsrommet!!.tiltakstype.arenaKode

		if (unleashClient.isEnabled("amt.enable-komet-deltakere") && tiltakstype == "ARBFORB") {
			log.info("Ignorerer deltaker på tiltak som komet er master for, id ${deltakerDto.id}")
		}

		val status = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltakerDto.id,
			type = tilDeltakerStatusType(deltakerDto.status),
			aarsak = tilDeltakerAarsak(deltakerDto.statusAarsak),
			aarsaksbeskrivelse = null,
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
			gjennomforingId = gjennomforingId,
			innsokBegrunnelse = deltakerDto.innsokBegrunnelse,
			innhold = null,
			kilde = Kilde.ARENA,
			forsteVedtakFattet = null,
			historikk = null,
			sistEndretAv = null,
			sistEndretAvEnhet = null
		)

		transactionTemplate.executeWithoutResult {
			deltakerService.upsertDeltaker(deltakerDto.personIdent, deltakerUpsert)
		}

		log.info("Fullført upsert av deltaker id=${deltakerUpsert.id} gjennomforingId=${gjennomforingId}")
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
	private fun tilDeltakerStatusType(status: DeltakerPayload.Status): DeltakerStatus.Type {
		return when(status){
			DeltakerPayload.Status.VENTER_PA_OPPSTART -> DeltakerStatus.Type.VENTER_PA_OPPSTART
			DeltakerPayload.Status.DELTAR -> DeltakerStatus.Type.DELTAR
			DeltakerPayload.Status.HAR_SLUTTET -> DeltakerStatus.Type.HAR_SLUTTET
			DeltakerPayload.Status.IKKE_AKTUELL -> DeltakerStatus.Type.IKKE_AKTUELL
			DeltakerPayload.Status.FEILREGISTRERT -> DeltakerStatus.Type.FEILREGISTRERT
			DeltakerPayload.Status.PABEGYNT -> DeltakerStatus.Type.PABEGYNT_REGISTRERING
			DeltakerPayload.Status.PABEGYNT_REGISTRERING -> DeltakerStatus.Type.PABEGYNT_REGISTRERING
			DeltakerPayload.Status.SOKT_INN -> DeltakerStatus.Type.SOKT_INN
			DeltakerPayload.Status.VURDERES -> DeltakerStatus.Type.VURDERES
			DeltakerPayload.Status.VENTELISTE -> DeltakerStatus.Type.VENTELISTE
			DeltakerPayload.Status.AVBRUTT -> DeltakerStatus.Type.AVBRUTT
			DeltakerPayload.Status.FULLFORT -> DeltakerStatus.Type.FULLFORT
		}
	}

	private fun tilDeltakerAarsak(aarsak: DeltakerPayload.StatusAarsak?): DeltakerStatus.Aarsak? {
		return when(aarsak){
			DeltakerPayload.StatusAarsak.SYK -> DeltakerStatus.Aarsak.SYK
			DeltakerPayload.StatusAarsak.FATT_JOBB -> DeltakerStatus.Aarsak.FATT_JOBB
			DeltakerPayload.StatusAarsak.TRENGER_ANNEN_STOTTE -> DeltakerStatus.Aarsak.TRENGER_ANNEN_STOTTE
			DeltakerPayload.StatusAarsak.FIKK_IKKE_PLASS -> DeltakerStatus.Aarsak.FIKK_IKKE_PLASS
			DeltakerPayload.StatusAarsak.AVLYST_KONTRAKT -> DeltakerStatus.Aarsak.SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT
			DeltakerPayload.StatusAarsak.IKKE_MOTT -> DeltakerStatus.Aarsak.IKKE_MOTT
			DeltakerPayload.StatusAarsak.ANNET -> DeltakerStatus.Aarsak.ANNET
			else -> null
		}
	}

	override fun processDeleteMessage(message: MessageWrapper<DeltakerPayload>) {
		val deltakerId = message.payload.id

		log.info("Motatt delete-melding, sletter deltaker med id=$deltakerId")

		deltakerService.slettDeltaker(deltakerId, Kilde.ARENA)
	}

}
