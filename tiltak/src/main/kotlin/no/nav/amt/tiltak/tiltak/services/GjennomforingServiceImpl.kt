package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GjennomforingServiceImpl(
	private val gjennomforingRepository: GjennomforingRepository,
	private val tiltakService: TiltakService,
	private val arrangorService: ArrangorService,
	@Lazy private val deltakerService: DeltakerService,
	private val amtArrangorClient: AmtArrangorClient,
) : GjennomforingService {

	private val log = LoggerFactory.getLogger(javaClass)

	// Noen gjennomføringer skal aldri bytte arrangør, ref https://confluence.adeo.no/pages/viewpage.action?pageId=535926750
	private val gjennomforingerSomIkkeSkalBytteArrangor = listOf(
		UUID.fromString("f5d4089b-a656-457a-85d8-2e74588902e2"),
		UUID.fromString("c0c5e06e-9b93-4ddc-a60b-de397b442ece")
	)

	override fun upsert(gjennomforing: GjennomforingUpsert) {
		val storedGjennomforing = gjennomforingRepository.get(gjennomforing.id)

		if (storedGjennomforing != null) {
			updateGjennomforing(storedGjennomforing, gjennomforing)
		} else {
			gjennomforingRepository.insert(gjennomforing)
		}
	}

	private fun updateGjennomforing(
		storedGjennomforing: GjennomforingDbo,
		updatedGjennomforing: GjennomforingUpsert,
	) {
		val arrangorId = if (storedGjennomforing.id in gjennomforingerSomIkkeSkalBytteArrangor) {
			log.info("Oppdaterer ikke arrangørId for svartelistet gjennomføring med id ${storedGjennomforing.id}")
			storedGjennomforing.arrangorId
		} else {
			if (updatedGjennomforing.arrangorId != storedGjennomforing.arrangorId) {
				val deltakere = deltakerService.hentDeltakerePaaGjennomforing(updatedGjennomforing.id)

				amtArrangorClient.fjernTilganger(
					arrangorId = storedGjennomforing.arrangorId,
					gjennomforingId = updatedGjennomforing.id,
					deltakerIder = deltakere.map { it.id },
				)
			}
			updatedGjennomforing.arrangorId
		}
		val update = storedGjennomforing.update(
			storedGjennomforing.copy(
				navn = updatedGjennomforing.navn,
				arrangorId = arrangorId,
				status = updatedGjennomforing.status,
				startDato = updatedGjennomforing.startDato,
				sluttDato = updatedGjennomforing.sluttDato,
				navEnhetId = updatedGjennomforing.navEnhetId,
				lopenr = updatedGjennomforing.lopenr,
				opprettetAar = updatedGjennomforing.opprettetAar,
				erKurs = updatedGjennomforing.erKurs
			)
		)

		if (update.status == UpdateStatus.UPDATED) {
			gjennomforingRepository.update(update.updatedObject!!)
		}
	}

	override fun slettGjennomforing(gjennomforingId: UUID) {
		gjennomforingRepository.delete(gjennomforingId)
		log.info("Gjennomføring med id=$gjennomforingId er slettet")
	}

	override fun getGjennomforing(id: UUID): Gjennomforing {
		return getGjennomforingOrNull(id) ?: throw NoSuchElementException("Fant ikke gjennomforing: $id")
	}

	override fun getGjennomforingOrNull(id: UUID): Gjennomforing? {
		return gjennomforingRepository.get(id)?.let { gjennomforingDbo ->
			val (tiltak, arrangor) = getTiltakOgArrangor(gjennomforingDbo.tiltakId, gjennomforingDbo.arrangorId)
			return@let gjennomforingDbo.toGjennomforing(tiltak, arrangor)
		}
	}

	override fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing> {
		return gjennomforingRepository.getGjennomforingWithArrangorAndTiltak(gjennomforingIder)
	}

	override fun getByArrangorId(arrangorId: UUID): List<Gjennomforing> {
		return gjennomforingRepository.getByArrangorId(arrangorId).map {
			val (tiltak, arrangor) = getTiltakOgArrangor(it.tiltakId, it.arrangorId)
			return@map it.toGjennomforing(tiltak, arrangor)
		}
	}

	override fun getArrangorId(gjennomforingId: UUID): UUID {
		return gjennomforingRepository.get(gjennomforingId)?.arrangorId ?: throw IllegalStateException("Fant ikke gjennomføring med id $gjennomforingId")
	}

	override fun getByLopenr(lopenr: Int): List<Gjennomforing> {
		return gjennomforingRepository.getByLopenr(lopenr)
			.map {
				val (tiltak, arrangor) = getTiltakOgArrangor(it.tiltakId, it.arrangorId)
				return@map it.toGjennomforing(tiltak, arrangor)
			}
	}

	private fun getTiltakOgArrangor(tiltakId: UUID, arrangorId: UUID): Pair<Tiltak, Arrangor> {
		val tiltak = tiltakService.getTiltakById(tiltakId)
		val arrangor = arrangorService.getArrangorById(arrangorId)
		return Pair(tiltak, arrangor)
	}

}
