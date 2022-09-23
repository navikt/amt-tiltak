package no.nav.amt.tiltak.tilgangskontroll.tilgang

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.amt.tiltak.tilgangskontroll.altinn.AltinnService
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils.tryCacheFirstNotNull
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils.tryCacheFirstNullable
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.util.*

@Service
open class ArrangorAnsattTilgangServiceImpl(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val ansattRolleService: AnsattRolleService,
	private val deltakerService: DeltakerService,
	private val altinnService: AltinnService,
	private val arrangorAnsattGjennomforingTilgangService: ArrangorAnsattGjennomforingTilgangService,
	private val arrangorService: ArrangorService,
	private val transactionTemplate: TransactionTemplate
) : ArrangorAnsattTilgangService {

	private val log = LoggerFactory.getLogger(javaClass)

	private val personligIdentToAnsattIdCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(12))
		.maximumSize(100_000)
		.build<String, UUID>()

	private val ansattIdToArrangorIdListCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofMinutes(15))
		.maximumSize(100_000)
		.build<UUID, List<UUID>>()

	override fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		val gjennomforinger = arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)

		val harTilgang = gjennomforinger.contains(gjennomforingId)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til gjennomføring med id=$gjennomforingId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}
	}

	override fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		val harTilgang = hentArrangorIderForAnsatt(ansattId).contains(arrangorId)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til arrangør med id=$arrangorId")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}
	}

	override fun verifiserTilgangTilDeltaker(ansattPersonligIdent: String, deltakerId: UUID) {
		val deltaker = deltakerService.hentDeltaker(deltakerId)?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltaker.gjennomforingId)
	}

	override fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller> {
		return ansattRolleService.hentAktiveRoller(ansattId)
	}

	override fun hentAnsattId(ansattPersonligIdent: String): UUID {
		val ansattId = tryCacheFirstNullable(personligIdentToAnsattIdCache, ansattPersonligIdent) {
			arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)?.id
		}

		if (ansattId == null) {
			secureLog.warn("Fnr $ansattPersonligIdent er ikke ansatt hos en tiltaksarrangør")
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		return ansattId
	}

	override fun opprettTilgang(ansattPersonligIdent: String, gjennomforingId: UUID) {
		val ansatt = hentAnsatt(ansattPersonligIdent)

		arrangorAnsattGjennomforingTilgangService.opprettTilgang(
			UUID.randomUUID(),
			ansatt.id,
			gjennomforingId
		)
	}

	override fun fjernTilgang(ansattPersonligIdent: String, gjennomforingId: UUID) {
		val ansatt = hentAnsatt(ansattPersonligIdent)

		arrangorAnsattGjennomforingTilgangService.fjernTilgang(ansatt.id, gjennomforingId)
	}

	override fun synkroniserRettigheterMedAltinn(ansattPersonligIdent: String) {
		try {
		    synkroniserAltinnRettigheter(ansattPersonligIdent)
		} catch (t: Throwable) {
			log.error("Feil under synkronisering av altinn rettigheter", t)
			secureLog.error("Feil under synkronisering av altinn rettigheter for fnr=$ansattPersonligIdent", t)
		}
	}

	override fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID> {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		return arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)
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
		}
		tilgangerSomSkalFjernes.forEach { tilgang ->
			transactionTemplate.executeWithoutResult {
				ansattRolleService.deaktiverRolleHosArrangor(ansatt.id, tilgang.arrangorId, tilgang.arrangorAnsattRolle)
				arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(ansatt.id, tilgang.arrangorId)
			}
		}
	}

	private fun finnTilgangerSomSkalLeggesTil(altinnTilganger: List<AnsattTilgang>, lagredeTilganger: List<AnsattTilgang>): List<AnsattTilgang> {
		// Returnerer alle altinnTilganger som ikke finnes i lagredeTilganger
		return altinnTilganger.subtract(lagredeTilganger.toSet()).toList()
	}

	private fun finnTilgangerSomSkalFjernes(altinnTilganger: List<AnsattTilgang>, lagredeTilganger: List<AnsattTilgang>): List<AnsattTilgang> {
		// Returnerer alle lagredeTilganger som ikke finnes i altinnTilganger
		return lagredeTilganger.subtract(altinnTilganger.toSet()).toList()
	}

	private fun hentAnsatt(ansattPersonligIdent: String): Ansatt {
		return arrangorAnsattService.getAnsattByPersonligIdent(ansattPersonligIdent)
			?: throw IllegalStateException("Fant ingen arrangør ansatt med personlig ident")
	}

	private fun hentArrangorIderForAnsatt(ansattId: UUID): List<UUID> {
		return tryCacheFirstNotNull(ansattIdToArrangorIdListCache, ansattId) {
			ansattRolleService.hentArrangorIderForAnsatt(ansattId)
		}
	}

	private data class AnsattTilgang(
		val arrangorId: UUID,
		val arrangorAnsattRolle: ArrangorAnsattRolle
	)
}


