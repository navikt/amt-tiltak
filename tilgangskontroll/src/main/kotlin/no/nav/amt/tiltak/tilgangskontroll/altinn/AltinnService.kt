package no.nav.amt.tiltak.tilgangskontroll.altinn

import com.github.benmanes.caffeine.cache.Caffeine
import no.nav.amt.tiltak.clients.amt_altinn_acl.AmtAltinnAclClient
import no.nav.amt.tiltak.clients.amt_altinn_acl.TiltaksarrangorAnsattRolle
import no.nav.amt.tiltak.clients.amt_altinn_acl.TiltaksarrangorAnsattRoller
import no.nav.amt.tiltak.common.utils.CacheUtils
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AltinnService(
	private val amtAltinnAclClient: AmtAltinnAclClient
) {

	private val personligIdentToRolleCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1))
		.build<String, List<TiltaksarrangorAnsattRoller>>()

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

	private fun TiltaksarrangorAnsattRolle.mapTilAnsattRolle(): ArrangorAnsattRolle {
		return when (this) {
			TiltaksarrangorAnsattRolle.KOORDINATOR -> ArrangorAnsattRolle.KOORDINATOR
			TiltaksarrangorAnsattRolle.VEILEDER -> ArrangorAnsattRolle.VEILEDER
		}
	}
}

data class ArrangorAnsattRoller(
	val organisasjonsnummer: String,
	val roller: List<ArrangorAnsattRolle>,
)
