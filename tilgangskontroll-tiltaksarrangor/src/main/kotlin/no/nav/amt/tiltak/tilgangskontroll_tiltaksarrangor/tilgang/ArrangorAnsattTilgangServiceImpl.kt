package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.altinn.AltinnService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*

@Service
open class ArrangorAnsattTilgangServiceImpl(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val ansattRolleService: AnsattRolleService,
	private val deltakerService: DeltakerService,
	private val gjennomforingService: GjennomforingService,
	private val altinnService: AltinnService,
	private val mineDeltakerlisterService: MineDeltakerlisterService,
	private val arrangorVeilederService: ArrangorVeilederService,
	private val arrangorService: ArrangorService,
	private val transactionTemplate: TransactionTemplate,
) : ArrangorAnsattTilgangService {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID) {
		val gjennomforing = gjennomforingService.getGjennomforing(gjennomforingId)

		if (!harKoordinatorTilgang(ansattId, gjennomforingId, gjennomforing.arrangor.id)){
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til gjennomføring med id=$gjennomforingId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ansatt har ikke tilgang til gjennomforing")
		}
	}

	override fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID) {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		val gjennomforing = gjennomforingService.getGjennomforing(deltaker.gjennomforingId)

		if(!harKoordinatorTilgang(ansattId, deltaker.gjennomforingId, gjennomforing.arrangor.id)
			&& !harVeilederTilgang(ansattId, deltakerId, gjennomforing.arrangor.id)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Arrangør ansatt med id:$ansattId har ikke tilgang til deltaker med id $deltakerId")
		}

	}

	override fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller> {
		return ansattRolleService.hentAktiveRoller(ansattId)
	}

	override fun synkroniserRettigheterMedAltinn(ansattPersonligIdent: String) {
		try {
			synkroniserAltinnRettigheter(ansattPersonligIdent)
		} catch (t: Throwable) {
			log.error("Feil under synkronisering av altinn rettigheter", t)
			secureLog.error("Feil under synkronisering av altinn rettigheter for fnr=$ansattPersonligIdent", t)
		}
	}

	override fun verifiserRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		val hasRolle = harRolleHosArrangor(ansattId, arrangorId, rolle)

		if (!hasRolle) {
			secureLog.error("Ansatt ident: $ansattId har ikke $rolle rolle hos arrangør: $arrangorId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ansatt har ikke $rolle rolle. Se secure logs for deltaljer")
		}
	}

	override fun verifiserHarRolleAnywhere(ansattId: UUID, rolle: ArrangorAnsattRolle) {
		val hasRolle = hentRoller(ansattId).contains(rolle)

		if (!hasRolle) {
			secureLog.error("Ansatt ident: $ansattId har ikke $rolle rolle hos noen arrangører")
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ansatt har ikke $rolle rolle. Se secure logs for deltaljer")
		}
	}

	override fun harRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle): Boolean {
		val ansatt = arrangorAnsattService.getAnsatt(ansattId)
		val tilgangerHosArrangor = ansatt.arrangorer.first {it.id == arrangorId}
		return tilgangerHosArrangor.roller.contains(rolle)
	}

	private fun hentRoller(ansattId: UUID): List<ArrangorAnsattRolle> {
		return hentAnsattTilganger(ansattId)
			.flatMap { it.roller }
	}

	private fun harLagtTilGjennomforing(ansattId: UUID, gjennomforingId: UUID): Boolean {
		val gjennomforinger = mineDeltakerlisterService.hent(ansattId)
		return gjennomforinger.contains(gjennomforingId)
	}

	private fun synkroniserAltinnRettigheter(ansattPersonligIdent: String) {
		val altinnRoller = altinnService.hentTiltaksarrangorRoller(ansattPersonligIdent)
		val maybeAnsatt = arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
		if (altinnRoller.isEmpty() && maybeAnsatt == null) {
			log.warn("En ikke-ansatt har logget inn, men hadde ikke tilganger i Altinn.")
			return
		}

		val ansatt = arrangorAnsattService.opprettAnsattHvisIkkeFinnes(ansattPersonligIdent)

		val lagredeAnsattTilganger = ansattRolleService.hentAktiveRoller(ansatt.id)
			.flatMap { it.roller.map { r -> AnsattTilgang(it.arrangorId, r) } }

		val altinnTilganger = altinnRoller
			.flatMap {
				val arrangor = arrangorService.getOrCreateArrangor(it.organisasjonsnummer)

				return@flatMap it.roller.map { rolle -> AnsattTilgang(arrangor.id, rolle) }
			}

		val tilgangerSomSkalLeggesTil = finnTilgangerSomSkalLeggesTil(altinnTilganger, lagredeAnsattTilganger)
		val tilgangerSomSkalFjernes = finnTilgangerSomSkalFjernes(altinnTilganger, lagredeAnsattTilganger)

		tilgangerSomSkalLeggesTil.forEach {
			ansattRolleService.opprettRolle(UUID.randomUUID(), ansatt.id, it.arrangorId, it.arrangorAnsattRolle)
			log.info("La til ny tilgang under synk med Altinn. ansattId=${ansatt.id} arrangorId=${it.arrangorId} rolle=${it.arrangorAnsattRolle}")
		}
		tilgangerSomSkalFjernes.forEach { tilgang ->
			transactionTemplate.executeWithoutResult {
				ansattRolleService.deaktiverRolleHosArrangor(ansatt.id, tilgang.arrangorId, tilgang.arrangorAnsattRolle)
				mineDeltakerlisterService.fjernAlleHosArrangor(ansatt.id, tilgang.arrangorId)
			}
			log.info("Fjernet tilgang under synk med Altinn. ansattId=${ansatt.id} arrangorId=${tilgang.arrangorId} rolle=${tilgang.arrangorAnsattRolle}")
		}

		arrangorAnsattService.setTilgangerSistSynkronisert(ansatt.id, LocalDateTime.now())
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
