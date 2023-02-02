package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.common.utils.CacheUtils.tryCacheFirstNotNull
import no.nav.amt.tiltak.common.utils.CacheUtils.tryCacheFirstNullable
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.altinn.AltinnService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
open class ArrangorAnsattTilgangServiceImpl(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val ansattRolleService: AnsattRolleService,
	private val deltakerService: DeltakerService,
	private val altinnService: AltinnService,
	private val arrangorAnsattGjennomforingTilgangService: ArrangorAnsattGjennomforingTilgangService,
	private val arrangorService: ArrangorService,
	private val transactionTemplate: TransactionTemplate,
	private val gjennomforingService: GjennomforingService
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

	override fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID, rolle: ArrangorAnsattRolle) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		verifiserTilgangTilGjennomforing(ansattId, gjennomforingId, rolle)
	}

	override fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID, rolle: ArrangorAnsattRolle) {
		val gjennomforinger = arrangorAnsattGjennomforingTilgangService.hentGjennomforingerForAnsatt(ansattId)

		val harTilgang = gjennomforinger.contains(gjennomforingId)

		shouldHaveRolleOnArrangorWithGjennomforing(ansattId, gjennomforingId, rolle)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til gjennomføring med id=$gjennomforingId")
			throw UnauthorizedException("Ansatt har ikke tilgang til gjennomforing")
		}
	}

	override fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		val ansattId = hentAnsattId(ansattPersonligIdent)

		verifiserTilgangTilArrangor(ansattId, arrangorId, rolle)
	}

	override fun verifiserTilgangTilArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		val harTilgang = hentArrangorIderForAnsatt(ansattId).contains(arrangorId)

		shouldHaveRolleOnArrangor(ansattId, arrangorId, rolle)

		if (!harTilgang) {
			secureLog.warn("Ansatt med id=$ansattId har ikke tilgang til arrangør med id=$arrangorId")
			throw UnauthorizedException("Ansatt har ikke tilgang til arrangor")
		}
	}

	override fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID, rolle: ArrangorAnsattRolle) {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		verifiserTilgangTilGjennomforing(ansattId, deltaker.gjennomforingId, rolle)
	}

	override fun verifiserTilgangTilDeltaker(ansattPersonligIdent: String, deltakerId: UUID, rolle: ArrangorAnsattRolle) {
		val deltaker = deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id $deltakerId")

		verifiserTilgangTilGjennomforing(ansattPersonligIdent, deltaker.gjennomforingId, rolle)
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

	override fun shouldHaveRolle(personligIdent: String, rolle: ArrangorAnsattRolle) {
		val ansatt = hentAnsatt(personligIdent)

		val hasRolle = hentAnsattTilganger(ansatt.id)
			.flatMap { it.roller }
			.contains(rolle)

		if (!hasRolle) {
			throw UnauthorizedException("Ansatt med id ${ansatt.id} har ikke $rolle rolle")
		}
	}

	private fun shouldHaveRolleOnArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle) {
		val hasRolle = hentAnsattTilganger(ansattId)
			.find { it.arrangorId === arrangorId }
			?.roller?.contains(rolle)
			?: false

		if (!hasRolle) {
			throw UnauthorizedException("Ansatt med id ${ansattId} har ikke $rolle rolle på Arrangør $arrangorId")
		}
	}

	private fun shouldHaveRolleOnArrangorWithGjennomforing(
		ansattId: UUID,
		gjennomforingId: UUID,
		rolle: ArrangorAnsattRolle
	) {

		val arrangorId = gjennomforingService.getGjennomforing(gjennomforingId).arrangor.id

		val hasRolle = hentAnsattTilganger(ansattId)
			.find { it.arrangorId == arrangorId }
			?.roller?.contains(rolle)
			?: false

		if (!hasRolle) {
			throw UnauthorizedException("Ansatt med id ${ansattId} har ikke $rolle rolle")
		}
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
				arrangorAnsattGjennomforingTilgangService.fjernTilgangTilGjennomforinger(ansatt.id, tilgang.arrangorId)
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


