package no.nav.amt.tiltak.tilgangskontroll.altinn

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt.tiltak.clients.amt_altinn_acl.TiltaksarrangorAnsattRolle
import no.nav.amt.tiltak.clients.amt_altinn_acl.TiltaksarrangorAnsattRoller
import no.nav.amt.tiltak.tilgangskontroll.tilgang.AnsattRolle
import no.nav.amt.tiltak.tilgangskontroll.utils.CacheUtils
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AltinnService(
	private val amtAltinnAclClient: AmtAltinnAclClient
) {

	private val personligIdentToRolleCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, List<TiltaksarrangorAnsattRoller>>()

	fun hentVirksomheterMedKoordinatorRettighet(ansattPersonligIdent: String): List<String> {
		return hentAnsattRoller(ansattPersonligIdent)
			.filter { it.roller.contains(TiltaksarrangorAnsattRolle.KOORDINATOR) }
			.map { it.organisasjonsnummer }
	}

	fun hentTiltaksarrangorRoller(ansattPersonligIdent: String): List<ArrangorAnsattRoller> {
		return hentAnsattRoller(ansattPersonligIdent)
			.map {
				ArrangorAnsattRoller(
					it.organisasjonsnummer,
					it.roller.map { r -> r.mapTilAnsattRolle() }
				)
			}
	}

	private fun hentAnsattRoller(ansattPersonligIdent: String): List<TiltaksarrangorAnsattRoller> {
		return CacheUtils.tryCacheFirstNotNull(personligIdentToRolleCache, ansattPersonligIdent) {
			return@tryCacheFirstNotNull amtAltinnAclClient.hentTiltaksarrangorRoller(ansattPersonligIdent)
		}
	}

	private fun TiltaksarrangorAnsattRolle.mapTilAnsattRolle(): AnsattRolle {
		return when (this) {
			TiltaksarrangorAnsattRolle.KOORDINATOR -> AnsattRolle.KOORDINATOR
			TiltaksarrangorAnsattRolle.VEILEDER -> AnsattRolle.VEILEDER
		}
	}
}

data class ArrangorAnsattRoller(
	val organisasjonsnummer: String,
	val roller: List<AnsattRolle>,
)
