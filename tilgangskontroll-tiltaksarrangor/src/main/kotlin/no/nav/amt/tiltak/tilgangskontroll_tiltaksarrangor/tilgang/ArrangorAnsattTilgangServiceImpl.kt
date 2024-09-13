package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorVeileder
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.ArrangorVeilederService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.MineDeltakerlisterService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.AmtArrangorService
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.arrangor.tilArrangorAnsattRoller
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.UUID

@Service
open class ArrangorAnsattTilgangServiceImpl(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val ansattRolleService: AnsattRolleService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val mineDeltakerlisterService: MineDeltakerlisterService,
	private val arrangorVeilederService: ArrangorVeilederService,
	private val arrangorService: ArrangorService,
	private val transactionTemplate: TransactionTemplate,
	private val amtArrangorService: AmtArrangorService,
) : ArrangorAnsattTilgangService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID) {
		val arrangorId = gjennomforingService.getArrangorId(gjennomforingId)

		if (!harKoordinatorTilgang(ansattId, gjennomforingId, arrangorId)){
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til gjennomføring med id=$gjennomforingId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ansatt har ikke tilgang til gjennomforing")
		}
	}

	override fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID) {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		val arrangorId = gjennomforingService.getArrangorId(deltaker.gjennomforingId)

		if (deltaker.harAdressebeskyttelse() && !harVeilederTilgang(ansattId, deltakerId, arrangorId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Arrangør ansatt med id:$ansattId har ikke tilgang til deltaker med id $deltakerId")
		} else if (!harKoordinatorTilgang(ansattId, deltaker.gjennomforingId, arrangorId) && !harVeilederTilgang(ansattId, deltakerId, arrangorId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Arrangør ansatt med id:$ansattId har ikke tilgang til deltaker med id $deltakerId")
		}
	}

	override fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller> {
		return ansattRolleService.hentAktiveRoller(ansattId)
	}

	override fun synkroniserRettigheter(ansattPersonligIdent: String) {
		try {
			val ansatt = amtArrangorService.getAnsatt(ansattPersonligIdent)
			if (ansatt == null) {
				log.warn("Fant ikke ansatt i amt-arrangor. Kan ikke oppdatere rettigheter.")
				return
			}
			if (ansatt.arrangorer.isEmpty()) {
				log.info("Ansatt ${ansatt.id} har ingen gyldige roller i amt-arrangor. Oppdaterer ikke rettigheter.")
				return
			}

			oppdaterRollerOgTilganger(ansatt)
		} catch (t: Throwable) {
			log.error("Feil under synkronisering av rettigheter", t)
			secureLog.error("Feil under synkronisering av rettigheter for fnr=$ansattPersonligIdent", t)
		}
	}

	override fun synkroniserRettigheter(ansattId: UUID) {
		try {
			val ansatt = amtArrangorService.getAnsatt(ansattId)
			if (ansatt == null || ansatt.arrangorer.isEmpty()) {
				log.error("Fant ikke ansatt med id $ansattId")
				return
			}
			oppdaterRollerOgTilganger(ansatt)
		} catch (t: Throwable) {
			log.error("Feil under synkronisering av rettigheter for ansatt med id $ansattId", t)
		}
	}

	override fun oppdaterRollerOgTilgangerForAnsatt(ansatt: ArrangorAnsatt) {
		oppdaterRollerOgTilganger(ansatt)
	}

	override fun verifiserRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		val hasRolle = harRolleHosArrangor(ansattId, arrangorId, rolle)

		if (!hasRolle) {
			secureLog.error("Ansatt ident: $ansattId har ikke $rolle rolle hos arrangør: $arrangorId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ansatt har ikke $rolle rolle. Se secure logs for deltaljer")
		}
	}

	override fun harRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle): Boolean {
		val ansatt = arrangorAnsattService.getAnsatt(ansattId)
		val tilgangerHosArrangor = ansatt.arrangorer.first {it.id == arrangorId}
		return tilgangerHosArrangor.roller.contains(rolle)
	}

	private fun harLagtTilGjennomforing(ansattId: UUID, gjennomforingId: UUID): Boolean {
		val gjennomforinger = mineDeltakerlisterService.hent(ansattId)
		return gjennomforinger.contains(gjennomforingId)
	}

	private fun oppdaterRollerOgTilganger(ansatt: ArrangorAnsatt) {
		arrangorAnsattService.upsertAnsatt(ansatt)

		oppdaterRoller(ansatt)

		oppdaterDeltakerlisteTilganger(ansatt)

		oppdaterVeilederTilganger(ansatt)

		arrangorAnsattService.setTilgangerSistSynkronisert(ansatt.id, LocalDateTime.now())
	}

	private fun oppdaterRoller(ansatt: ArrangorAnsatt) {
		val lagredeAnsattTilganger = ansattRolleService.hentAktiveRoller(ansatt.id)
			.flatMap { it.roller.map { r -> AnsattTilgang(it.arrangorId, r) } }

		val ansattTilganger = ansatt
			.tilArrangorAnsattRoller()
			.flatMap {
				val arrangor = arrangorService.getOrCreateArrangor(it.arrangor)

				return@flatMap it.roller.map { rolle -> AnsattTilgang(arrangor.id, rolle) }
			}

		val tilgangerSomSkalLeggesTil = finnTilgangerSomSkalLeggesTil(ansattTilganger, lagredeAnsattTilganger)
		val tilgangerSomSkalFjernes = finnTilgangerSomSkalFjernes(ansattTilganger, lagredeAnsattTilganger)

		tilgangerSomSkalLeggesTil.forEach {
			ansattRolleService.opprettRolle(UUID.randomUUID(), ansatt.id, it.arrangorId, it.arrangorAnsattRolle)
			log.info("La til ny tilgang. ansattId=${ansatt.id} arrangorId=${it.arrangorId} rolle=${it.arrangorAnsattRolle}")
		}
		tilgangerSomSkalFjernes.forEach { tilgang ->
			transactionTemplate.executeWithoutResult {
				ansattRolleService.deaktiverRolleHosArrangor(ansatt.id, tilgang.arrangorId, tilgang.arrangorAnsattRolle)
				when (tilgang.arrangorAnsattRolle) {
					ArrangorAnsattRolle.KOORDINATOR -> mineDeltakerlisterService.fjernAlleHosArrangor(
						ansatt.id,
						tilgang.arrangorId
					)

					ArrangorAnsattRolle.VEILEDER -> arrangorVeilederService.fjernAlleDeltakereForVeilederHosArrangor(
						ansatt.id,
						tilgang.arrangorId
					)
				}
			}
			log.info("Fjernet tilgang. ansattId=${ansatt.id} arrangorId=${tilgang.arrangorId} rolle=${tilgang.arrangorAnsattRolle}")
		}
	}

	private fun oppdaterDeltakerlisteTilganger(ansatt: ArrangorAnsatt) {
		val lagredeDeltakerlister = mineDeltakerlisterService.hent(ansatt.id)
		val deltakerlisterSomSkalLeggesTil = finnDeltakerlisterSomSkalLeggesTil(
			amtArrangorDeltakerlister = ansatt.arrangorer.flatMap { it.koordinator },
			lagredeDeltakerlister = lagredeDeltakerlister
		)
		val deltakerlisterSomSkalFjernes = finnDeltakerlisterSomSkalFjernes(
			amtArrangorDeltakerlister = ansatt.arrangorer.flatMap { it.koordinator },
			lagredeDeltakerlister = lagredeDeltakerlister
		)
		deltakerlisterSomSkalLeggesTil.forEach {
			mineDeltakerlisterService.leggTil(
				id = UUID.randomUUID(),
				arrangorAnsattId = ansatt.id,
				gjennomforingId = it
			)
		}
		deltakerlisterSomSkalFjernes.forEach {
			mineDeltakerlisterService.fjern(arrangorAnsattId = ansatt.id, gjennomforingId = it)
		}
		log.info("Lagt til ${deltakerlisterSomSkalLeggesTil.size} deltakerlister og fjernet ${deltakerlisterSomSkalFjernes.size} for ansatt ${ansatt.id}")
	}

	private fun oppdaterVeilederTilganger(ansatt: ArrangorAnsatt) {
		val lagretVeilederFor = arrangorVeilederService.hentDeltakereForVeileder(ansatt.id)
		val veilederKoblingerSomSkalLeggesTil = finnVeilederkoblingerSomSkalLeggesTil(
			lagredeVeilederkoblinger = lagretVeilederFor,
			amtArrangorVeiledere = ansatt.arrangorer.flatMap { it.veileder }
		)
		val veilederKoblingerSomSkalFjernes = finnVeilederkoblingerSomSkalFjernes(
			lagredeVeilederkoblinger = lagretVeilederFor,
			amtArrangorVeiledere = ansatt.arrangorer.flatMap { it.veileder }
		)
		veilederKoblingerSomSkalFjernes.forEach {
			arrangorVeilederService.fjernAnsattSomVeileder(
				ansattId = ansatt.id,
				deltakerId = it.deltakerId,
				erMedveileder = it.type == ArrangorAnsatt.VeilederType.MEDVEILEDER
			)
		}
		veilederKoblingerSomSkalLeggesTil.forEach {
			arrangorVeilederService.leggTilAnsattSomVeileder(
				ansattId = ansatt.id,
				deltakerId = it.deltakerId,
				erMedveileder = it.type == ArrangorAnsatt.VeilederType.MEDVEILEDER
			)
		}
		log.info("Lagt til ${veilederKoblingerSomSkalLeggesTil.size} og fjernet ${veilederKoblingerSomSkalFjernes.size} veileder-relasjoner for ansatt ${ansatt.id}")
	}

	private fun finnTilgangerSomSkalLeggesTil(
		altinnTilganger: List<AnsattTilgang>,
		lagredeTilganger: List<AnsattTilgang>
	): List<AnsattTilgang> {
		// Returnerer alle altinnTilganger som ikke finnes i lagredeTilganger
		return altinnTilganger.subtract(lagredeTilganger.toSet()).toList()
	}

	private fun finnTilgangerSomSkalFjernes(
		altinnTilganger: List<AnsattTilgang>,
		lagredeTilganger: List<AnsattTilgang>
	): List<AnsattTilgang> {
		// Returnerer alle lagredeTilganger som ikke finnes i altinnTilganger
		return lagredeTilganger.subtract(altinnTilganger.toSet()).toList()
	}

	private fun finnDeltakerlisterSomSkalLeggesTil(
		amtArrangorDeltakerlister: List<UUID>,
		lagredeDeltakerlister: List<UUID>
	): List<UUID> {
		return amtArrangorDeltakerlister.filter { id -> lagredeDeltakerlister.find { it == id } == null }
	}

	private fun finnDeltakerlisterSomSkalFjernes(
		amtArrangorDeltakerlister: List<UUID>,
		lagredeDeltakerlister: List<UUID>
	): List<UUID> {
		return lagredeDeltakerlister.filter { id -> amtArrangorDeltakerlister.find { it == id } == null }
	}

	private fun finnVeilederkoblingerSomSkalLeggesTil(
		lagredeVeilederkoblinger: List<ArrangorVeileder>,
		amtArrangorVeiledere: List<ArrangorAnsatt.VeilederDto>
	): List<ArrangorAnsatt.VeilederDto> {
		val lagredeVeiledere = lagredeVeilederkoblinger.map { it.toVeilederDto() }
		return amtArrangorVeiledere.filter { veileder ->
				lagredeVeiledere.find { it.deltakerId == veileder.deltakerId && it.type == veileder.type } == null
			}
	}

	private fun finnVeilederkoblingerSomSkalFjernes(
		lagredeVeilederkoblinger: List<ArrangorVeileder>,
		amtArrangorVeiledere: List<ArrangorAnsatt.VeilederDto>
	): List<ArrangorAnsatt.VeilederDto> {
		return lagredeVeilederkoblinger.map { it.toVeilederDto() }
			.filter { veileder ->
				amtArrangorVeiledere.find { it.deltakerId == veileder.deltakerId && it.type == veileder.type } == null
			}
	}

	private fun ArrangorVeileder.toVeilederDto(): ArrangorAnsatt.VeilederDto {
		return ArrangorAnsatt.VeilederDto(
			deltakerId = deltakerId,
			type = if (erMedveileder) {
				ArrangorAnsatt.VeilederType.MEDVEILEDER
			} else {
				ArrangorAnsatt.VeilederType.VEILEDER
			}
		)
	}

	private fun harKoordinatorTilgang(ansattId: UUID, gjennomforingId: UUID, arrangorId: UUID): Boolean {
		val tilgangTilArrangor = hentAnsattTilganger(ansattId)
			.find { it.arrangorId == arrangorId }
			?.roller
			?.contains(ArrangorAnsattRolle.KOORDINATOR)

		return tilgangTilArrangor == true && harLagtTilGjennomforing(ansattId, gjennomforingId)
	}

	private fun harVeilederTilgang(ansattId: UUID, deltakerId: UUID, arrangorId: UUID): Boolean {
		val tilgangTilArrangor = hentAnsattTilganger(ansattId)
			.find { it.arrangorId == arrangorId }
			?.roller
			?.contains(ArrangorAnsattRolle.VEILEDER)

		return tilgangTilArrangor == true && arrangorVeilederService.erVeilederFor(ansattId, deltakerId)
	}

	private data class AnsattTilgang(
		val arrangorId: UUID,
		val arrangorAnsattRolle: ArrangorAnsattRolle
	)
}
